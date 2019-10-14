package com.docker.rpc.remote.stub;

import chat.json.Result;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.utils.ScriptHttpUtils;
import script.groovy.servlets.grayreleased.GrayReleased;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/6/26.
 * Description：
 */
public class RemoteServersManager {
    private static RemoteServersManager instance;
    private final String TAG = RemoteServersManager.class.getSimpleName();
    private List<String> remoteHostList = new ArrayList<String>();
    private List<String> crossRemoteHostList = new ArrayList<>();
    private Map<String, Map<String, Map<String, Map<String, RemoteServers.Server>>>> remoteServersMap = new ConcurrentHashMap<String, Map<String, Map<String, Map<String, RemoteServers.Server>>>>();
    //跨集群使用
    private Map<String, String> remoteServersTokenMap = new ConcurrentHashMap<>();
    public void addRemoteHost(final String host) {
        if (!remoteHostList.contains(host)) {
            remoteHostList.add(host);
            TimerTaskEx timerTaskEx = new TimerTaskEx() {
                @Override
                public void execute() {
                    ServersResult result = (ServersResult) ScriptHttpUtils.get(host + "/rest/discovery/serviceservers", ServersResult.class);
                    if (result != null) {
                        Map<String, Map<String, List<RemoteServers.Server>>> theServers = result.getData();
                        if (theServers != null) {
                            Map<String, Map<String, Map<String, RemoteServers.Server>>> grayTypeServersMap = null;
                            for (String grayType : theServers.keySet()) {
                                grayTypeServersMap = new ConcurrentHashMap<String, Map<String, Map<String, RemoteServers.Server>>>();
                                Map<String, List<RemoteServers.Server>> grayTypeMap = theServers.get(grayType);
                                if (grayTypeMap != null && !grayTypeMap.isEmpty()) {
                                    Map<String, Map<String, RemoteServers.Server>> serversMap = new ConcurrentHashMap<String, Map<String, RemoteServers.Server>>();
                                    for (String service : theServers.get(grayType).keySet()) {
                                        List<RemoteServers.Server> servers = (List<RemoteServers.Server>) grayTypeMap.get(service);
                                        if (servers != null && !servers.isEmpty()) {
                                            Map<String, RemoteServers.Server> serverMap = new ConcurrentHashMap<String, RemoteServers.Server>();
                                            for (int i = 0; i < servers.size(); i++) {
                                                serverMap.put(servers.get(i).getServer(), servers.get(i));
                                            }
                                            serversMap.put(service, serverMap);
                                        }
                                    }
                                    grayTypeServersMap.put(grayType, serversMap);
                                }
                            }
                            if (grayTypeServersMap != null && !grayTypeServersMap.isEmpty()) {
                                remoteServersMap.put(host, grayTypeServersMap);
                            }
                        }
                    }
                }
            };
            timerTaskEx.execute();
            TimerEx.schedule(timerTaskEx, 10000L, 10000L);
        }
    }
    public void addCrossHost(String host){
        if(!crossRemoteHostList.contains(host)){
            crossRemoteHostList.add(host);
            //2小时刷新一次另一个集群的验证token
            TimerTaskEx taskEx = new TimerTaskEx() {
                @Override
                public void execute() {
                    RemoteTokenResult remoteTokenResult = (RemoteTokenResult)ScriptHttpUtils.get(host + "/rest/discovery/accessToken", RemoteTokenResult.class);
                    if(remoteTokenResult != null){
                        String jwtToken = remoteTokenResult.getData();
                        if(jwtToken != null){
                            remoteServersTokenMap.put(host, jwtToken);
                        }
                    }else {
                        remoteServersTokenMap.remove(host);
                        TimerEx.schedule(new TimerTaskEx() {
                            @Override
                            public void execute() {
                                RemoteTokenResult remoteTokenResult = (RemoteTokenResult)ScriptHttpUtils.get(host + "/rest/discovery/accessToken", RemoteTokenResult.class);
                                if(remoteTokenResult != null){
                                    String jwtToken = remoteTokenResult.getData();
                                    if(jwtToken != null){
                                        remoteServersTokenMap.put(host, jwtToken);
                                        this.cancel();
                                        LoggerEx.info(TAG, "RemoteServer host has reset to available, host: " + host);
                                    }
                                }
                            }
                        }, 60000L, 60000L);
                    }
                }
            };
            taskEx.execute();
            TimerEx.schedule(taskEx, 60000L, 7200000L);
        }
    }
    public static class ServersResult extends Result<Map<String, Map<String, List<RemoteServers.Server>>>> {

    }
    //用于跨集群刷新otken
    public static class RemoteTokenResult extends Result<String> {

    }
    private Map<String, Map<String, Map<String, RemoteServers.Server>>> getFinalServersMap(String host) {
        if (remoteServersMap.size() > 0) {
            return remoteServersMap.get(host);
        }
        return null;
    }

    public Map<String, RemoteServers.Server> getServers(String service, String host) {
        GrayReleased grayReleased = GrayReleased.grayReleasedThreadLocal.get();
        String type = GrayReleased.defaultVersion;

        if (grayReleased != null) {
            if (grayReleased.getType() != null) {
                type = grayReleased.getType();
            }
        }
        Map<String, Map<String, Map<String, RemoteServers.Server>>> theServersFinalMap = getFinalServersMap(host);
        if (service != null && theServersFinalMap != null && theServersFinalMap.size() > 0) {
            Map<String, Map<String, RemoteServers.Server>> typeMap = theServersFinalMap.get(type);
            if ((typeMap == null || typeMap.size() == 0) && !type.equals(GrayReleased.defaultVersion)) {
                typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                LoggerEx.warn(TAG, "Service version cant find type: " + type + ", Now, find in default!!!");
            }
            if (typeMap != null && typeMap.size() > 0) {
                Map<String, RemoteServers.Server> servers = (Map<String, RemoteServers.Server>) typeMap.get(service);
                //如果type不为default，查出来的servcers为空，那么就去default里找
                if (servers == null || servers.isEmpty()) {
                    if (!type.equals(GrayReleased.defaultVersion)) {
                        typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                        if (typeMap != null && typeMap.size() > 0) {
                            servers = (Map<String, RemoteServers.Server>) typeMap.get(service);
                        }
                    }
                }
                if(servers != null && !servers.isEmpty()){
                    return servers;
                }else {
                    LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
                }
            } else {
                LoggerEx.error(TAG, "The service: " + service + " has no server,cant invoke!");
            }
        } else {
            LoggerEx.error(TAG, "theServersFinalMap is empty, please check");
        }
        return null;
    }
    public String getRemoteServerToken(String host){
        String token = remoteServersTokenMap.get(host);
        return token;
    }
    public synchronized static RemoteServersManager getInstance() {
        if(instance == null){
            instance = new RemoteServersManager();
        }
        return instance;
    }
}
