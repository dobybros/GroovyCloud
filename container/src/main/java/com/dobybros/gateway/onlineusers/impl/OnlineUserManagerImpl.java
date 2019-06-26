package com.dobybros.gateway.onlineusers.impl;

import chat.errors.CoreException;
import chat.utils.ConcurrentHashSet;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;

import javax.annotation.Resource;
import java.util.Collection;

public class OnlineUserManagerImpl extends OnlineUserManager {
	private static final String TAG = "OUMI";
	@Resource
	private OfflineMessageSavingTask offlineMessageSavingTask;
	@Resource
	private RPCMessageSendingTask messageSendingTask;
	
	@Resource
	private MessageEventHandler messageEventHandler;

	public OnlineUserManagerImpl() {
	}
	
	@Override
	protected final void sendEventToOnlineUsers(Message event, OnlineUser excludeUser) throws CoreException {
		Collection<String> receivedUserIds = new ConcurrentHashSet<>();
		Collection<String> targetIds = messageEventHandler.handleEvent(event, excludeUser, receivedUserIds, false);
		if(targetIds != null) {
//					Collection<String> retryTargetIds = event.getTargetIds();
//					if(retryTargetIds != null && !retryTargetIds.isEmpty() && !receivedUserIds.isEmpty()) {
//						if(!event.isInternal()) //如果是internal的事件， 则不存储离线消息
//							offlineMessageSavingTask.updateOfflineMessagePendingMap(event.getId(), receivedUserIds);
//					}
			if(!targetIds.isEmpty()) {
				event.setReceiverIds(targetIds);
				messageEventHandler.broadcastEvent(event);
			}
		}
	}
	@Override
	public Collection<String> eventReceived(Message event, OnlineUser excludeUser) throws CoreException {
		Collection<String> receivedUserIds = new ConcurrentHashSet<>();
		return messageEventHandler.handleEvent(event, excludeUser, receivedUserIds, true);
	}
}