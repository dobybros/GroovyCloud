package com.dobybros.gateway.eventhandler;

import chat.errors.CoreException;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.onlineusers.OnlineUser;

import java.util.Collection;

public interface EventHandler {
	/**
	 * return targetId collection
	 * 
	 * 
	 * @param event
	 * @param excludeUser
	 * @return  who doesn't receive userIds
	 */
	public Collection<String> handleEvent(Message event, OnlineUser excludeUser, Collection<String> receivedUserIds, boolean isReceived) throws CoreException;
	public void broadcastEvent(Message event) throws CoreException;
}
