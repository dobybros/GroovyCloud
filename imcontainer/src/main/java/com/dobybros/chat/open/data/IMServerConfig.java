package com.dobybros.chat.open.data;

import java.util.concurrent.TimeUnit;

/**
 * @author lick
 * @date 2019/11/19
 */
public class IMServerConfig {
    private boolean needOfflineMessages;
    private boolean filterDuplicatedRecentMessages;
    private boolean needDeviceInfo;

    public IMServerConfig() {
    }

    public IMServerConfig(boolean needOfflineMessages, boolean filterDuplicatedRecentMessages, boolean needDeviceInfo) {
        this.needDeviceInfo = needDeviceInfo;
        this.needOfflineMessages = needOfflineMessages;
        this.filterDuplicatedRecentMessages = filterDuplicatedRecentMessages;
    }

    public boolean isNeedOfflineMessages() {
        return needOfflineMessages;
    }

    public void setNeedOfflineMessages(boolean needOfflineMessages) {
        this.needOfflineMessages = needOfflineMessages;
    }

    public boolean isFilterDuplicatedRecentMessages() {
        return filterDuplicatedRecentMessages;
    }

    public void setFilterDuplicatedRecentMessages(boolean filterDuplicatedRecentMessages) {
        this.filterDuplicatedRecentMessages = filterDuplicatedRecentMessages;
    }

    public boolean isNeedDeviceInfo() {
        return needDeviceInfo;
    }

    public void setNeedDeviceInfo(boolean needDeviceInfo) {
        this.needDeviceInfo = needDeviceInfo;
    }

    public static IMServerConfig imConfig, roomConfig;
    static {
        imConfig = new IMServerConfig(true, true, true);
        roomConfig = new IMServerConfig(false, false, false);
    }
    public static IMServerConfig forIM() {
        return imConfig;
    }
    public static IMServerConfig forRoom() {
        return roomConfig;
    }

}
