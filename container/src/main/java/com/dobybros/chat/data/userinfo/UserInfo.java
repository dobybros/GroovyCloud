package com.dobybros.chat.data.userinfo;

import com.alibaba.fastjson.JSON;
import com.dobybros.chat.open.data.DeviceInfo;

import java.util.Arrays;
import java.util.Map;

/**
 * 这个是要持久化的用户基础数据， 包含deviceToken， terminal以及未读数等信息。 
 * 未读数并不会及时的跟随收到新消息而变化， 而是定期更新的。 
 * 
 * @author aplombchen
 *
 */
public class UserInfo {

	/*
        用户id
     */
	private String userId;

	/*
        用户所在service
     */
	private String service;

	/*
        用户在此service上的未读数
     */
	private Integer offlineUnreadCount;

	/*
        用户设备信息
     */
	private Map<Integer, DeviceInfo> devices;

	/*
        用户所在gateway的信息
     */
	private ServerInfo serverInfo;

	/*
        用户创建时间
     */
	private Long createTime;


	public Map<Integer, DeviceInfo> getDevices() {
		return devices;
	}

	public void setDevices(Map<Integer, DeviceInfo> devices) {
		this.devices = devices;
	}

	public Integer getOfflineUnreadCount() {
		return offlineUnreadCount;
	}

	public void setOfflineUnreadCount(Integer offlineUnreadCount) {
		this.offlineUnreadCount = offlineUnreadCount;
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

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	public DeviceInfo getDeviceInfo(Integer terminal) {
		if (this.getDevices() != null && terminal != null) {
			return this.getDevices().get(terminal);
		}
		return null;
	}

	public String toString() {
		return "UserInfo: userId " + userId + " service " + service + " serverInfo " + (serverInfo != null ? JSON.toJSONString(serverInfo) : null) + " offlineUnreadCount " + offlineUnreadCount + " createTime " + createTime + " devices " + (devices != null ? Arrays.toString(devices.keySet().toArray()) : "");
	}
}