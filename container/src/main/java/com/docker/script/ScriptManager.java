package com.docker.script;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.data.Service;
import com.docker.data.ServiceVersion;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.ServersService;
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
import script.file.FileAdapter.FileEntity;
import script.file.FileAdapter.PathEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.RuntimeBootListener;
import script.groovy.servlets.grayreleased.GrayReleased;
import script.utils.ShutdownListener;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;


public class ScriptManager implements ShutdownListener {
    private static final String TAG = ScriptManager.class.getSimpleName();
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    private GridFSFileHandler fileAdapter;
    @Autowired
    private ServersService serversService;
    @Autowired
    private DockerStatusServiceImpl dockerStatusService;
    @Autowired
    private ServiceVersionServiceImpl serviceVersionService;
    //是否允许热更
    private Boolean hotDeployment;
    //加载时，当某个包发生错误是否强制退出进程(开发环境不退出，线上退出)
    private Boolean killProcess;
    private String serverType;
    private String remotePath;
    private String localPath;
    private ConcurrentHashMap<String, BaseRuntime> scriptRuntimeMap = new ConcurrentHashMap<>();
    private Class<?> baseRuntimeClass;
    private Map<String, Integer> defalutServiceVersionMap = new ConcurrentHashMap<>();
    boolean isShutdown = false;
    boolean isLoaded = false;
    private final String versionSeperator = "_v";

    public static final String SERVICE_NOTFOUND = "servicenotfound";
    public static final Boolean DELETELOCAL = false;

    private String runtimeBootClass;

