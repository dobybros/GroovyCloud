package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.data.DockerStatus;
import com.docker.data.Service;
import com.docker.data.ServiceVersion;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.ServiceVersionServiceImpl;
import com.docker.utils.ScriptHttpUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.servlets.grayreleased.GrayReleased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/6/26.
 * Description：
 */
public class RemoteServersManager {
    private static volatile RemoteServersManager instance;
    private final String TAG = RemoteServersManager.class.getSimpleName();
    private List<String> crossRemoteHostList = new ArrayList<>();
    private Map<String, Map<String, Map<String, RemoteServers.Server>>> remoteServersMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> serviceMaxVersionMap = new ConcurrentHashMap<>();
    private Map<String, String> remoteServersTokenMap = new ConcurrentHashMap<>();
    private Map<String, TimerTaskEx> timerTaskExMap = new ConcurrentHashMap<>();
    private ServiceVersionServiceImpl serviceVersionService;
    private DockerStatusServiceImpl dockerStatusService;

    public RemoteServersManager(ServiceVersionServiceImpl serviceVersionService, DockerStatusServiceImpl dockerStatusService) {
        this.serviceVersionService = serviceVersionService;
        this.dockerStatusService = dockerStatusService;
    }

    public RemoteServersManager() {
    }

    public void init() {
        TimerTaskEx timerTaskEx = new TimerTaskEx("RefreshRemoteServersByHost") {
            @Override
            public void execute() {
                remoteServersMap = getServiceServers();
                serviceMaxVersionMap = getServiceMaxVersion();
            }
        };
        timerTaskEx.execute();
        TimerEx.schedule(timerTaskEx, 10000L, 20000L);
    }

    public void addCrossHost(String host) {
        if (!crossRemoteHostList.contains(host)) {
            crossRemoteHostList.add(host);
            //2小时刷新一次另一个集群的验证token
            TimerTaskEx taskEx = new TimerTaskEx("RefreshRemoteTokenWhenCross") {
                @Override
                public void execute() {
                    Map header = new HashMap();
                    header.put("key", "FSDdfFDWfR324fs98DSF*@#");
                    Result remoteTokenResult = (Result) ScriptHttpUtils.post(null, host + "/base/crossClusterCreateToken", header, Result.class);
                    if (remoteTokenResult != null && remoteTokenResult.success()) {
                        String jwtToken = (String) remoteTokenResult.getData();
                        if (jwtToken != null) {
                            remoteServersTokenMap.put(host, jwtToken);
                        }
                    } else {
                        LoggerEx.error(TAG, "Get crossClusterCreateToken errMsg: " + (remoteTokenResult == null ? "null" : remoteTokenResult.toString()));
                        remoteServersTokenMap.remove(host);
                        this.cancel();
                        TimerEx.schedule(new TimerTaskEx("RefreshRemoteTokenWhenCrossFailedRetry") {
                            @Override
                            public void execute() {
                                Map header = new HashMap();
                                header.put("key", "FSDdfFDWfR324fs98DSF*@#");
                                Result remoteTokenResult = (Result) ScriptHttpUtils.post(null, host + "/base/crossClusterCreateToken", header, Result.class);
                                if (remoteTokenResult != null && remoteTokenResult.success()) {
                                    String jwtToken = (String) remoteTokenResult.getData();
                                    if (jwtToken != null) {
                                        remoteServersTokenMap.put(host, jwtToken);
                                        this.cancel();
                                        if (timerTaskExMap.get(host) != null) {
                                            TimerEx.schedule(timerTaskExMap.get(host), 60000L, 7200000L);
                                            LoggerEx.info(TAG, "RemoteServer host has reset to available, host: " + host);
                                        }
                                    }
                                } else {
                                    LoggerEx.error(TAG, "Get retry crossClusterCreateToken errMsg: " + (remoteTokenResult == null ? "null" : remoteTokenResult.toString()));
                                }
                            }
                        }, 60000L, 60000L);
                    }
                }
            };
            timerTaskExMap.put(host, taskEx);
            taskEx.execute();
            TimerEx.schedule(taskEx, 60000L, 7200000L);
        }
    }

    //用于跨集群刷新otken
    public static class RemoteTokenResult extends Result<String> {

    }

    public Map<String, Map<String, String>> getServiceMaxVersionMap() {
        return serviceMaxVersionMap;
    }

