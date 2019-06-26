package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.open.data.Message;

public interface MessageReceivedListener {
	public Integer messageReceived(Message message);
}
