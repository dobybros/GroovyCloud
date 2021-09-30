package com.dobybros.gateway.channels.tcp;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.channels.websocket.data.ChannelContext;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.pack.Pack;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TcpChannel extends Channel {
	protected static final String TAG = "TCP";
	private Long pingTime = 0L;
	private Long pingInterval = TimeUnit.SECONDS.toMillis(8);
	private ChannelContext channelContext;
	private OnlineUser onlineUser;
	private Map<String, OnlineServiceUser> onlineServiceUsers;
	public TcpChannel(Integer terminal) {
		super(terminal);
	}
	
	@Override
	public void offer(Data event) {
		sendEvent(event);
	}
	
	@Override
	public void send(Data event) {
		sendEvent(event);
	}

	private void sendEvent(final Data message) {
		if(message == null || !channelContext.channelIsActive())
			return;
		
		try {
			Pack hailPack = DataVersioning.getDataPack(channelContext, message);
//			synchronized (session) {
			channelContext.write(hailPack);
//			}
		} catch (Throwable e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Send message " + message + " failed, " + e.getMessage());
		}
	}
	
	@Override
	public boolean close(int close) {
		try {
			if(channelContext != null && channelContext.channelIsActive()) {
				channelContext.close();
			}
			channelClosed(close);
		} catch (Throwable t) {
			t.printStackTrace();
			LoggerEx.error(TAG, "Close " + close + " failed, " + t.getMessage());
		}
		return true;
	}

	@Override
	public void setAttribute(String key, String value) {
		if (channelContext != null)
			channelContext.setAttribute(key, value);
	}

	@Override
	public String getAttribute(String key) {
		if (channelContext != null) {
			try {
				return (String) channelContext.getAttribute(key);
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Get attribute error, key: " +  key + ", eMsg: " + t.getMessage());
			}
		}

		return null;
	}


	public OnlineUser getOnlineUser() {
		return onlineUser;
	}

	public void setOnlineUser(OnlineUser onlineUser) {
		this.onlineUser = onlineUser;
	}

	public Map<String, OnlineServiceUser> getOnlineServiceUsers() {
		return onlineServiceUsers;
	}

	public void setOnlineServiceUsers(Map<String, OnlineServiceUser> onlineServiceUsers) {
		this.onlineServiceUsers = onlineServiceUsers;
	}
	@Override
	public Short getEncodeVersion(){
		return channelContext != null ? channelContext.getEncodeVersion() : null;
	}

	public Long getPingTime() {
		return pingTime;
	}

	public void ping(Long pingTime) {
		this.pingTime = pingTime;
	}

	public Long getPingInterval() {
		return pingInterval;
	}

	public void setPingInterval(Long pingInterval) {
		this.pingInterval = pingInterval;
	}
	public String getIp(){
		return channelContext != null ? channelContext.getIp() : null;
	}

	public ChannelContext getChannelContext() {
		return channelContext;
	}

	public void setChannelContext(ChannelContext channelContext) {
		this.channelContext = channelContext;
	}
}
