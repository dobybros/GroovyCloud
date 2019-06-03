package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.docker.annotations.ConfigProperty;
import com.docker.annotations.I18nBean;
import com.docker.annotations.ServiceBean;
import com.docker.data.Service;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.script.i18n.I18nHandler;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.storage.adapters.ServersService;
import com.docker.utils.SpringContextUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import script.file.FileAdapter;
import script.file.FileAdapter.FileEntity;
import script.file.FileAdapter.PathEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.FieldInjectionListener;
import script.groovy.runtime.RuntimeBootListener;
import script.utils.ShutdownListener;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;


public class ScriptManager implements ShutdownListener {
    private static final String TAG = ScriptManager.class.getSimpleName();

    //	@Resource
    private FileAdapter fileAdapter;
    //是否允许热更
    private Boolean hotDeployment;
    @Resource
    private ServersService serversService;

    private DockerStatusService dockerStatusService;
//    private RMIServerImplWrapper rpcServer;
//    private RMIServerImplWrapper rpcServerSsl;
    private String remotePath;
    private String localPath;
    private ConcurrentHashMap<String, BaseRuntime> scriptRuntimeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<BaseRuntime>> serviceVersionMap = new ConcurrentHashMap<>();

    private Class<?> baseRuntimeClass;
    boolean isShutdown = false;

    public static final String SERVICE_NOTFOUND = "servicenotfound";
    public static final Boolean DELETELOCAL = false;

    private String runtimeBootClass;

    public void init() {
        if(hotDeployment){
            TimerEx.schedule(new TimerTaskEx() {
                @Override
                public void execute() {
                    synchronized (ScriptManager.this) {
                        if (!isShutdown)
                            reload();
                    }
                }
            }, 5000, TimeUnit.SECONDS.toMillis(10));
        }else {
            reload();
        }
    }

    public BaseRuntime getBaseRuntime(String service) {
        BaseRuntime runtime = scriptRuntimeMap.get(service);
        if (runtime == null) {
            List<BaseRuntime> runtimes = serviceVersionMap.get(service);
            if (runtimes != null && !runtimes.isEmpty()) {
                runtime = runtimes.get(runtimes.size() - 1);
            }
        }
        if (runtime == null) {
            MyBaseRuntime notFoundRuntime = (MyBaseRuntime) scriptRuntimeMap.get(SERVICE_NOTFOUND);
            if (notFoundRuntime != null) {
                runtime = notFoundRuntime.getRuntimeWhenNotFound(service);
            }
        }
        return runtime;
    }

    public Set<Map.Entry<String, BaseRuntime>> getBaseRunTimes() {
        return scriptRuntimeMap.entrySet();
    }

