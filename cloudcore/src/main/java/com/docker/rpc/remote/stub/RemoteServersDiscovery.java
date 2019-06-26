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
    Map<String, Map<String, List<RemoteServers.Server>>> theServersFinalMap = new ConcurrentHashMap();
    private static final String TAG = RemoteServersDiscovery.class.getSimpleName();

    public RemoteServersDiscovery(String host) {
        if (host.startsWith("http")) {
            this.host = host;
        } else {
            this.host = "http://" + host;
        }
        TimerEx.schedule(discoveryServersTask, null, 10000L);
    }

    public static class ServersResult extends Result<Map<String, Map<String, List<RemoteServers.Server>>>> {

    }

    public static class ServiceVersionResult extends Result<List<ServiceVersion>> {

    }

    public Map<String, RemoteServers.Server> getServers(String service) {
        GrayReleased grayReleased = GrayReleased.grayReleasedThreadLocal.get();
        String type = GrayReleased.defaultVersion;
        if (grayReleased != null) {
            if (grayReleased.getType() != null) {
                type = grayReleased.getType();
            }
        }
        if (service != null && theServersFinalMap.size() > 0) {
            Map<String, List<RemoteServers.Server>> typeMap = theServersFinalMap.get(type);
            if ((typeMap == null || typeMap.size() == 0) && !type.equals(GrayReleased.defaultVersion)) {
                typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                LoggerEx.warn(TAG, "Service version cant find type: " + type + ", Now, find in default!!!");
            }
            if (typeMap != null && typeMap.size() > 0) {
                List<RemoteServers.Server> servers = (List<RemoteServers.Server>) typeMap.get(service);
                //如果type不为default，查出来的servcers为空，那么就去default里找
                if (servers == null || servers.isEmpty()) {
                    if (!type.equals(GrayReleased.defaultVersion)) {
                        typeMap = theServersFinalMap.get(GrayReleased.defaultVersion);
                        if (typeMap != null && typeMap.size() > 0) {
                            servers = (List<RemoteServers.Server>) typeMap.get(service);
                        }
                    }
                }
                if (servers != null && servers.size() > 0) {
                    Map map = new ConcurrentHashMap();
                    for (int i = 0; i < servers.size(); i++) {
                        map.put(servers.get(i).getServer(), servers.get(i));
                    }
                    if (map.size() > 0) {
                        return map;
                    }
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
    }

    private TimerTaskEx discoveryServersTask = new TimerTaskEx() {
        @Override
        public void execute() {
            getTheServersFinalMap();
        }
    };

    private void getTheServersFinalMap() {
        ServersResult result = (ServersResult) ScriptHttpUtils.get(host + "/rest/discovery/serviceservers", ServersResult.class);
        if (result != null) {
            Map<String, Map<String, List<RemoteServers.Server>>> theServers = result.getData();
            if (theServers != null) {
                theServersFinalMap = theServers;
            }
        }
    }
}
