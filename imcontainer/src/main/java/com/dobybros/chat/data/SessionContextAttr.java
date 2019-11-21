package com.dobybros.chat.data;

/**
 * @author lick
 * @date 2019/11/18
 */
public class SessionContextAttr {
    private String userId;
    private String service;
    private Integer terminal;
    private String channelId;

    public SessionContextAttr(String userId, String service, Integer terminal){
        this.userId = userId;
        this.service = service;
        this.terminal = terminal;
    }
    public SessionContextAttr(String userId, String service, Integer terminal, String channelId){
        this.userId = userId;
        this.service = service;
        this.terminal = terminal;
        this.channelId = channelId;
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

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean checkParamsNotNull(){
        return userId != null && service != null && terminal != null;
    }
    public boolean checkAllParamsNotNull(){
        return userId != null && service != null && terminal != null && channelId != null;
    }

}
