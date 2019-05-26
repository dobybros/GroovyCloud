package com.dobybros.chat.rpc.impl;
public interface ExpireListener<T> {
	/**
	 * @param t
	 * @param touch
	 * @param expireTime
	 * @return true means the loop should not continue. false means just continue as it is. 
	 */
	public boolean expired(T t, long touch, long expireTime);
}