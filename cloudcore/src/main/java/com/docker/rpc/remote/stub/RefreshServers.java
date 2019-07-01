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
    private Map<String, Map<String, Map<String, List<RemoteServers.Server>>>> remoteServersMap = new ConcurrentHashMap();
    public void addRemoteHost(final String host){
        if(!remoteHostList.contains(host)){
            remoteHostList.add(host);
            TimerEx.schedule(new TimerTaskEx() {
                @Override
                public void execute() {
                    ServersResult result = (ServersResult) ScriptHttpUtils.get( host + "/rest/discovery/serviceservers", ServersResult.class);
                    if (result != null) {
                        Map<String, Map<String, List<RemoteServers.Server>>> theServers = result.getData();
                        if (theServers != null) {
                            remoteServersMap.put(host, theServers);
                        }
                    }
                }
            }, null, 10000L);        }
    }
    public synchronized static RefreshServers getInstance() {
        if(instance == null){
            instance = new RefreshServers();
        }
        return instance;
    }
    public static class ServersResult extends Result<Map<String, Map<String, List<RemoteServers.Server>>>> {

    }

    Map<String, Map<String, List<RemoteServers.Server>>> getFinalServersMap(String host){
        if(remoteServersMap.size() > 0){
            return remoteServersMap.get(host);
        }
        return null;
    }
}
