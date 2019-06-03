package com.dobybros.chat.open.data;

/**
 * Created by zhanjing on 2017/7/26.
 */
public class DeviceInfo {
    private Integer terminal;

    private String deviceToken;

    private Long loginTime;

    private String locale;

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String toString() {
        return "DeviceInfo: terminal " + terminal + " deviceToken " + deviceToken + " loginTime " + loginTime + " locale " + locale;
    }
}
