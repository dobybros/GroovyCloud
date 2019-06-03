package com.dobybros.gateway.onlineusers.impl;

import com.dobybros.gateway.onlineusers.OnlineUser;
import com.docker.utils.SpringContextUtil;

import java.util.Properties;

public class AdminOnlineUserImpl extends OnlineUser {
	
	private Properties adminContactsProperties = (Properties) SpringContextUtil.getBean("adminContactsProperties");
	
}
