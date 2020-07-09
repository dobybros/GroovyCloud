package com.dobybros.chat.channels;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import script.memodb.ObjectId;

public abstract class Channel {
	private static final String TAG = Channel.class.getSimpleName();
	private Integer terminal;
	private Integer version;
	private String id;
	
	private ChannelListener channelListener;

	public Channel(Integer terminal) {
		this.terminal = terminal;
		id = ObjectId.get().toString();
	}

	/**
	 * Try to send immediately
	 * 
	 * @param event
	 */
	public abstract void send(Data event);
	public abstract Short getEncodeVersion();
	/**
	 * Only offer into queue, will send later. 
	 * 
	 * @param event
	 */
	public abstract void offer(Data event);

	public abstract boolean close(int close);

	public abstract void setAttribute(String key, String value);

	public abstract String getAttribute(String key);
	
	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}
	
	public ChannelListener getChannelListener() {
		return channelListener;
	}

	public void setChannelListener(ChannelListener channelListener) {
		this.channelListener = channelListener;
	}

	public void channelCreated() {
		if(channelListener != null) {
			try {
				channelListener.channelCreated(this);
			} catch (Throwable e) {
				LoggerEx.error(TAG, "channel created failed, " + e.getMessage());
			}
		}
	}
	public void channelClosed(int close) {
		if(channelListener != null) {
			try {
				channelListener.channelClosed(this, close);
			} catch (Throwable e) {
				LoggerEx.error(TAG, "channel closed " + close + " failed, " + e.getMessage());
			}
		}
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static interface ChannelListener {
		// 客户端channel断掉重连是关闭以前的通道
		public static final int CLOSE_CHANNELEXPIRED = 10;

		// 踢掉其他设备上的channel
		public static final int CLOSE_KICKED = 20;

		// 用户登出关闭channel
		public static final int CLOSE_LOGOUT = 30;

		// 用户同设备有旧channel切换到新channel时，关闭旧channel
		public static final int CLOSE_SWITCHCHANNEL = 40;

		// 通道发生异常
		public static final int CLOSE_ERROR = 50;

		// session close
		public static final int CLOSE = 60;
		public static final int CLOSE_USEREXPIRED = 70;
		public static final int CLOSE_DESTROYED = 80;
		public static final int CLOSE_DESTROYED_NOTDELETEMONITORMEMORY = 85;
		public static final int CLOSE_SHUTDOWN = 90;
		public static final int CLOSE_MUSTUPGRADE = 100;
		public static final int CLOSE_PASSWORDCHANGED = 110;
		public static final int CLOSE_NOTICEIM_IMMEDIATELY = 117;
		public static final int CLOSE_NOTICEIM = 118;
		public static final int CLOSE_IMMEDIATELY = 119;
		public static final int CLOSE_FORBIDDEN = 120;

		public void channelClosed(Channel channel, int close);
		public void channelCreated(Channel channel);
		public void sent(Data data);
		public void exceptionCaught(Throwable cause);
	}
	
}
