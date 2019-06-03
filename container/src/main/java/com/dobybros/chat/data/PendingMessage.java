package com.dobybros.chat.data;

import com.dobybros.chat.open.data.Message;
import com.docker.data.DataObject;

/**
 * Gateway服务器连接不上第三方服务器， 例如单聊服务器不可用时。 
 * 收到的消息需要存储起来， 等第三方服务器恢复之后， 再给第三方服务器处理。 
 * 
 * @author aplombchen
 */
public class PendingMessage extends DataObject {
	private Long time;
	
	private Message message;
	
	/**
	 * 该消息要发送的业务服务器
	 * singlechat/* 代表发送给单聊服务器的任何一台服务器。 
	 * singlechat/skdfjea 代表发送给单聊服务器的服务器名称为skdfjea的服务器。 
	 * 
	 * 此处只会存储singlechat
	 */
	private String server; 

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
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
	
}