    public Map<String, RemoteServers.Server> getServers(String service) {
        try {
            GrayReleased grayReleased = GrayReleased.grayReleasedThreadLocal.get();
            String type = GrayReleased.defaultVersion;

            if (grayReleased != null) {
                if (grayReleased.getType() != null) {
                    type = grayReleased.getType();
                }
            }
            Map<String, Map<String, Map<String, RemoteServers.Server>>> theServersFinalMap = remoteServersMap;
            if (service != null && theServersFinalMap != null && theServersFinalMap.size() > 0) {
                Map<String, Map<String, RemoteServers.Server>> typeMap = theServersFinalMap.get(type);
                if ((typeMap == null || typeMap.size() == 0) && !type.equals(GrayReleased.defaultVersion)) {
                    typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                    LoggerEx.warn(TAG, "Service version cant find type: " + type + ", Now, find in default!!!");
                }
                if (typeMap != null && typeMap.size() > 0) {
                    Map<String, RemoteServers.Server> servers = typeMap.get(service);
                    //如果type不为default，查出来的servcers为空，那么就去default里找
                    if (servers == null || servers.isEmpty()) {
                        if (!type.equals(GrayReleased.defaultVersion)) {
                            typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                            if (typeMap != null && typeMap.size() > 0) {
                                servers = typeMap.get(service);
                            }
                        }
                    }
                    if (servers != null && !servers.isEmpty()) {
                        return servers;
                    } else {
                        LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
                    }
                } else {
                    LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
                }
            } else {
                LoggerEx.error(TAG, "theServersFinalMap is empty, please check");
            }
            return null;
        } finally {
            GrayReleased.grayReleasedThreadLocal.remove();
        }
    }

    public String getRemoteServerToken(String host) {
        String token = remoteServersTokenMap.get(host);
        return token;
    }

