package com.dobybros.chat.tasks.presence;

import java.util.concurrent.ConcurrentHashMap;

public class OnlineTargetsHelper {
	private static final String TAG = OnlineTargetsHelper.class.getSimpleName();
	
	private static final OnlineTargetsHelper instance = new OnlineTargetsHelper();
	
	/**
	 * targetId, OnlineInfo
	 * All online targets are in this map. 
	 */
	private ConcurrentHashMap<String, OnlineInfo> allOnlineMap = new ConcurrentHashMap<>();

	/**
	 * server, targetId set
	 * Every targetId on each server.   
	 */
	private ConcurrentHashMap<String, ServerInfo> onlineChatServerMap = new ConcurrentHashMap<>();
	
	public static OnlineTargetsHelper getInstance() {
		return instance;
	}
	
	public ConcurrentHashMap<String, OnlineInfo> getAllOnlineMap() {
		return allOnlineMap;
	}
	
	/**
	 * Add target into allOnlineMap, the target may not be assigned to any server yet. 
	 * 
	 * @param targetId
	 * @param onlineInfo
	 * @return return the actual OnlineInfo. 
	 */
	public OnlineInfo addTargetToAll(String targetId, OnlineInfo onlineInfo) {
		OnlineInfo theRealOne = allOnlineMap.putIfAbsent(targetId, onlineInfo);
		if(theRealOne == null)
			return onlineInfo;
		return theRealOne;
	}
	
	public boolean removeTargetFromAll(String targetId, OnlineInfo info) {
		return allOnlineMap.remove(targetId, info);
	}
	
	public OnlineInfo getOnlineInfo(String targetId) {
		if(targetId == null)
			return null;
		return allOnlineMap.get(targetId);
	}
	
	public boolean containsTargetId(String targetId) {
		return allOnlineMap.containsKey(targetId);
	}
	
	public void addTargetToServer(String targetId, String server) {
		ServerInfo serverInfo = onlineChatServerMap.get(server);
		if(serverInfo == null) {
			serverInfo = new ServerInfo();
			ServerInfo old = onlineChatServerMap.putIfAbsent(server, serverInfo);
			if(old != null)
				serverInfo = old;
		}
		serverInfo.addTargetId(targetId);
	}
	
	public boolean removeTargetFromServer(String targetId, String server) {
		ServerInfo serverInfo = onlineChatServerMap.get(server);
		if(serverInfo != null) {
			return serverInfo.removeTargetId(targetId);
		}
		return false;
	}
	
	public void removeServer(String server) {
		ServerInfo serverInfo = onlineChatServerMap.remove(server);
		if(serverInfo != null) {
			for(String targetId : serverInfo.getTargetIds()) {
				OnlineInfo info = getOnlineInfo(targetId);
				if(info.getServer() != null && info.getServer().equals(server)) {
//					allOnlineMap.remove(targetId, info);
					info.setStatus(OnlineInfo.STATUS_OFFLINE);
					info.setServer(null);
				}
			}
			serverInfo.clear();
		}
	}
	
	public Integer countOnlineOnServer(String server) {
		ServerInfo serverInfo = onlineChatServerMap.get(server);
		if(serverInfo != null) {
			return serverInfo.size();
		}
		return null;
	}

	public ConcurrentHashMap<String, ServerInfo> getOnlineChatServerMap() {
		return onlineChatServerMap;
	}
	
}
