package com.docker.rpc.remote.stub;

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
    private String fromService;
    private List<ServiceVersion> serviceVersions = null;
    Map<String, List<RemoteServers.Server>> theServers = null;
    List theServersList = new ArrayList();
    private static final String TAG = RemoteServersDiscovery.class.getSimpleName();

    public RemoteServersDiscovery(String host) {
        if (host.startsWith("http")) {
            this.host = host;
        } else {
            this.host = "http://" + host;
        }
        TimerEx.schedule(discoveryServiceVersions, 1, 3000);
    }

    private TimerTaskEx discoveryServiceVersions = new TimerTaskEx() {
        @Override
        public void execute() {
            ServiceVersionResult result = (ServiceVersionResult) ScriptHttpUtils.get(host + "/rest/discovery/serviceversion", ServiceVersionResult.class);
            if (result != null) {
                serviceVersions = result.getData();
                if (serviceVersions != null) {
                    for (ServiceVersion serviceVersion : serviceVersions) {
                        serviceVersion.setServerType(Arrays.asList(serviceVersion.getServerType().get(0).substring(1, serviceVersion.getServerType().get(0).length() - 1).split(",")));
                    }
                    this.cancel();
                    TimerEx.schedule(discoveryServersTask, 10, 10000);
                }
            }
        }
    };
    private TimerTaskEx discoveryServersTask = new TimerTaskEx() {
        @Override
        public void execute() {
            ServersResult result = (ServersResult) ScriptHttpUtils.post(JSON.toJSONString(serviceVersions), host + "/rest/discovery/serviceservers", ServersResult.class);
            if (result != null) {
                theServers = result.getData();
                if(theServers != null){
                    theServersList.add(theServers);
                    if (theServersList.size() > 2) {
                        theServersList.remove(0);
                    }
                }
            }
        }
    };

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
        String version = null;
        List serverTypes = null;
        String defaultVersion = null;
        for (ServiceVersion serviceVersion : serviceVersions) {
            if(grayReleased != null){
                if(type.equals(GrayReleased.defaultVersion)){
                    defaultVersion = serviceVersion.getServiceVersions().get(service);
                }
            }
            if (type.equals(serviceVersion.getType())) {
                if (serviceVersion.getServiceVersions() != null) {
                    version = serviceVersion.getServiceVersions().get(service);
                    serverTypes = serviceVersion.getServerType();
                }
            }
        }
        List<String> serviceServerList = new ArrayList<>();
        List<String> defaultServiceServerList = null;
        if (version != null && serverTypes != null && serverTypes.size() > 0) {
            for (int i = 0; i < serverTypes.size(); i++) {
                if (!serviceServerList.contains(service + "_" + version + "_" + serverTypes.get(i))) {
                    serviceServerList.add(service + "_" + version + "_" + serverTypes.get(i));
                }
            }
            //如果type里边的service没有指定版本,就去default里找默认版本
            if(grayReleased != null){
                defaultServiceServerList = new ArrayList<>();
                for (int i = 0; i < serverTypes.size(); i++) {
                    if (!defaultServiceServerList.contains(service + "_" + defaultVersion + "_" + serverTypes.get(i))) {
                        defaultServiceServerList.add(service + "_" + defaultVersion + "_" + serverTypes.get(i));
                    }
                }
            }
        }
        List<RemoteServers.Server> serverList = null;
        if(theServersList.size() > 0){
            Map theServersMap = (Map) theServersList.get(theServersList.size() - 1);
            if (theServersMap != null && theServersMap.size() > 0) {
                serverList = new ArrayList<>();
                //server重复性检查
                List<String> list = new ArrayList();
                for (String serviceVersion : serviceServerList) {
                    List<RemoteServers.Server> servers = theServers.get(serviceVersion);
                    if (servers != null) {
                        for (int i = 0; i < servers.size(); i++) {
                            if (!list.contains(servers.get(i).getServer())) {
                                list.add(servers.get(i).getServer());
                                serverList.add(servers.get(i));
                            }
                        }
                    }
                }
                if(serverList.isEmpty() && grayReleased != null){
                    for (String serviceVersion : defaultServiceServerList) {
                        List<RemoteServers.Server> servers = theServers.get(serviceVersion);
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
        }
        if (serverList != null && serverList.size() > 0) {
            Map map = new ConcurrentHashMap();
            for (int i = 0; i < serverList.size(); i++) {
                map.put(serverList.get(i).getServer(), serverList.get(i));
            }
            if (map.size() > 0) {
                return map;
            }
        }else {
            LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
        }
        return null;
    }
}
