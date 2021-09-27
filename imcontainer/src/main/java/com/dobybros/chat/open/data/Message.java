package com.dobybros.chat.open.data;

import chat.utils.ChatUtils;

import java.util.Collection;
import java.util.HashSet;


public class Message {

	/**
	 * 消息的ID
	 */
	private String id;

	/**
	 * 用于消息排重， 特别是在客户端有重发逻辑时， 相同的消息只应该会被发送一次。
	 * 不用带到单聊或者组聊服务器中。
	 */
	private String clientId;

	/**
	 * 将要收到该消息的目标用户ID， 如果有用户收到消息之后， 会把该用户的ID从这里移除掉。
	 * 当这条消息是从离线数据库里查出来的， 此值为空。
	 */
	private Collection<String> receiverIds;

	private String receiverService;

	private Long time;

	/**
	 * 该消息要发送的业务服务器
	 * singlechat/* 代表发送给单聊服务器的任何一台服务器。
	 * singlechat/skdfjea 代表发送给单聊服务器的服务器名称为skdfjea的服务器。
	 */
	private String server;

	/**
	 * 发送消息的用户ID
	 */
	private String userId;

	/**
	 * 发送消息的用户所在的服务， 例如dobybros
	 */
	private String service;

	/**
	 * 消息的二机制数据, 这是业务数据, 不用解析处理。
	 */
	private byte[] data;

	/**
	 * 这是业务数据的类型
	 */
	private String type;

	/**
	 * 业务数据的编码类型
	 */
	private Integer encode;

	private Boolean notSaveOfflineMessage;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Collection<String> getReceiverIds() {
		return receiverIds;
	}

	public void setReceiverIds(Collection<String> receiverIds) {
		this.receiverIds = receiverIds;
	}

	public String getReceiverService() {
		return receiverService;
	}

	public void setReceiverService(String receiverService) {
		this.receiverService = receiverService;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getEncode() {
		return encode;
	}

	public void setEncode(Integer encode) {
		this.encode = encode;
	}

	public Boolean getNotSaveOfflineMessage() {
		return notSaveOfflineMessage;
	}

	public void setNotSaveOfflineMessage(Boolean notSaveOfflineMessage) {
		this.notSaveOfflineMessage = notSaveOfflineMessage;
	}
	
	public Message cloneNew() {
		Message message = cloneWithEmptyReceiveIds();
		if(receiverIds != null)
			message.receiverIds = new HashSet<String>(receiverIds);
		return message;
	}
	
	public Message cloneWithEmptyReceiveIds() {
		Message message = new Message();
		message.clientId = clientId;
		message.data = data;//这个是没有复制的， 共享引用。 需要注意的， 一般情况下不会修改这个二进制
		message.id = id;
		message.server = server;
		message.service = service;
		message.time = time;
		message.type = type;
		message.encode = encode;
		message.userId = userId;
		message.receiverService = receiverService;
		message.notSaveOfflineMessage = notSaveOfflineMessage;
		return message;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(Message.class.getSimpleName());
		builder.append(": ").append(userId).append(" send ").append(type).append(" message to ").append(ChatUtils.toString(receiverIds));
		return builder.toString();
	}
}