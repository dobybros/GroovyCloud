package com.dobybros.chat.data;

import com.dobybros.chat.open.data.Message;

/**
 * 离线消息的获取是使用的Message里的receiverIds
 * 清除未读离线消息是直接删除receiverIds中的id。
 * 
 * @author aplombchen
 *
 */
public class OfflineMessage extends DataObject {

	private Long time;

	private Message message;

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