    private String getServiceName(String service) {
        Integer version = null;
        String versionSeperator = "_v";
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
        String versionSeperator = "_v";
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
            Collection<FileEntity> files = fileAdapter.getFilesInDirectory(new PathEx(remotePath), new String[]{"zip"}, true);
            OnlineServer server = OnlineServer.getInstance();
            if (server == null) {
                LoggerEx.error(TAG, "Online server is null while reload scripts");
                return;
            }
            String serverType = server.getServerType();
            if (serverType == null) {
                LoggerEx.error(TAG, "ServerType is null while reload scripts");
                return;
            }
            if (files != null) {
                Set<String> remoteServices = new HashSet<>();
                String serverTypePath = "/" + serverType + "/";
                for (FileEntity file : files) {
                    try {
                        // for example, /gateway/SS/groovy.zip
                        String abPath = file.getAbsolutePath();
                        int index = abPath.indexOf(serverTypePath);
                        boolean createRuntime = false;
                        if (index > -1) {
                            String thePath = abPath.substring(index + serverTypePath.length(), abPath.length()); //SS/groovy.zip
                            String[] strs = thePath.split("/");
                            if (strs.length == 2) {
                                String service = strs[0];
                                String zipFile = strs[1];
                                String language = null;
                                String localScriptPath = null;

                                remoteServices.add(service);
                                BaseRuntime runtime = scriptRuntimeMap.get(service);
                                boolean needRedeploy = false;
                                if (runtime != null && (runtime.getVersion() == null || runtime.getVersion() < file.getLastModificationTime())) {
                                    needRedeploy = true;
                                    try {
                                        runtime.close();
                                        scriptRuntimeMap.remove(service);
                                        List<BaseRuntime> runtimes = serviceVersionMap.get(runtime.getServiceName());
                                        if (runtimes != null && !runtimes.isEmpty()) {
                                            runtimes.remove(runtime);
                                        }
                                        LoggerEx.error(TAG, "Runtime " + runtime + " service " + service + " closed because of deployment");
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        LoggerEx.error(TAG, "close runtime " + runtime + " service " + service + " failed, " + t.getMessage());
                                    } finally {
                                        runtime = null;
                                    }
                                }
                                if (runtime == null) {
                                    createRuntime = true;
                                    needRedeploy = true;
                                    language = zipFile.substring(0, zipFile.length() - ".zip".length()).toLowerCase();
                                    switch (language) {
                                        case "groovy":
                                            if (baseRuntimeClass != null) {
                                                runtime = (BaseRuntime) baseRuntimeClass.newInstance();
                                            } else {
                                                runtime = new MyBaseRuntime();
                                            }
                                            RuntimeBootListener bootListener = null;
                                            if(runtimeBootClass != null) { //script.groovy.runtime.GroovyBooter
                                                Class<?> bootClass = Class.forName(runtimeBootClass);
                                                bootListener = (RuntimeBootListener) bootClass.newInstance();
                                                bootListener.setGroovyRuntime(runtime);
                                            }
                                            runtime.setRuntimeBootListener(bootListener);
                                            if (runtime instanceof MyBaseRuntime) {
                                                final MyBaseRuntime baseRuntime = (MyBaseRuntime) runtime;
                                                runtime.addFieldInjectionListener(new FieldInjectionListener<ServiceBean>() {
                                                    public Class<ServiceBean> annotationClass() {
                                                        return ServiceBean.class;
                                                    }

                                                    @Override
                                                    public void inject(ServiceBean annotation, Field field, Object obj) {
                                                        String serviceName = annotation.name();
                                                        if (!StringUtils.isBlank(serviceName)) {
                                                            baseRuntime.prepareServiceStubProxy();
                                                            Object serviceStub = baseRuntime.getServiceStubManager().getService(serviceName, field.getType());
                                                            if (!field.isAccessible())
                                                                field.setAccessible(true);
                                                            try {
                                                                field.set(obj, serviceStub);
                                                            } catch (Throwable e) {
                                                                e.printStackTrace();
                                                                LoggerEx.error(TAG, "Set field " + field.getName() + " for service " + serviceName + " class " + field.getType() + " in class " + obj.getClass());
                                                            }
                                                        }
                                                    }
                                                });




                                                runtime.addFieldInjectionListener(new FieldInjectionListener<ConfigProperty>() {
                                                    public Class<ConfigProperty> annotationClass() {
                                                        return ConfigProperty.class;
                                                    }

                                                    @Override
                                                    public void inject(ConfigProperty annotation, Field field, Object obj) {
                                                        String key = annotation.name();
                                                        if (!StringUtils.isBlank(key)) {
                                                            Properties properties = baseRuntime.getConfig();
                                                            if (properties == null)
                                                                return;
                                                            String value = properties.getProperty(key);
                                                            if (value == null)
                                                                return;
                                                            if (!field.isAccessible())
                                                                field.setAccessible(true);
                                                            try {
                                                                field.set(obj, TypeUtils.cast(value, field.getType(), ParserConfig.getGlobalInstance()));
                                                            } catch (Throwable e) {
                                                                e.printStackTrace();
                                                                LoggerEx.error(TAG, "Set field " + field.getName() + " for config key " + key + " class " + field.getType() + " in class " + obj.getClass());
                                                            }
                                                        }
                                                    }
                                                });

                                                runtime.addFieldInjectionListener(new FieldInjectionListener<I18nBean>() {
                                                    public Class<I18nBean> annotationClass(){
                                                        return I18nBean.class;
                                                    }
                                                    @Override
                                                    public void inject(I18nBean annotation, Field field, Object obj) {
                                                        I18nHandler i18nHandler = baseRuntime.getI18nHandler();
                                                        if (!field.isAccessible())
                                                            field.setAccessible(true);
                                                        try {
                                                            field.set(obj, TypeUtils.cast(i18nHandler, field.getType(), ParserConfig.getGlobalInstance()));
                                                        } catch (Throwable e) {
                                                            e.printStackTrace();
                                                            LoggerEx.error(TAG, "Set field " + field.getName() + " for i18nhandler key "+ i18nHandler + " class " + field.getType() + " in class " + obj.getClass());
                                                        }
                                                    }
                                                });

                                            }

//										switch(serverType) {
//											case "gateway":
//												runtime = new GatewayGroovyRuntime();
//												break;
//											case "login":
//												runtime = new LoginGroovyRuntime();
//												break;
//											case "presence":
//												runtime = new PresenceGroovyRuntime();
//												break;
//										}
                                            break;
                                    }
                                    if (runtime != null) {
                                        localScriptPath = localPath + serverTypePath + service + "/" + language;
                                        runtime.setPath(localScriptPath + "/");
                                    }
                                }

                                if (runtime != null && needRedeploy) {
                                    File localZipFile = new File(localPath + serverTypePath + thePath);
                                    FileUtils.deleteQuietly(localZipFile);
                                    OutputStream zipOs = FileUtils.openOutputStream(localZipFile);
                                    fileAdapter.readFile(new PathEx(file.getAbsolutePath()), zipOs);
                                    IOUtils.closeQuietly(zipOs);

                                    String n = localZipFile.getName();
                                    n = n.substring(0, n.length() - ".zip".length());
                                    localScriptPath = localPath + serverTypePath + service + "/" + n;
                                    FileUtils.deleteDirectory(new File(localScriptPath));
                                    CRC32 crc = new CRC32();
                                    String str = "groovy.zip";
                                    crc.update(str.getBytes());
                                    long valuePwd = crc.getValue();
                                    unzip(localZipFile, localScriptPath, String.valueOf(valuePwd));

                                    Service theService = null;
                                    if (createRuntime) {
                                        String propertiesPath = localScriptPath + "/config.properties";
                                        Properties properties = new Properties();
                                        File propertiesFile = new File(propertiesPath);
                                        if (propertiesFile.exists() && propertiesFile.isFile()) {
                                            InputStream is = FileUtils.openInputStream(propertiesFile);
                                            properties.load(is);
                                            IOUtils.closeQuietly(is);
                                        }

                                        String minVersionStr = properties != null ? properties.getProperty("service.minversion") : null;
                                        Integer minVersion = null;
                                        if (minVersionStr != null) {
                                            try {
                                                minVersion = Integer.parseInt(minVersionStr);
                                            } catch (Exception e) {
                                            }
                                        }
                                        if (minVersion == null) {
                                            minVersion = 0;
                                        }
                                        Integer version = getServiceVersion(service);
                                        String serviceName = getServiceName(service);

                                        runtime.setServiceName(serviceName);
                                        runtime.setServiceVersion(version);

                                        try {
                                            if (serversService != null) {
                                                Document configDoc = serversService.getServerConfig(serviceName);
                                                if (configDoc != null) {
                                                    LoggerEx.info(TAG, "Read server " + serviceName + " config " + configDoc);
                                                    Set<String> keys = configDoc.keySet();
                                                    for (String key : keys) {
                                                        String theValue = configDoc.getString(key);
                                                        String value = properties.getProperty(key);
                                                        if (value == null) {
                                                            key = key.replaceAll("_", ".");
                                                            value = properties.getProperty(key);
                                                        }
                                                        if (value != null) {
                                                            properties.put(key, theValue);
                                                        }
                                                    }
                                                }
                                            } else {
                                                LoggerEx.info(TAG, "serversService is null, will not read config from database for service " + serviceName);
                                            }
                                        } catch (Throwable t) {
                                            LoggerEx.error(TAG, "Read server " + serviceName + " config failed, " + t.getMessage());
                                        }
                                        //触发serviceVersions
                                        runtime.prepare(service, properties, localScriptPath);

                                        theService = new Service();
                                        theService.setService(serviceName);
                                        theService.setMinVersion(minVersion);
                                        theService.setVersion(version);
                                        theService.setUploadTime(file.getLastModificationTime());
//										theService.setType(Service.FIELD_SERVER_TYPE_NORMAL);

                                        if (dockerStatusService != null) {
                                            //Aplomb delete service first before add, fixed the duplicated service bug.
                                            dockerStatusService.deleteService(OnlineServer.getInstance().getServer(), theService.getService(), theService.getVersion());
                                        }

                                        scriptRuntimeMap.put(service, runtime);
                                        List<BaseRuntime> versionList = serviceVersionMap.get(serviceName);
                                        //使用新的容器是因为防止删除的一瞬间， 获取为空的问题， 因此采用新容器更换的办法。
                                        List<BaseRuntime> newVersionList = null;
                                        if (versionList == null) {
                                            newVersionList = new ArrayList<>();
                                        } else {
                                            newVersionList = new ArrayList<>(versionList);
                                        }
//                                        if (newVersionList.contains(runtime)) {
//                                            newVersionList.remove(runtime);
//                                        }
                                        newVersionList.add(runtime);
                                        Collections.sort(newVersionList, new Comparator<BaseRuntime>() {
                                            @Override
                                            public int compare(BaseRuntime o1, BaseRuntime o2) {
                                                return o1.getServiceVersion().compareTo(o2.getServiceVersion());
                                            }
                                        });
                                        serviceVersionMap.put(serviceName, newVersionList);
                                    } else {
                                        Integer version = getServiceVersion(service);
                                        String serviceName = getServiceName(service);
                                        if (dockerStatusService != null)
                                            dockerStatusService.updateServiceUpdateTime(OnlineServer.getInstance().getServer(), serviceName, version, file.getLastModificationTime());
                                    }
                                    Integer version = getServiceVersion(service);
                                    String serviceName = getServiceName(service);
                                    try {
                                        runtime.setVersion(file.getLastModificationTime());
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
                                        LoggerEx.error(TAG, "Redeploy service " + service + " failed, " + t.getMessage());
                                        theService.setType(Service.FIELD_SERVER_TYPE_DEPLOY_FAILED);
//										if(dockerStatusService != null)
//											dockerStatusService.updateServiceType(OnlineServer.getInstance().getServer(), serviceName, version, Service.FIELD_SERVER_TYPE_DEPLOY_FAILED);
                                        throw t;
                                    } finally {
                                        if (dockerStatusService != null) {
                                            dockerStatusService.addService(OnlineServer.getInstance().getServer(), theService);
//											dockerStatusService.updateServiceType(OnlineServer.getInstance().getServer(), serviceName, version, Service.FIELD_SERVER_TYPE_NORMAL);
                                        }
//                                        String servicePathex = abPath.split("groovy.zip")[0];
//                                        String servicePath = servicePathex.split("/scripts")[1];
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

                                }
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Reload script zip file " + file.getAbsolutePath() + " failed, " + t.getMessage());
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
                                    List<BaseRuntime> runtimes = serviceVersionMap.get(serviceName);
                                    if (runtimes != null) {
                                        runtimes.remove(runtime);
                                    }
                                    if (dockerStatusService != null) {
                                        dockerStatusService.deleteService(OnlineServer.getInstance().getServer(), serviceName, version);
                                    }
                                } catch (CoreException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Delete service " + key + " from docker " + OnlineServer.getInstance().getServer() + " failed, " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "reload failed, " + e.getMessage());
        }
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
                    LoggerEx.info(TAG, "Service " + key + " remove failed, " + t.getMessage());
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

    public DockerStatusService getDockerStatusService() {
        return dockerStatusService;
    }

    public void setDockerStatusService(DockerStatusService dockerStatusService) {
        this.dockerStatusService = dockerStatusService;
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

    public FileAdapter getFileAdapter() {
        return fileAdapter;
    }

    public void setFileAdapter(FileAdapter fileAdapter) {
        this.fileAdapter = fileAdapter;
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
//    public RMIServerImplWrapper getRpcServer() {
//        return rpcServer;
//    }
//
//    public void setRpcServer(RMIServerImplWrapper rpcServer) {
//        this.rpcServer = rpcServer;
//    }
//
//    public RMIServerImplWrapper getRpcServerSsl() {
//        return rpcServerSsl;
//    }
//
//    public void setRpcServerSsl(RMIServerImplWrapper rpcServerSsl) {
//        this.rpcServerSsl = rpcServerSsl;
//    }
}
