package com.dobybros.chat.script.annotations.gateway;

public class GWUserParams {
    int close;
    String userId;
    String service;
    Integer terminal;
    public static final int ACTION_SESSIONCREATED = 10;
    public static final int ACTION_SESSIONCLOSED = 20;
    public static final int ACTION_CHANNELCREATED = 30;
    public static final int ACTION_CHANNELCLOSED = 40;
    int action;
    public GWUserParams(int action, String userId, String service) {
        this(action, userId, service, null, 0);
    }
    public GWUserParams(int action, String userId, String service, Integer terminal) {
        this(action, userId, service, terminal, 0);
    }
    public GWUserParams(int action, String userId, String service, Integer terminal, int close) {
        this.close = close;
        this.service = service;
        this.userId = userId;
        this.terminal = terminal;
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
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
