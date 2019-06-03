package com.dobybros.chat.tasks.presence;


import com.dobybros.chat.open.data.DeviceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * loginTime， weightIds和weightScores有可能为空， 例如Balancer在有Chat server在工作时当机重启了， Balancer会从Chat servers获取所有用户登录信息以恢复Balancer的内存数据。
 * 
 * @author aplomb
 *
 */
public class OnlineInfo {

	public static final int STATUS_ONLINE = 1;
	public static final int STATUS_OFFLINE = 10;

	// gateWay
	private String server;

	// 在线状态，当所有service的所有device都没有的时候status为离线状态，否则处于在线状态
	private int status;

	// 一个用户可能会有多个service
	private Map<String, OnlineServiceInfo> serviceInfoMap;

	public OnlineInfo(int status) {
		this.status = status;
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String toString() {
		return "OnlineInfo " + server + "-" + status + "-" + serviceInfoMap;
	}

	public Map<String, OnlineServiceInfo> getServiceInfoMap() {
		return serviceInfoMap;
	}

	public void setServiceInfoMap(Map<String, OnlineServiceInfo> serviceInfoMap) {
		this.serviceInfoMap = serviceInfoMap;
	}

	/**
	 * 获取serviceInfo，根据needCreate来确定获取不到时是否需要创建
	 */
	public OnlineServiceInfo getOnlineServiceInfo(String service, boolean needCreate) {
        if (service != null) {
            if (this.getServiceInfoMap() == null)
                this.setServiceInfoMap(new HashMap<>());
            OnlineServiceInfo onlineServiceInfo = serviceInfoMap.get(service);
            if (onlineServiceInfo == null && needCreate) {
                onlineServiceInfo = new OnlineServiceInfo();
                onlineServiceInfo.setService(service);
                this.getServiceInfoMap().put(service, onlineServiceInfo);
            }
            return onlineServiceInfo;
        } else
            return null;
	}

	/**
	 * 获取deviceInfo，根据needCreate来确定获取不到时是否需要创建，needCreate为true时如果serviceInfo为空的话也需要创建serviceInfo
	 */
	public DeviceInfo getDeviceInfo(String service, Integer terminal, boolean needCreate) {
        if (service != null && terminal != null) {
            OnlineServiceInfo onlineServiceInfo = this.getOnlineServiceInfo(service, needCreate);
            if (onlineServiceInfo != null)
                return onlineServiceInfo.getOnlineDeviceInfo(terminal, needCreate);
            else
                return null;
        } else {
            return null;
        }
	}

	/**
	 * 删除deviceInfo，如果onlineServiceInfo的deviceMap被删空了，则删除onlineServiceInfo
	 */
	public void removeDeviceInfo(String service, Integer terminal) {
		if (service != null && terminal != null) {
			OnlineServiceInfo onlineServiceInfo = this.getOnlineServiceInfo(service, false);
			if (onlineServiceInfo != null) {
				onlineServiceInfo.removeOnlineDeviceInfo(terminal);
				if (onlineServiceInfo.getDeviceMap().isEmpty()) {
					serviceInfoMap.remove(service);
				}
			}
		}
	}
}
