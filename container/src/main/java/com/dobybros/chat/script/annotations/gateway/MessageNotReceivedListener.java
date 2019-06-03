package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.UserStatus;

import java.util.Map;

public interface MessageNotReceivedListener {
	public void messageNotReceived(Message message, Map<String, UserStatus> userStatusMap);
}
