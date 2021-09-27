package com.docker.script;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.utils.ConcurrentHashSet;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.alibaba.fastjson.JSON;
import com.docker.data.*;
import com.docker.rpc.remote.stub.RemoteServersManager;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.ServersService;
import com.docker.storage.adapters.impl.DeployServiceVersionServiceImpl;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.ServiceVersionServiceImpl;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import script.file.FileAdapter;
import script.file.FileAdapter.FileEntity;
import script.file.FileAdapter.PathEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.RuntimeBootListener;
import script.utils.ShutdownListener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.CRC32;


public class ScriptManager implements ShutdownListener {
    private static final String TAG = ScriptManager.class.getSimpleName();
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    private ServersService serversService;
    @Autowired
    private DockerStatusServiceImpl dockerStatusService;
    @Autowired
    private ServiceVersionServiceImpl serviceVersionService;
    @Autowired
    private DeployServiceVersionServiceImpl deployServiceVersionService;
    //是否允许热更
    private Boolean hotDeployment;
    //加载时，当某个包发生错误是否强制退出进程(开发环境不退出，线上退出)
    private Boolean killProcess;
    private Boolean useHulkAdmin;
    private String serverType;
    private String remotePath;
    private String localPath;
    private ConcurrentHashMap<String, BaseRuntime> scriptRuntimeMap = new ConcurrentHashMap<>();
    private Class<?> baseRuntimeClass;
    private Map<String, Integer> defalutServiceVersionMap = new ConcurrentHashMap<>();
    boolean isShutdown = false;
    boolean isLoaded = false;
    private final String versionSeperator = "_v";
    private DeployServiceVersion deployServiceVersion;
    private FileAdapter fileAdapter;

    public static final String SERVICE_NOTFOUND = "servicenotfound";
    public static final Boolean DELETELOCAL = false;
    private final int compileOnceNumber = 1;
    private final boolean compileAllService = false;
    private String runtimeBootClass;

