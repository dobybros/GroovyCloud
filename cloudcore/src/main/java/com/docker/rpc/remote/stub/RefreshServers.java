package com.docker.rpc.remote.stub;

import chat.json.Result;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.utils.ScriptHttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/6/26.
 * Description：
 */
public class RefreshServers {
    public static RefreshServers instance;
    private List<String> remoteHostList = new ArrayList();
    private Map<String, Map<String, Map<String, Map<String, RemoteServers.Server>>>> remoteServersMap = new ConcurrentHashMap<String, Map<String, Map<String, Map<String, RemoteServers.Server>>>>();

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

    public synchronized static RefreshServers getInstance() {
        if (instance == null) {
            instance = new RefreshServers();
        }
        return instance;
    }

    public static class ServersResult extends Result<Map<String, Map<String, List<RemoteServers.Server>>>> {

    }

    Map<String, Map<String, Map<String, RemoteServers.Server>>> getFinalServersMap(String host) {
        if (remoteServersMap.size() > 0) {
            return remoteServersMap.get(host);
        }
        return null;
    }
}
