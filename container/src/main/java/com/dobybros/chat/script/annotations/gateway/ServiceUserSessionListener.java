package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.MsgResult;

import java.util.List;

public abstract class ServiceUserSessionListener {

	private String userId;

	private String service;

	public void sessionCreated() {}

	public void sessionClosed(int close) {}

	public List<Integer> channelRegisterd(Integer terminal) {
		return null;
	}

	public void channelCreated(Integer terminal) {}

	public void channelClosed(Integer terminal, int close) {}

	public MsgResult messageReceived(Message message, Integer terminal) {
		return null;
	}

	public MsgResult dataReceived(Message message, Integer terminal) {
		return null;
	}

	public Long getMaxInactiveInterval() {
		return null;
	}

	public void messageSent(Data event, Integer excludeTerminal, Integer toTerminal) {}

	public void messageReceivedFromUsers(Message message, String receiverId, String receiverService){}

	public void pingReceived(Integer terminal) {}


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
}
