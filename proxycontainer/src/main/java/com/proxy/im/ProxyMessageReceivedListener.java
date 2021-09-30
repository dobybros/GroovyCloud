package com.proxy.im;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.websocket.data.ChannelContext;

public abstract class ProxyMessageReceivedListener {

	public abstract void messageReceived(Data data, ChannelContext context)
			throws CoreException;
}