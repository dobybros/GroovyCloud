package com.dobybros.chat.tasks.presence;

import com.dobybros.chat.open.data.DeviceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于service的对象，一个人可以包含多个service
 *
 * Created by zhanjing on 2017/7/24.
 */
public class OnlineServiceInfo {

    // eg：saasicsonseller
    String service;

    // 未读数
    private Integer offlineUnreadCount;

    // 是否需要存储到数据库（这个地方只做预留）
    private boolean needSave = false;

    // 设备信息
    Map<Integer, DeviceInfo> deviceMap;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getOfflineUnreadCount() {
        return offlineUnreadCount;
    }

    public void setOfflineUnreadCount(Integer offlineUnreadCount) {
        this.offlineUnreadCount = offlineUnreadCount;
    }

    public Map<Integer, DeviceInfo> getDeviceMap() {
        return deviceMap;
    }

    public void setDeviceMap(Map<Integer, DeviceInfo> deviceMap) {
        this.deviceMap = deviceMap;
    }

    public synchronized int increaseUnreadCount() {
        if(offlineUnreadCount == null)
            offlineUnreadCount = 0;
        if(!needSave)
            needSave = true;
        return ++offlineUnreadCount;
    }

    public boolean isNeedSave() {
        return needSave;
    }

    public void setNeedSave(boolean needSave) {
        this.needSave = needSave;
    }

    public DeviceInfo getOnlineDeviceInfo(Integer terminal, boolean needCreate) {
        if (terminal != null) {
            if (this.getDeviceMap() == null)
                this.setDeviceMap(new HashMap<>());
            DeviceInfo onlineDeviceInfo = this.getDeviceMap().get(terminal);
            if (onlineDeviceInfo == null && needCreate) {
                onlineDeviceInfo = new DeviceInfo();
                onlineDeviceInfo.setTerminal(terminal);
                this.getDeviceMap().put(terminal, onlineDeviceInfo);
            }
            return onlineDeviceInfo;
        } else {
            return null;
        }
    }

    public void removeOnlineDeviceInfo(Integer terminal) {
        if (deviceMap != null && terminal != null) {
            deviceMap.remove(terminal);
        }
    }
}
