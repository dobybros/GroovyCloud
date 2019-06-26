package com.dobybros.chat.tasks;

import com.dobybros.chat.open.data.Message;

public interface MessageReceivingFilter {
	/**
	 * Filter message with specified type 
	 * 
	 * return true to stop spreading.  Means stop to execute OnlineUserManager#eventReceived. 
	 * return false to continue;
	 * 
	 * @param t
	 * @return
	 */
	public boolean filter(Message t);
}
