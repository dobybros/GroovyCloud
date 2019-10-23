package client.imclient;

import client.imclient.data.IMMessage;
import client.imclient.utils.WorkerQueue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class IMClient {
    private String userId;
    private String service;
    private String loginUrl;
    private String token;
    private Integer terminal;

    private AtomicLong msgCounter;

    private WorkerQueue<IMMessage> messageWorkerQueue;
    private ConcurrentHashMap<String, IMMessageResultListener> resultMap;

    public IMClient(String userId, String service, Integer terminal, String token, String loginUrl) {
        this.userId = userId;
        this.service = service;
        this.loginUrl = loginUrl;
        this.token = token;
        this.terminal = terminal;

        msgCounter = new AtomicLong(0);
        resultMap = new ConcurrentHashMap<>();

        messageWorkerQueue = new WorkerQueue<>();
        messageWorkerQueue.setHandler(new WorkerQueue.Handler<IMMessage>() {
            @Override
            public boolean handle(IMMessage message) {
                return false;
            }
        });
    }

    public void sendMessage(IMMessage message, IMMessageResultListener resultListener) {
        messageWorkerQueue.offerAndStart(message);
        String msgId = message.getId();
        if(msgId == null) {
            msgId = "MSG_" + msgCounter.getAndIncrement();
        }
        if(resultListener != null)
        resultMap.put(msgId, resultListener);
//        resultMap.put(msgId, resultListener);
//        return future;
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

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }
}
