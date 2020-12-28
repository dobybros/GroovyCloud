package com.dobybros.chat.script.annotations.gateway;

import chat.json.Result;
import chat.logs.LoggerEx;
import chat.utils.PropertiesContainer;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.alibaba.fastjson.JSON;
import com.docker.server.OnlineServer;
import com.docker.storage.kafka.BaseKafkaConfCenter;
import com.docker.storage.kafka.KafkaProducerHandler;
import com.docker.utils.ScriptHttpUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2020/6/20.
 * Description：
 */
public class DataSessionListener {
    private Map<String, TimerTaskEx> serviceTimerMap = new ConcurrentHashMap<>();
    private KafkaProducerHandler kafkaProducerHandler = null;
    //store data to monitor by timer
    void restoreData(String userId, String service) {
        cancelStoreDataTimer(userId, service);
        if(kafkaProducerHandler == null){
            kafkaProducerHandler = new KafkaProducerHandler(BaseKafkaConfCenter.getInstance().getKafkaConfCenter());
            kafkaProducerHandler.connect();
        }
        TimerTaskEx storeDataTimer = new TimerTaskEx() {
            @Override
            public void execute() {
                Object data = getRoomData(userId, service);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("roomIdService", getRoomIdService(userId, service));
                dataMap.put("server", OnlineServer.getInstance().getServer());
                dataMap.put("address", "http://" + OnlineServer.getInstance().getIp() + ":" + OnlineServer.getInstance().getHttpPort());
                dataMap.put("idc", OnlineServer.getInstance().getLanId());
                dataMap.put("data", data);
                kafkaProducerHandler.send("GatewayMemoryBackUp", JSON.toJSONString(dataMap).getBytes(StandardCharsets.UTF_8));
            }
        };
        TimerEx.schedule(storeDataTimer, 30000L, 5000L);
        serviceTimerMap.put(userId + service, storeDataTimer);
    }

    private void cancelStoreDataTimer(String userId, String service) {
        TimerTaskEx storeDataTimer = serviceTimerMap.remove(userId + service);
        if (storeDataTimer != null) {
            storeDataTimer.cancel();
        }
    }

    Object getRoomDataFromMonitor(String roomId, String service) {
        Result result = ScriptHttpUtils.post(JSON.toJSONString(getMonitorParams(roomId, service)), PropertiesContainer.getInstance().getProperty("gateway.monitor.url") + "/data", getMonitorHeaders(), Result.class);
        if (result != null && result.success()) {
            return result.getData();
        }
        return null;
    }

    void removeMonitorRoomData(String roomId, String service, int close) {
        cancelStoreDataTimer(roomId, service);
        if(kafkaProducerHandler != null){
            kafkaProducerHandler.disconnect();
            kafkaProducerHandler = null;
        }
        Map params = getMonitorParams(roomId, service);
        params.put("close", close);
        Result result = ScriptHttpUtils.post(JSON.toJSONString(params), PropertiesContainer.getInstance().getProperty("gateway.monitor.url") + "/cleardata", getMonitorHeaders(), Result.class);
        if (result == null || !result.success()) {
            TimerEx.schedule(new TimerTaskEx() {
                int tryTimes = 0;
                @Override
                public void execute() {
                    Result theResult = ScriptHttpUtils.post(JSON.toJSONString(params), PropertiesContainer.getInstance().getProperty("gateway.monitor.url") + "/cleardata", getMonitorHeaders(), Result.class);
                    if(theResult != null && theResult.success()){
                        this.cancel();
                    }
                    if(tryTimes == 3){
                        LoggerEx.fatal(TAG, "removeMonitorRoomData err,roomId: "+ roomId + ",service: " + service +", errMsg: " + (result == null ? "null" : result.getMsg()));
                        this.cancel();
                    }else {
                        tryTimes++;
                    }
                }
            }, 5000L, 5000L);
        }
    }
    private Map getMonitorParams(String roomId, String service){
        Map<String, String> params = new HashMap<>();
        params.put("roomIdService", getRoomIdService(roomId, service));
        params.put("server", OnlineServer.getInstance().getServer());
        params.put("idc", OnlineServer.getInstance().getLanId());
        return params;
    }
    private Map getMonitorHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("key", (String) PropertiesContainer.getInstance().getProperty("internal.key"));
        return headers;
    }
    private String getRoomIdService(String userId, String service) {
        return userId + "###" + service;
    }
    //save init room data if exist
    public void saveRoomData(String userId, String service, Object data) {

    }

    //get room data
    public Object getRoomData(String userId, String service) {
        return null;
    }

    public boolean backUpMemory(){
        return false;
    }
}
