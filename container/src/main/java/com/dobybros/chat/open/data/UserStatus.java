package com.dobybros.chat.open.data;


import java.util.Map;

public class UserStatus {
	private Integer offlineUnreadCount;
	
	private String service;

	private String userId;

	private String lanId;

	private Map<Integer, DeviceInfo> deviceInfoMap;


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

	public Integer getOfflineUnreadCount() {
		return offlineUnreadCount;
	}

	public void setOfflineUnreadCount(Integer offlineUnreadCount) {
		this.offlineUnreadCount = offlineUnreadCount;
	}

	public Map<Integer, DeviceInfo> getDeviceInfoMap() {
		return deviceInfoMap;
	}

	public void setDeviceInfoMap(Map<Integer, DeviceInfo> deviceInfoMap) {
		this.deviceInfoMap = deviceInfoMap;
	}

	public String getLanId() {
		return lanId;
	}

	public void setLanId(String lanId) {
		this.lanId = lanId;
	}
}