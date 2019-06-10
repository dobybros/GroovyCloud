package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.alibaba.fastjson.JSON;
import com.docker.data.Lan;
import com.docker.data.ServiceVersion;
import com.docker.storage.adapters.LansService;
import com.docker.utils.ScriptHttpUtils;
import com.docker.utils.SpringContextUtil;
import script.groovy.servlets.grayreleased.GrayReleased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServersDiscovery {
    private String host;
    private List<ServiceVersion> serviceVersions = new ArrayList<>();
    Map<String, List<RemoteServers.Server>> theServersFinalMap = new ConcurrentHashMap();
    private static final String TAG = RemoteServersDiscovery.class.getSimpleName();

    public RemoteServersDiscovery(String host) {
        if (host.startsWith("http")) {
            this.host = host;
        } else {
            this.host = "http://" + host;
        }
        TimerEx.schedule(discoveryServersTask, null, 10000L);
    }

    public static class ServersResult extends Result<Map<String, List<RemoteServers.Server>>> {

    }

    public static class ServiceVersionResult extends Result<List<ServiceVersion>> {

    }

    public List<ServiceVersion> getServiceVersions() {
        return serviceVersions;
    }

    public void setServiceVersions(List<ServiceVersion> serviceVersions) {
        this.serviceVersions = serviceVersions;
    }

    public Map<String, RemoteServers.Server> getServers(String service) {
        GrayReleased grayReleased = GrayReleased.grayReleasedThreadLocal.get();
        String type = GrayReleased.defaultVersion;
        if (grayReleased != null) {
            if (grayReleased.getType() != null) {
                type = grayReleased.getType();
            }
        }
        Integer version = null;
        List finalServerTypes = new ArrayList();
        Integer defaultVersion = null;
        if(serviceVersions.isEmpty()){
            getCommonServiceversions();
        }
        if (!serviceVersions.isEmpty()) {
            for (ServiceVersion serviceVersion : serviceVersions) {
                //如果type里边的service没有指定版本,就去default里找默认版本
                if (!type.equals(GrayReleased.defaultVersion)) {
                    if (serviceVersion.getType().equals(GrayReleased.defaultVersion)) {
                        if (defaultVersion == null) {
                            defaultVersion = Integer.valueOf(serviceVersion.getServiceVersions().get(service));
                        } else {
                            if (Integer.valueOf(serviceVersion.getServiceVersions().get(service)) > defaultVersion) {
                                defaultVersion = Integer.valueOf(serviceVersion.getServiceVersions().get(service));
                            }
                        }
                    }
                }
                if (type.equals(serviceVersion.getType())) {
                    if (serviceVersion.getServiceVersions() != null) {
                        if (serviceVersion.getServiceVersions().get(service) != null) {
                            //如果不同的serverType中有相同的service，但是service版本不一样，就选择版本更大的
                            if (version == null) {
                                version = Integer.valueOf(serviceVersion.getServiceVersions().get(service));
                            } else {
                                if (Integer.valueOf(serviceVersion.getServiceVersions().get(service)) > version) {
                                    version = Integer.valueOf(serviceVersion.getServiceVersions().get(service));
                                }
                            }
                        }
                        List serverTypes = serviceVersion.getServerType();
                        if (serverTypes != null) {
                            for (Object serverType : serverTypes) {
                                if (!finalServerTypes.contains(serverType)) {
                                    finalServerTypes.add(serverType);
                                }
                            }
                        }
                    }
                }
            }
        }
        List<String> serviceServerList = new ArrayList<>();
        List<String> defaultServiceServerList = null;
        if (finalServerTypes != null && finalServerTypes.size() > 0) {
            if (version != null) {
                for (int i = 0; i < finalServerTypes.size(); i++) {
                    if (!serviceServerList.contains(service + "_" + version.toString() + "_" + finalServerTypes.get(i))) {
                        serviceServerList.add(service + "_" + version.toString() + "_" + finalServerTypes.get(i));
                    }
                }
            } else {
                //如果type里边的service没有指定版本,就去default里找默认版本
                if (!type.equals(GrayReleased.defaultVersion)) {
                    defaultServiceServerList = new ArrayList<>();
                    if (finalServerTypes != null) {
                        for (int i = 0; i < finalServerTypes.size(); i++) {
                            if (!defaultServiceServerList.contains(service + "_" + defaultVersion.toString() + "_" + finalServerTypes.get(i))) {
                                defaultServiceServerList.add(service + "_" + defaultVersion.toString() + "_" + finalServerTypes.get(i));
                            }
                        }
                    }
                }
            }
        }
        List<RemoteServers.Server> serverList = null;
        if(theServersFinalMap.isEmpty()){
            getTheServersFinalMap();
        }
        if (theServersFinalMap.size() > 0) {
            serverList = new ArrayList<>();
            //server重复性检查
            List<String> list = new ArrayList();
            for (String serviceVersion : serviceServerList) {
                List<RemoteServers.Server> servers = theServersFinalMap.get(serviceVersion);
                if (servers != null) {
                    for (int i = 0; i < servers.size(); i++) {
                        if (!list.contains(servers.get(i).getServer())) {
                            list.add(servers.get(i).getServer());
                            serverList.add(servers.get(i));
                        }
                    }
                }
            }
            if (serverList.isEmpty() && !type.equals(GrayReleased.defaultVersion)) {
                for (String serviceVersion : defaultServiceServerList) {
                    List<RemoteServers.Server> servers = theServersFinalMap.get(serviceVersion);
                    if (servers != null) {
                        for (int i = 0; i < servers.size(); i++) {
                            if (!list.contains(servers.get(i).getServer())) {
                                list.add(servers.get(i).getServer());
                                serverList.add(servers.get(i));
                            }
                        }
                    }
                }
            }
        }
        if (serverList != null && serverList.size() > 0) {
            Map map = new ConcurrentHashMap();
            for (int i = 0; i < serverList.size(); i++) {
                map.put(serverList.get(i).getServer(), serverList.get(i));
            }
            if (map.size() > 0) {
                return map;
            }
        } else {
            LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
        }
        return null;
    }

    private void getCommonServiceversions() {
        ServiceVersionResult result = (ServiceVersionResult) ScriptHttpUtils.get(host + "/rest/discovery/serviceversion", ServiceVersionResult.class);
        if (result != null) {
            List<ServiceVersion> theServiceVersions = result.getData();
            if (!theServiceVersions.isEmpty()) {
                serviceVersions = theServiceVersions;
                getTheServersFinalMap();
            }
        }
    }

    private TimerTaskEx discoveryServersTask = new TimerTaskEx() {
        @Override
        public void execute() {
            getCommonServiceversions();
        }
    };

    private void getTheServersFinalMap() {
        ServersResult result = (ServersResult) ScriptHttpUtils.post(JSON.toJSONString(serviceVersions), host + "/rest/discovery/serviceservers", ServersResult.class);
        if (result != null) {
            Map<String, List<RemoteServers.Server>> theServers = result.getData();
            if (theServers != null) {
                theServersFinalMap = theServers;
            }
        }
    }
}