    private Map<String, Map<String, Map<String, RemoteServers.Server>>> getServiceServers() {
        try {
            Map<String, Map<String, Map<String, String>>> serviceVersionsMap = getServiceVersion();
            if (serviceVersionsMap != null && serviceVersionsMap.size() > 0) {
                Map<String, Map<String, Map<String, RemoteServers.Server>>> serviceServersReallyMap = new ConcurrentHashMap<>();
                for (String type : serviceVersionsMap.keySet()) {
                    if (type != null) {
                        Map<String, Map<String, RemoteServers.Server>> typeServerMap = serviceServersReallyMap.get(type);
                        if (typeServerMap == null) {
                            typeServerMap = new ConcurrentHashMap<>();
                            serviceServersReallyMap.put(type, typeServerMap);
                        }
                        Map<String, Map<String, String>> typeMap = serviceVersionsMap.get(type);
                        if (typeMap != null && typeMap.size() > 0) {
                            for (String serverType : typeMap.keySet()) {
                                Map<String, String> serverTypeMap = typeMap.get(serverType);
                                List<DockerStatus> dockers = dockerStatusService.getAllDockerStatus();
                                for (String serverTypeService : serverTypeMap.keySet()) {
                                    if (dockers != null) {
                                        for (DockerStatus dockerStatus : dockers) {
                                            if (dockerStatus.getStatus() == DockerStatus.STATUS_OK || dockerStatus.getStatus() == DockerStatus.STATUS_PAUSE) {
                                                if (dockerStatus.getServerType().equals(serverType)) {
                                                    List<Service> services = dockerStatus.getServices();
                                                    RemoteServers.Server server = null;
                                                    if (services != null) {
                                                        for (Service service : services) {
                                                            if (service.getService().equals(serverTypeService)) {
                                                                Map<String, RemoteServers.Server> servers = typeServerMap.computeIfAbsent(service.getService(), k -> new ConcurrentHashMap<>());
                                                                server = new RemoteServers.Server();
                                                                if (serverTypeMap.get(serverTypeService).equals("-1") || (service.getService().equals(serverTypeService) && service.getVersion().toString().equals(serverTypeMap.get(serverTypeService)))) {
                                                                    boolean canAddService = false;
                                                                    if (servers.size() > 0) {
                                                                        for (String serverStr : servers.keySet()) {
                                                                            RemoteServers.Server serverOld = servers.get(serverStr);
                                                                            if (service.getVersion() > serverOld.getVersion()) {
                                                                                servers.clear();
                                                                                canAddService = true;
                                                                            } else if (service.getVersion() == serverOld.getVersion()) {
                                                                                canAddService = true;
                                                                            }
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        canAddService = true;
                                                                    }
                                                                    if (canAddService) {
                                                                        server.setIp(dockerStatus.getIp());
                                                                        server.setLanId(dockerStatus.getLanId());
                                                                        server.setRpcPort(dockerStatus.getRpcPort());
                                                                        server.setServer(dockerStatus.getServer());
                                                                        server.setSslRpcPort(dockerStatus.getSslRpcPort());
                                                                        server.setVersion(service.getVersion());
                                                                        server.setPublicDomain(dockerStatus.getPublicDomain());
                                                                        servers.put(dockerStatus.getServer(), server);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return serviceServersReallyMap;
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "Servers registry error, errMsg: " + ExceptionUtils.getFullStackTrace(t));
        }
        return null;
    }

    private Map<String, Map<String, Map<String, String>>> getServiceVersion() throws CoreException {
        List<ServiceVersion> serviceVersions = serviceVersionService.getServiceVersionsAll();
        if (serviceVersions != null && serviceVersions.size() > 0) {
            Map<String, Map<String, Map<String, String>>> serviceVersionReallyMap = new ConcurrentHashMap<>();
            for (ServiceVersion serviceVersion : serviceVersions) {
                String type = serviceVersion.getType();
                if (!StringUtils.isEmpty(type)) {
                    //type层
                    Map<String, Map<String, String>> typeMap = serviceVersionReallyMap.get(type);
                    if (typeMap == null) {
                        typeMap = new ConcurrentHashMap<>();
                        serviceVersionReallyMap.put(type, typeMap);
                    }
                    List<String> serverTypes = serviceVersion.getServerType();
                    if (serverTypes != null) {
                        for (String serverType : serverTypes) {
                            //serverType层
                            Map<String, String> serverTypeMap = typeMap.get(serverType);
                            if (serverTypeMap == null) {
                                serverTypeMap = new ConcurrentHashMap<>();
                                typeMap.put(serverType, serverTypeMap);
                            }
                            Map<String, String> serviceVersionsMap = serviceVersion.getServiceVersions();
                            if (serviceVersionsMap != null) {
                                for (String service : serviceVersionsMap.keySet()) {
                                    //service version层
                                    //使用最大版本
                                    if (StringUtils.isEmpty(serverTypeMap.get(service))) {
                                        if (StringUtils.isEmpty(serviceVersionsMap.get(service))) {
                                            serverTypeMap.put(service, "-1");
                                        } else {
                                            serverTypeMap.put(service, serviceVersionsMap.get(service));
                                        }
                                    } else {
                                        if (Integer.parseInt(serviceVersionsMap.get(service)) > Integer.parseInt(serverTypeMap.get(service))) {
                                            serverTypeMap.put(service, serviceVersionsMap.get(service));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return serviceVersionReallyMap;
        }
        return null;
    }

    private Map<String, Map<String, String>> getServiceMaxVersion() {
        try {
            Map<String, Map<String, Map<String, String>>> serviceVersionsMap = getServiceVersion();
            if (serviceVersionsMap != null && !serviceVersionsMap.isEmpty()) {
                Map<String, Map<String, String>> serviceMaxVersionMap = new ConcurrentHashMap<>();
                for (String type : serviceVersionsMap.keySet()) {
                    Map<String, Map<String, String>> serverTypeMap = serviceVersionsMap.get(type);
                    if (serverTypeMap != null && !serverTypeMap.isEmpty()) {
                        Map<String, String> newServiceMap = serviceMaxVersionMap.get(type);
                        if (newServiceMap == null) {
                            newServiceMap = new ConcurrentHashMap<>();
                            serviceMaxVersionMap.put(type, newServiceMap);
                        }
                        for (Map<String, String> serviceMap : serverTypeMap.values()) {
                            for (String service : serviceMap.keySet()) {
                                String curVersion = newServiceMap.get(service);
                                if (curVersion == null || Integer.parseInt(serviceMap.get(service)) > Integer.parseInt(curVersion)) {
                                    newServiceMap.put(service, serviceMap.get(service));
                                }
                            }
                        }
                    }
                }
                if (!serviceMaxVersionMap.isEmpty()) {
                    return serviceMaxVersionMap;
                }
            }
        } catch (CoreException e) {
            LoggerEx.error(TAG, "Get service max version err, errMsg: " + ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    public String getServiceMaxVersion(String type, String service) {
        Map<String, String> serviceMap = serviceMaxVersionMap.get(type);
        if (serviceMap != null) {
            return serviceMap.get(service);
        }
        return null;
    }

    private void setServiceVersionService(ServiceVersionServiceImpl serviceVersionService) {
        this.serviceVersionService = serviceVersionService;
    }

    private void setDockerStatusService(DockerStatusServiceImpl dockerStatusService) {
        this.dockerStatusService = dockerStatusService;
    }

    public static RemoteServersManager getRemoteServersManager() {
        return instance;
    }

    public synchronized static RemoteServersManager getInstance() {
        if (instance == null) {
            instance = new RemoteServersManager();

        }
        return instance;
    }

    public synchronized static RemoteServersManager getInstance(ServiceVersionServiceImpl serviceVersionService, DockerStatusServiceImpl dockerStatusService) {
        if (instance == null) {
            synchronized (RemoteServersManager.class) {
                if (instance == null) {
                    instance = new RemoteServersManager(serviceVersionService, dockerStatusService);
                }
            }
        } else {
            instance.setServiceVersionService(serviceVersionService);
            instance.setDockerStatusService(dockerStatusService);
        }
        return instance;
    }
}
