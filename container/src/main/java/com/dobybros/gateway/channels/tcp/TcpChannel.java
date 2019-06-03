package com.dobybros.gateway.channels.tcp;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.pack.Pack;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import java.util.Map;

public class TcpChannel extends Channel {
	protected static final String TAG = "TCP";
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

}
