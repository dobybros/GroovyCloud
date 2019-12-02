package com.proxy.im;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;

public abstract class ProxyMessageReceivedListener {

	public abstract void messageReceived(Data data, SessionContext sessionContext)
			throws CoreException;
}