    public void init() {
        File dockerFile = new File(localPath + "/" + OnlineServer.getInstance().getDockerName());
        if (dockerFile.exists()) {
            File[] serviceFiles = dockerFile.listFiles();
            if(serviceFiles != null){
                for (File serviceFile : serviceFiles) {
                    try {
                        FileUtils.deleteDirectory(new File(localPath + "/" + OnlineServer.getInstance().getDockerName() + "/" + serviceFile.getName() + "/groovy"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (hotDeployment) {
            TimerEx.schedule(new TimerTaskEx(ScriptManager.class.getSimpleName()) {
                @Override
                public void execute() {
                    if (!isLoaded) {
                        synchronized (ScriptManager.this) {
                            if (!isShutdown && !isLoaded)
                                reload();
                        }
                    }
                }
            }, 5000L, 10000L);
        } else {
            Long time = System.currentTimeMillis();
            reload();
            LoggerEx.info(TAG, "Reload server spend time: " + (System.currentTimeMillis() - time));
        }
    }

    public Set<Map.Entry<String, BaseRuntime>> getBaseRunTimes() {
        return scriptRuntimeMap.entrySet();
    }

    private String getServiceName(String service) {
        Integer version = null;
        int lastIndex = service.lastIndexOf(versionSeperator);
        if (lastIndex > 0) {
            String curVerStr = service.substring(lastIndex + versionSeperator.length());
            if (curVerStr != null) {
                try {
                    version = Integer.parseInt(curVerStr);
                } catch (Exception e) {
                }
            }
            if (version != null) {
                String serviceName = service.substring(0, lastIndex);
                return serviceName;
            }
        }
        return service;
    }

    private Integer getServiceVersion(String service) {
        Integer version = null;
        int lastIndex = service.lastIndexOf(versionSeperator);
        if (lastIndex > 0) {
            String curVerStr = service.substring(lastIndex + versionSeperator.length());
            if (curVerStr != null) {
                try {
                    version = Integer.parseInt(curVerStr);
                } catch (Exception e) {
                }
            }
        }
        if (version == null)
            version = 1;
        return version;
    }

    private void reload() {
        try {
//            if (testCount == 10) {
//                testCount = 1;
//                if (testStatus == 0) {
//                    testStatus = 1;
//                } else if (testStatus == 1) {
//                    testStatus = 0;
//                }
//            }
//            LoggerEx.info(TAG, "Reload again", "gwsfusignal_statistics", "peerId: 7vf8d9s7v8f9ds" + testCount.toString() + " ||| status: " + testStatus.toString());
//            testCount++;
//            isLoaded = true;
            List<String> serviceVersionFinalList = getDeployServiceVersions();
            if (!serviceVersionFinalList.isEmpty()) {
                Set<String> remoteServices = new ConcurrentHashSet<>();
                List<Service> services = new CopyOnWriteArrayList<>();
                if (!compileAllService) {
                    while (!serviceVersionFinalList.isEmpty()) {
                        List<String> serviceVersionList = new ArrayList<>();
                        for (int i = 0; i < serviceVersionFinalList.size(); i++) {
                            if (serviceVersionList.size() < compileOnceNumber) {
                                serviceVersionList.add(serviceVersionFinalList.remove(i));
                            } else {
                                break;
                            }
                        }
                        if (!serviceVersionList.isEmpty()) {
                            CountDownLatch scriptCountDownLatch = new CountDownLatch(serviceVersionList.size());
                            for (String theServiceVersion : serviceVersionList) {
                                ServerStart.getInstance().getThreadPool().execute(() -> {
                                    complieService(theServiceVersion, remoteServices, services);
                                    scriptCountDownLatch.countDown();
                                });
                            }
                            scriptCountDownLatch.await();
                        }
                    }
                } else {
                    CountDownLatch scriptCountDownLatch = new CountDownLatch(serviceVersionFinalList.size());
                    for (String theServiceVersion : serviceVersionFinalList) {
                        ServerStart.getInstance().getThreadPool().execute(() -> {
                            complieService(theServiceVersion, remoteServices, services);
                            scriptCountDownLatch.countDown();
                        });
                    }
                    scriptCountDownLatch.await();
                }
                if (!services.isEmpty()) {
                    for (Service service : services) {
                        if (service != null && dockerStatusService != null) {
                            dockerStatusService.addService(OnlineServer.getInstance().getServer(), service);
                        }
                    }
                }
                DockerStatus dockerStatus = dockerStatusService.getDockerStatusByServer(OnlineServer.getInstance().getServer());
                if (dockerStatus.getStatus() != DockerStatus.STATUS_OK) {
                    dockerStatus.setStatus(DockerStatus.STATUS_OK);
                    dockerStatusService.update(OnlineServer.getInstance().getServer(), dockerStatus);
                    LoggerEx.info(TAG, "================ This dockerStatus reload finish =======================");
                }
                if (!useHulkAdmin) {
                    updateServiceVersion(deployServiceVersion);
                }
                Collection<String> keys = scriptRuntimeMap.keySet();
                for (String key : keys) {
                    if (!remoteServices.contains(key)) {
                        BaseRuntime runtime = scriptRuntimeMap.remove(key);
                        if (runtime != null) {
                            LoggerEx.info(TAG, "Service " + key + " is going to be removed, because it is not found in remote.");
                            try {
                                runtime.close();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            } finally {
                                try {
                                    String serviceName = getServiceName(key);
                                    Integer version = getServiceVersion(key);
                                    if (dockerStatusService != null) {
                                        dockerStatusService.deleteService(OnlineServer.getInstance().getServer(), serviceName, version);
                                    }
                                } catch (CoreException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Delete service " + key + " from docker " + OnlineServer.getInstance().getServer() + " failed, " + ExceptionUtils.getFullStackTrace(e));
                                }
                            }
                        }
                    }
                }
            } else {
                LoggerEx.info(TAG, "ServerType: " + serverType + " has no services");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            if (killProcess) {
                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
                handleFailedDeploy(e);
                System.exit(1);
            }
        } finally {
            isLoaded = false;
        }

    }

    private void updateServiceVersion(DeployServiceVersion deployServiceVersion) {
        try {
            List<ServiceVersion> serviceVersionList = serviceVersionService.getServiceVersionsByType(deployServiceVersion.getServerType(), deployServiceVersion.getType());
            if (serviceVersionList != null && !serviceVersionList.isEmpty()) {
                for (ServiceVersion serviceVersion : serviceVersionList) {
                    serviceVersion.setServiceVersions(deployServiceVersion.getServiceVersions());
                    serviceVersion.setDeployId(deployServiceVersion.getDeployId());
                    serviceVersionService.addServiceVersion(serviceVersion);
                }
            } else {
                ServiceVersion serviceVersion = new ServiceVersion();
                List<String> list = new ArrayList<>();
                list.add(deployServiceVersion.getServerType());
                serviceVersion.setServerType(list);
                serviceVersion.set_id(deployServiceVersion.get_id());
                serviceVersion.setType(deployServiceVersion.getType());
                serviceVersion.setServiceVersions(deployServiceVersion.getServiceVersions());
                serviceVersion.setDeployId(deployServiceVersion.getDeployId());
                serviceVersionService.addServiceVersion(serviceVersion);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private void complieService(String theServiceVersion, Set<String> remoteServices, List<Service> services) {
        String zipFile = "groovy.zip";
        try {
            FileEntity fileEntity = fileAdapter.getFileEntity(new PathEx(remotePath + theServiceVersion + "/" + zipFile));
            if (fileEntity != null) {
                boolean createRuntime = false;
                String service = theServiceVersion;
                String localScriptPath = null;
                String serverTypePath = "/" + OnlineServer.getInstance().getDockerName() + "/";
                remoteServices.add(service);
                BaseRuntime runtime = scriptRuntimeMap.get(service);
                boolean needRedeploy = false;
                //之前已经解析过了，如果有新的groovy.zip生成
                if (runtime != null && (runtime.getVersion() == null || runtime.getVersion() < fileEntity.getLastModificationTime())) {
                    needRedeploy = true;
                    try {
                        runtime.close();
                        scriptRuntimeMap.remove(service);
                        LoggerEx.error(TAG, "Runtime " + runtime + " service " + service + " closed because of deployment");
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "close runtime " + runtime + " service " + service + " failed, " + ExceptionUtils.getFullStackTrace(t));
                    } finally {
                        runtime = null;
                    }
                }
                if (runtime == null) {
                    createRuntime = true;
                    needRedeploy = true;
                    if (baseRuntimeClass != null) {
                        runtime = (BaseRuntime) baseRuntimeClass.getDeclaredConstructor().newInstance();
                    } else {
                        runtime = new MyBaseRuntime();
                    }
                    beanFactory.autowireBean(runtime);
                    RuntimeBootListener bootListener = null;
                    if (runtimeBootClass != null) { //script.groovy.runtime.GroovyBooter
                        Class<?> bootClass = Class.forName(runtimeBootClass);
                        bootListener = (RuntimeBootListener) bootClass.getDeclaredConstructor().newInstance();
                        bootListener.setGroovyRuntime(runtime);
                    }
                    runtime.setRuntimeBootListener(bootListener);
                    if (runtime != null) {
                        localScriptPath = localPath + serverTypePath + service + "/" + zipFile.split("\\.")[0];
                        runtime.setPath(localScriptPath + "/");
                    }
                }

                if (runtime != null && needRedeploy) {
                    File localZipFile = new File(localPath + serverTypePath + theServiceVersion + "/" + zipFile);
                    FileUtils.deleteQuietly(localZipFile);
                    OutputStream zipOs = FileUtils.openOutputStream(localZipFile);
                    fileAdapter.readFile(new PathEx(fileEntity.getAbsolutePath()), zipOs);
                    IOUtils.closeQuietly(zipOs);

                    String n = localZipFile.getName();
                    n = n.substring(0, n.length() - ".zip".length());
                    localScriptPath = localPath + serverTypePath + service + "/" + n;
                    File groovyFile = new File(localScriptPath);
                    if(groovyFile.exists()){
                        File[] groovyFiles = groovyFile.listFiles();
                        if(groovyFiles != null){
                            for (int i = 0; i < groovyFiles.length; i++) {
                                try {
                                    FileUtils.deleteQuietly(groovyFiles[i]);
                                }catch(Throwable t){}
                            }
                        }
                    }
                    CRC32 crc = new CRC32();
                    crc.update(zipFile.getBytes());
                    long valuePwd = crc.getValue();
                    unzip(localZipFile, localScriptPath, String.valueOf(valuePwd));

                    Service theService = null;
                    if (createRuntime) {
                        Integer version = getServiceVersion(service);
                        String serviceName = getServiceName(service);
                        runtime.setServiceName(serviceName);
                        runtime.setServiceVersion(version);
                        Properties properties = prepareServiceProperties(localScriptPath, service);
                        //触发serviceVersions
                        runtime.prepare(service, properties, localScriptPath);

                        theService = new Service();
                        theService.setService(serviceName);
                        theService.setVersion(version);
                        theService.setUploadTime(fileEntity.getLastModificationTime());
                        if (properties.get(Service.FIELD_MAXUSERNUMBER) != null) {
                            theService.setMaxUserNumber(Long.valueOf((String) properties.get(Service.FIELD_MAXUSERNUMBER)));
                        }
                        if (dockerStatusService != null) {
                            //Aplomb delete service first before add, fixed the duplicated service bug.
                            dockerStatusService.deleteService(OnlineServer.getInstance().getServer(), theService.getService(), theService.getVersion());
                        }

                        scriptRuntimeMap.put(service, runtime);
                        //使用新的容器是因为防止删除的一瞬间， 获取为空的问题， 因此采用新容器更换的办法。
                    } else {
                        Integer version = getServiceVersion(service);
                        String serviceName = getServiceName(service);
                        if (dockerStatusService != null)
                            dockerStatusService.updateServiceUpdateTime(OnlineServer.getInstance().getServer(), serviceName, version, fileEntity.getLastModificationTime());
                    }
                    try {
                        runtime.setVersion(fileEntity.getLastModificationTime());
                        runtime.start();
                        Collection<ClassAnnotationHandler> handlers = runtime.getAnnotationHandlers();
                        if (handlers != null && theService != null) {
                            for (ClassAnnotationHandler handler : handlers) {
                                if (handler instanceof ClassAnnotationHandlerEx)
                                    ((ClassAnnotationHandlerEx) handler).configService(theService);
                            }
                        }
                        theService.setType(Service.FIELD_SERVER_TYPE_NORMAL);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "Redeploy service " + service + " failed, " + ExceptionUtils.getFullStackTrace(t));
                        theService.setType(Service.FIELD_SERVER_TYPE_DEPLOY_FAILED);
                        throw t;
                    } finally {
                        services.add(theService);
                        if (DELETELOCAL) {
                            String servicePath = serverTypePath + service;
                            File localFile = new File(localPath + servicePath);
                            File temp = null;
                            Collection<File> fileList = FileUtils.listFiles(localFile, new String[]{"groovy", "zip"}, true);
                            if (fileList != null)
                                for (File file1 : fileList) {
                                    try {
                                        file1.delete();
                                    } catch (Throwable t) {
                                        LoggerEx.error(TAG, "delete file failed, file" + file1.getName());
                                    }
                                }
                            LoggerEx.info(TAG, "delete localFile: " + localFile + " success");
                        }
                        LoggerEx.info(TAG, "=====Notice!!! The service: " + theServiceVersion + " has being redeployed====");
                    }
                }
            } else {
                LoggerEx.error(TAG, "Failed get groovy.zip, service is " + theServiceVersion);
                throw new CoreException(ChatErrorCodes.ERROR_NO_GROOVYFILE, "Failed get groovy.zip, service is " + theServiceVersion);
            }
        } catch (Throwable e) {
            if (killProcess) {
                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
                handleFailedDeploy(e);
                System.exit(1);
            } else {
                e.printStackTrace();
                LoggerEx.error(TAG, "err: " + e);
            }
        }
    }

    private void handleFailedDeploy(Throwable t) {
        try {
            DockerStatus dockerStatus = dockerStatusService.getDockerStatusByServer(OnlineServer.getInstance().getServer());
            if (dockerStatus != null) {
                dockerStatus.setStatus(DockerStatus.STATUS_FAILED);
                dockerStatus.setFailedReason(t.getMessage());
                dockerStatusService.update(OnlineServer.getInstance().getServer(), dockerStatus);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Properties prepareServiceProperties(String localScriptPath, String serviceName) throws Throwable {
        String propertiesPath = localScriptPath + "/config.properties";
        Properties properties = new Properties();
        File propertiesFile = new File(propertiesPath);
        if (propertiesFile.exists() && propertiesFile.isFile()) {
            InputStream is = FileUtils.openInputStream(propertiesFile);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            properties.load(reader);
            reader.close();
            IOUtils.closeQuietly(is);
        }

        try {
            if (serversService != null) {
                Document configDoc = serversService.getServerConfig(serviceName);
                if (configDoc != null) {
                    String configDependencies = configDoc.getString("config_dependencies");
                    Map<String, Object> configDependencyMap = null;
                    if (configDependencies != null) {
                        String[] theConfigDependencies = configDependencies.split(",");
                        if (theConfigDependencies.length > 0) {
                            configDependencyMap = new ConcurrentHashMap<>();
                            for (int i = theConfigDependencies.length - 1; i >= 0; i--) {
                                Document configDependencyDoc = serversService.getServerConfig(theConfigDependencies[i]);
                                if (configDependencyDoc != null) {
                                    configDependencyMap.putAll(configDependencyDoc);
                                }
                            }
                        }
                    }
                    if (configDependencyMap == null) {
                        configDependencyMap = configDoc;
                    } else {
                        configDependencyMap.putAll(configDoc);
                    }
                    configDependencyMap.remove(DataObject.FIELD_ID);
                    Set<String> keys = configDependencyMap.keySet();
                    for (String key : keys) {
                        properties.put(key.replaceAll("_", "."), configDependencyMap.get(key));
                    }
                }
                if (!properties.isEmpty()) {
                    for (Object key : properties.keySet()) {
                        Object value = properties.get(key);
                        if (value instanceof String) {
                            if (StringUtils.isBlank((String) value)) {
                                properties.remove(key);
                            }
                        }
                    }
                }
                LoggerEx.info(TAG, "Read service: " + serviceName + ", merge config: " + properties);
            } else {
                LoggerEx.info(TAG, "serversService is null, will not read config from database for service " + serviceName);
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "Read server " + serviceName + " config failed, " + ExceptionUtils.getFullStackTrace(t));
        }
        return properties;
    }

    public BaseRuntime getBaseRuntime(String service) {
        if (service == null) return null;
        if (!service.contains(versionSeperator)) {
            if (defalutServiceVersionMap.size() > 0) {
                Integer version = defalutServiceVersionMap.get(service);
                if (version != null && version != -1) {
                    service = service + versionSeperator + version;
                }
            }
        }
        BaseRuntime runtime = scriptRuntimeMap.get(service);
        if (runtime == null) {
            LoggerEx.error(TAG, "Service " + service + "'s baseRuntime is null, remoteServersManager serviceMaxVersionMap: " + JSON.toJSONString(RemoteServersManager.getInstance().getServiceMaxVersionMap()) + ",severType: " + serverType);
        }
        return runtime;
    }

    private List<String> getDeployServiceVersions() throws CoreException {
        deployServiceVersion = deployServiceVersionService.getServiceVersion(serverType);
        List<String> finalServiceVersions = new ArrayList<>();
        if (deployServiceVersion != null) {
            if (deployServiceVersion.getDeployId() != null) {
                dockerStatusService.updateDeployId(OnlineServer.getInstance().getServer(), deployServiceVersion.getDeployId());
            }
            Map<String, String> serviceVersions = deployServiceVersion.getServiceVersions();
            if (serviceVersions != null) {
                for (String service : serviceVersions.keySet()) {
                    finalServiceVersions.add(service + "_v" + serviceVersions.get(service));
                    defalutServiceVersionMap.put(service, Integer.valueOf(serviceVersions.get(service)));
                }
            }
        }
        return finalServiceVersions;
    }

    private void unzip(File zipFile, String dir, String passwd) throws CoreException {
        ZipFile zFile = null;
        try {
            zFile = new ZipFile(zipFile);
            File destDir = new File(dir);
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }
            zFile.extractAll(dir);

            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            File[] extractedFiles = new File[extractedFileList.size()];
            extractedFileList.toArray(extractedFiles);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "password is error,destFile:" + dir);
        }
    }

    @Override
    public synchronized void shutdown() {
        isShutdown = true;
        if (!killProcess) {
            try {
                dockerStatusService.deleteDockerStatus(OnlineServer.getInstance().getServer());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        Collection<String> keys = scriptRuntimeMap.keySet();
        for (String key : keys) {
            BaseRuntime runtime = scriptRuntimeMap.get(key);
            if (runtime != null) {
                LoggerEx.info(TAG, "Service " + key + " is going to be removed, because of shutdown");
                try {
                    runtime.close();
                    LoggerEx.info(TAG, "Service " + key + " has been removed, because of shutdown");
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.info(TAG, "Service " + key + " remove failed, " + ExceptionUtils.getFullStackTrace(t));
                } finally {
                    scriptRuntimeMap.remove(key);
                }
            }
        }
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Class<?> getBaseRuntimeClass() {
        return baseRuntimeClass;
    }

    public void setBaseRuntimeClass(Class<?> baseRuntimeClass) {
        this.baseRuntimeClass = baseRuntimeClass;
    }

    public String getRuntimeBootClass() {
        return runtimeBootClass;
    }

    public void setRuntimeBootClass(String runtimeBootClass) {
        this.runtimeBootClass = runtimeBootClass;
    }

    public ServersService getServersService() {
        return serversService;
    }

    public void setServersService(ServersService serversService) {
        this.serversService = serversService;
    }

    public Boolean getHotDeployment() {
        return hotDeployment;
    }

    public void setHotDeployment(Boolean hotDeployment) {
        this.hotDeployment = hotDeployment;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public Boolean getKillProcess() {
        return killProcess;
    }

    public void setKillProcess(Boolean killProcess) {
        this.killProcess = killProcess;
    }

    public Boolean getUseHulkAdmin() {
        return useHulkAdmin;
    }

    public void setUseHulkAdmin(Boolean useHulkAdmin) {
        this.useHulkAdmin = useHulkAdmin;
    }

    public Map<String, Integer> getDefalutServiceVersionMap() {
        return defalutServiceVersionMap;
    }

    public void setFileAdapter(FileAdapter fileAdapter) {
        this.fileAdapter = fileAdapter;
    }
}
