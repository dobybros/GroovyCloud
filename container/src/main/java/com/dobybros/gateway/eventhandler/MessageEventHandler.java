package com.dobybros.gateway.eventhandler;

import chat.errors.CoreException;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.onlineusers.OnlineUser;

import java.util.Collection;

public class MessageEventHandler extends CommonEventHandler {
	private static final String TAG = MessageEventHandler.class.getSimpleName();
	
	public MessageEventHandler() {
	}

	@Override
	public Collection<String> handleEvent(Message message, OnlineUser excludeUser, Collection<String> receivedUserIds, boolean isReceived) throws CoreException {
		Collection<String> participantIds = message.getReceiverIds();
		Collection<String> whoNotReceivedIds = handleUserReceive(participantIds, message, excludeUser, receivedUserIds, isReceived);
		return whoNotReceivedIds;
	}

}