package com.docker.rpc.remote.stub;

import chat.logs.LoggerEx;
import script.groovy.servlets.grayreleased.GrayReleased;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServersDiscovery {
    private String host;
    private static final String TAG = RemoteServersDiscovery.class.getSimpleName();

    public RemoteServersDiscovery(String host) {
       this.host = host;
    }

    public Map<String, RemoteServers.Server> getServers(String service) {
        GrayReleased grayReleased = GrayReleased.grayReleasedThreadLocal.get();
        String type = GrayReleased.defaultVersion;
        if (grayReleased != null) {
            if (grayReleased.getType() != null) {
                type = grayReleased.getType();
            }
        }
        Map<String, Map<String, Map<String, RemoteServers.Server>>> theServersFinalMap = RefreshServers.getInstance().getFinalServersMap(this.host);
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
}
