package com.dobybros.gateway.channels.tcp;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalDecoder;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.pack.Pack;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TcpChannel extends Channel {
	protected static final String TAG = "TCP";
	private Long pingTime = 0L;
	private Long pingInterval = TimeUnit.SECONDS.toMillis(8);
	private IoSession session;
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
		if(message == null || session.isClosing())
			return;
		
		try {
			Pack hailPack = DataVersioning.getDataPack(session, message);
//			synchronized (session) {
				WriteFuture writeFuture = session.write(hailPack);
//			}
		} catch (Throwable e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Send message " + message + " failed, " + e.getMessage());
		}
	}
	
	@Override
	public boolean close(int close) {
		try {
			if(!session.isClosing()) {
				if(session != null)
					session.close(false);
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
		if (session != null)
			session.setAttribute(key, value);
	}

	@Override
	public String getAttribute(String key) {
		if (session != null) {
			try {
				return (String) session.getAttribute(key);
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Get attribute error, key: " +  key + ", eMsg: " + t.getMessage());
			}
		}

		return null;
	}

	public IoSession getSession() {
		return session;
	}

	public void setSession(IoSession session) {
		this.session = session;
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
		return HailProtocalDecoder.getEncodeVersion(session);
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
		return (String) session.getAttribute(UpStreamHandler.ATTRIBUTE_IP);
	}
}
