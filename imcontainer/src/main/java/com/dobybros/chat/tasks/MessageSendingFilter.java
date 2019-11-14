package com.dobybros.chat.tasks;

import chat.utils.Switch64;
import com.dobybros.chat.open.data.Message;
import com.docker.rpc.RPCClientAdapter;

public interface MessageSendingFilter<T extends Message> {
	/**
	 * Filter message with specified type 
	 * 
	 * @param t
	 * @param clientAdapter 
	 * @param targetId 
	 * @return
	 */
	public void filter(T t, String targetId, RPCClientAdapter clientAdapter, Switch64 offlineSwitches);
}
