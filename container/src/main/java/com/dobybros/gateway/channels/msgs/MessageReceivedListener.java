package com.dobybros.gateway.channels.msgs;

import chat.errors.CoreException;
import com.dobybros.chat.annotation.MessageReceived;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.docker.utils.SpringContextUtil;
import org.apache.mina.core.session.IoSession;

public abstract class MessageReceivedListener {
	private Class<? extends Data> dataClass;
	
	protected OnlineUserManager onlineUserManager = (OnlineUserManager) SpringContextUtil.getBean("onlineUserManager");

	public abstract void messageReceived(Data data, IoSession session)
			throws CoreException;

	public Class<? extends Data> getDataClass() {
		if(dataClass == null) {
			MessageReceived messageReceived = this.getClass().getAnnotation(MessageReceived.class);
			if(messageReceived != null) 
				dataClass = messageReceived.dataClass();
		}
		return dataClass;
	}

}