    public void init() {
//        if(dockerStatusService != null){
//            try {
//                dockerStatusService.deleteDockerStatusByServerType(serverType);
//            } catch (CoreException e) {
//                LoggerEx.info(TAG, "No serveral server,serverType: " + serverType);
//            }
//        }
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
            reload();
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
            isLoaded = true;
            List<String> serviceVersionFinalList = getServiceVersions();
            if (serviceVersionFinalList != null) {
                Set<String> remoteServices = new HashSet<>();
                for (String theServiceVersion : serviceVersionFinalList) {
                    String zipFile = "groovy.zip";
                    try {
                        FileEntity fileEntity = fileAdapter.getFileEntity(new PathEx(remotePath + theServiceVersion + "/" + zipFile));
                        if (fileEntity != null) {
                            boolean createRuntime = false;
                            String service = theServiceVersion;
                            String localScriptPath = null;
                            String serverTypePath = "/" + serverType + "/";
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
                                    runtime = (BaseRuntime) baseRuntimeClass.newInstance();
                                } else {
                                    runtime = new MyBaseRuntime();
                                }
                                beanFactory.autowireBean(runtime);
                                RuntimeBootListener bootListener = null;
                                if (runtimeBootClass != null) { //script.groovy.runtime.GroovyBooter
                                    Class<?> bootClass = Class.forName(runtimeBootClass);
                                    bootListener = (RuntimeBootListener) bootClass.newInstance();
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
                                FileUtils.deleteDirectory(new File(localScriptPath));
                                CRC32 crc = new CRC32();
                                crc.update(zipFile.getBytes());
                                long valuePwd = crc.getValue();
                                unzip(localZipFile, localScriptPath, String.valueOf(valuePwd));

                                Service theService = null;
                                if (createRuntime) {
                                    String propertiesPath = localScriptPath + "/config.properties";
                                    Properties properties = new Properties();
                                    File propertiesFile = new File(propertiesPath);
                                    if (propertiesFile.exists() && propertiesFile.isFile()) {
                                        InputStream is = FileUtils.openInputStream(propertiesFile);
                                        InputStreamReader reader = new InputStreamReader(is, "utf-8");
                                        properties.load(reader);
                                        reader.close();
                                        IOUtils.closeQuietly(is);
                                    }
                                    Integer version = getServiceVersion(service);
                                    String serviceName = getServiceName(service);

                                    runtime.setServiceName(serviceName);
                                    runtime.setServiceVersion(version);

                                    try {
                                        if (serversService != null) {
                                            Document configDoc = serversService.getServerConfig(serviceName);
                                            if (configDoc != null) {
                                                Set<String> keys = configDoc.keySet();
                                                for (String key : keys) {
                                                    properties.put(key.replaceAll("_", "."), configDoc.getString(key));
                                                }
                                            }
                                            LoggerEx.info(TAG, "Read service: " + serviceName + ", merge config: " + properties);
                                        } else {
                                            LoggerEx.info(TAG, "serversService is null, will not read config from database for service " + serviceName);
                                        }
                                    } catch (Throwable t) {
                                        LoggerEx.error(TAG, "Read server " + serviceName + " config failed, " + ExceptionUtils.getFullStackTrace(t));
                                    }
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
                                    if (theService != null && dockerStatusService != null) {
                                        dockerStatusService.addService(OnlineServer.getInstance().getServer(), theService);
                                    }
                                    if (DELETELOCAL) {
                                        String servicePath = serverTypePath + service;
                                        File localFile = new File(localPath + servicePath);
                                        File temp = null;
                                        Collection<File> filelist = FileUtils.listFiles(localFile, new String[]{"groovy", "zip"}, true);
                                        try {

                                            if (filelist != null) {
                                                for (File file1 : filelist) {
                                                    file1.delete();
                                                }
                                            }
                                            LoggerEx.info(TAG, "delete localFile: " + localFile + " success");
                                        } catch (Exception e) {
                                            LoggerEx.error(TAG, "delete file failed");
                                            throw e;
                                        }
                                    }
                                }
                                LoggerEx.info(TAG, "=====Notice!!! The service: " + service + " has being redeployed====");
                            }
                        } else {
                            LoggerEx.error(TAG, "Failed get groovy.zip, service is " + theServiceVersion);
                            throw new CoreException(ChatErrorCodes.ERROR_NO_GROOVYFILE, "Failed get groovy.zip, service is " + theServiceVersion);
                        }
                    } catch (Exception e) {
                        if (killProcess) {
                            throw e;
                        } else {
                            e.printStackTrace();
                            LoggerEx.error(TAG, "err: " + e);
                        }
                    }
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
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (killProcess) {
                System.exit(1);
            }
        } finally {
            isLoaded = false;
        }

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
            LoggerEx.error(TAG, "Service " + service + "'s baseRuntime is null");
        }
        return runtime;
    }

    private List<String> getServiceVersions() throws CoreException {
        List<ServiceVersion> serviceVersions = serviceVersionService.getServiceVersions(serverType);

        Map<String, List<String>> serviceVersionFinalMap = new ConcurrentHashMap<>();
        Map<String, Integer> defaultVersionMap = new ConcurrentHashMap<>();
        for (ServiceVersion serviceVersion : serviceVersions) {
            Map<String, Integer> serviceFinalMap = new ConcurrentHashMap<>();
            Map<String, String> serviceVersionMap = serviceVersion.getServiceVersions();
            if (serviceVersionMap != null) {
                for (String serviceName : serviceVersionMap.keySet()) {
                    if (serviceVersion.getType().equals(GrayReleased.defaultVersion)) {
                        if (serviceFinalMap.get(serviceName) == null) {
                            if (StringUtils.isEmpty(serviceVersionMap.get(serviceName))) {
                                defaultVersionMap.put(serviceName, -1);
                            } else {
                                defaultVersionMap.put(serviceName, Integer.valueOf(serviceVersionMap.get(serviceName)));
                            }
                        } else {
                            if (!StringUtils.isEmpty(serviceVersionMap.get(serviceName))) {
                                if (Integer.parseInt(serviceVersionMap.get(serviceName)) > defaultVersionMap.get(serviceName)) {
                                    defaultVersionMap.put(serviceName, Integer.valueOf(serviceVersionMap.get(serviceName)));
                                }
                            }
                        }
                    }
                    if (serviceFinalMap.get(serviceName) == null) {
                        if (StringUtils.isEmpty(serviceVersionMap.get(serviceName))) {
                            serviceFinalMap.put(serviceName, -1);
                        } else {
                            serviceFinalMap.put(serviceName, Integer.valueOf(serviceVersionMap.get(serviceName)));
                        }
                    } else {
                        if (!StringUtils.isEmpty(serviceVersionMap.get(serviceName))) {
                            if (Integer.parseInt(serviceVersionMap.get(serviceName)) > serviceFinalMap.get(serviceName)) {
                                serviceFinalMap.put(serviceName, Integer.valueOf(serviceVersionMap.get(serviceName)));
                            }
                        }
                    }
                }
                List<String> newServiceVersionList = new ArrayList<>();
                if (serviceFinalMap.size() > 0) {
                    for (String serviceName : serviceFinalMap.keySet()) {
                        if (serviceFinalMap.get(serviceName) != -1) {
                            newServiceVersionList.add(serviceName + "_v" + serviceFinalMap.get(serviceName).toString());
                        } else {
                            newServiceVersionList.add(serviceName);
                        }
                    }
                }
                if (newServiceVersionList.size() > 0) {
                    serviceVersionFinalMap.put(serviceVersion.getType(), newServiceVersionList);
                }
            }
        }
        if (!defaultVersionMap.isEmpty()) {
            defalutServiceVersionMap = defaultVersionMap;
        }
        if (serviceVersionFinalMap.size() > 0) {
            List<String> serviceVersionFinalList = new ArrayList<>();
            for (String type : serviceVersionFinalMap.keySet()) {
                List<String> serviceVersionList = serviceVersionFinalMap.get(type);
                if (serviceVersionList != null && serviceVersionList.size() > 0) {
                    for (String serviceVersion : serviceVersionList) {
                        if (!serviceVersionFinalList.contains(serviceVersion)) {
                            serviceVersionFinalList.add(serviceVersion);
                        }
                    }
                }
            }
            return serviceVersionFinalList;
        }
        return null;
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
        Collection<String> keys = scriptRuntimeMap.keySet();
        for (String key : keys) {
            BaseRuntime runtime = scriptRuntimeMap.remove(key);
            if (runtime != null) {
                LoggerEx.info(TAG, "Service " + key + " is going to be removed, because of shutdown");
                try {
                    runtime.close();
                    LoggerEx.info(TAG, "Service " + key + " has been removed, because of shutdown");
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.info(TAG, "Service " + key + " remove failed, " + ExceptionUtils.getFullStackTrace(t));
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

    public Map<String, Integer> getDefalutServiceVersionMap() {
        return defalutServiceVersionMap;
    }
}
