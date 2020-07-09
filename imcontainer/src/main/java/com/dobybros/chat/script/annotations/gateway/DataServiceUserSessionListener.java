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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lick on 2020/6/19.
 * Description：
 */
public class DataServiceUserSessionListener {
    private TimerTaskEx storeDataTimer = null;
    private KafkaProducerHandler kafkaProducerHandler = null;
    private String parentUserId;

    private String userId;

    private String service;
    public void restoreData() {
        cancelStoreDataTimer();
        if(kafkaProducerHandler == null){
            kafkaProducerHandler = new KafkaProducerHandler(BaseKafkaConfCenter.getInstance().getKafkaConfCenter());
            kafkaProducerHandler.connect();
        }
        storeDataTimer = new TimerTaskEx() {
            @Override
            public void execute() {
                Object data = getRoomData();
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("roomIdService", getRoomIdService(userId, service));
                dataMap.put("server", OnlineServer.getInstance().getServer());
                dataMap.put("address", "http://" + OnlineServer.getInstance().getIp() + ":" + OnlineServer.getInstance().getHttpPort());
                dataMap.put("idc", OnlineServer.getInstance().getLanId());
                dataMap.put("data", data);
                kafkaProducerHandler.send("GatewayMemoryBackUp", JSON.toJSONString(dataMap).getBytes(Charset.defaultCharset()));
            }
        };
        TimerEx.schedule(storeDataTimer, 20000L, 5000L);
    }

    private void cancelStoreDataTimer(){
        if(storeDataTimer != null){
            storeDataTimer.cancel();
            storeDataTimer = null;
        }
    }
    Object getRoomDataFromMonitor() {
        Result result = ScriptHttpUtils.post(JSON.toJSONString(getMonitorParams()), PropertiesContainer.getInstance().getProperty("gateway.monitor.url") + "/data", getMonitorHeaders(), Result.class);
        if (result != null && result.success()) {
            return result.getData();
        }
        return null;
    }
    void removeMonitorRoomData(int close) {
        cancelStoreDataTimer();
        if(kafkaProducerHandler != null){
            kafkaProducerHandler.disconnect();
            kafkaProducerHandler = null;
        }
        Map params = getMonitorParams();
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
                        LoggerEx.fatal(TAG, "removeMonitorRoomData err,roomId: "+ userId + ",service: " + service +", errMsg: " + (result == null ? "null" : result.getMsg()));
                        this.cancel();
                    }else {
                        tryTimes++;
                    }
                }
            }, 5000L, 5000L);
        }
    }
    private String getRoomIdService(String userId, String service) {
        return userId + "###" + service;
    }
    private Map getMonitorParams(){
        Map<String, String> params = new HashMap<>();
        params.put("roomIdService", getRoomIdService(userId, service));
        params.put("server", OnlineServer.getInstance().getServer());
        return params;
    }
    private Map getMonitorHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("key", (String) PropertiesContainer.getInstance().getProperty("internal.key"));
        return headers;
    }
    public void saveRoomData(Object data) {

    }
    public boolean backUpMemory(){
        return false;
    }
    public Object getRoomData() {
        return null;
    }
    public String getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
