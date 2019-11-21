package com.proxy.im;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.handlers.ProxyContainerDuplexSender;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.utils.SpringContextUtil;
import com.proxy.annotation.ProxyMessageReceived;

public abstract class ProxyMessageReceivedListener {
	protected OnlineServerWithStatus onlineServer = (OnlineServerWithStatus) SpringContextUtil.getBean("onlineServer");
	protected ProxyContainerDuplexSender proxyContainerDuplexSender = (ProxyContainerDuplexSender)SpringContextUtil.getBean("proxyContainerDuplexSender");
	private Class<? extends Data> dataClass;
	

	public abstract void messageReceived(Data data, SessionContext sessionContext)
			throws CoreException;

	public Class<? extends Data> getDataClass() {
		if(dataClass == null) {
			ProxyMessageReceived messageReceived = this.getClass().getAnnotation(ProxyMessageReceived.class);
			if(messageReceived != null) 
				dataClass = messageReceived.dataClass();
		}
		return dataClass;
	}

}