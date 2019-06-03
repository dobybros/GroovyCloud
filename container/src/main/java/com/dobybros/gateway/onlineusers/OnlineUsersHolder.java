package com.dobybros.gateway.onlineusers;


import com.docker.server.OnlineServer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUsersHolder {
	private ConcurrentHashMap<String, OnlineUser> onlineUserMap = new ConcurrentHashMap<>();
//	private EventReceivingTaskEx eventReceivingTask;
	private OnlineUserManager onlineUserManager;
	
	private OnlineServer onlineServer;
	
	public OnlineUsersHolder() {
	}
	
	public void init() {
	}

	public void destroy() {
	}

	public OnlineServer getOnlineServer() {
		return onlineServer;
	}

	public void setOnlineServer(OnlineServer acuServer) {
		this.onlineServer = acuServer;
	}

	public OnlineUser addOnlineUserIfAbsent(OnlineUser onlineUser) {
		OnlineUser user = onlineUserMap.putIfAbsent(onlineUser.getUserId(), onlineUser);
//		if(user == null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(onlineUser.getUserId(), TargetAction.ACTION_ADD));
		return user;
	}
	
	public OnlineUser addOnlineUser(OnlineUser onlineUser) {
		OnlineUser user = onlineUserMap.put(onlineUser.getUserId(), onlineUser);
//		if(user == null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(onlineUser.getUserId(), TargetAction.ACTION_ADD));
		return user;
	}
	
	public boolean deleteOnlineUser(OnlineUser removedUser) {
		boolean bool = onlineUserMap.remove(removedUser.getUserId(), removedUser);
//		if(bool)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(removedUser.getUserId(), TargetAction.ACTION_DELETE));
		return bool;
	}
	
	public OnlineUser deleteOnlineUser(String userId) {
		OnlineUser onlineUser = onlineUserMap.remove(userId);
//		if(onlineUser != null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(userId, TargetAction.ACTION_DELETE));
		return onlineUser;
	}
	
	public OnlineUser getOnlineUser(String userId) {
		OnlineUser user = onlineUserMap.get(userId);
		return user;
	}

	public Integer onlineUserCount() {
		return onlineUserMap.size();
	}
	
	public Collection<OnlineUser> onlineUsers() {
		return onlineUserMap.values();
	}
	
	public Collection<String> onlineUserIds() {
		return onlineUserMap.keySet();
	}

	public OnlineUserManager getOnlineUserManager() {
		return onlineUserManager;
	}

	public void setOnlineUserManager(OnlineUserManager onlineUserManager) {
		this.onlineUserManager = onlineUserManager;
	}

}
