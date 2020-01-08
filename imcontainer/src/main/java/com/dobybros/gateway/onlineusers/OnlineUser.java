package com.dobybros.gateway.onlineusers;

import chat.logs.LoggerEx;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.open.data.DeviceInfo;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.storage.adapters.OfflineMessageAdapter;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.gateway.channels.tcp.TcpChannel;
import com.docker.onlineserver.OnlineServerWithStatus;
import org.apache.commons.lang.StringUtils;
import script.memodb.ObjectId;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUser {
	private static final String TAG = OnlineUser.class.getSimpleName();

	private ConcurrentHashMap<String, OnlineServiceUser> serviceUserMap = new ConcurrentHashMap<>();
	protected OnlineUserManager onlineUseManager;
	private String userId;
	protected OnlineServerWithStatus onlineServer;
	protected UserInfoAdapter userInfoAdapter;
	protected OfflineMessageSavingTask offlineMessageSavingTask;
	protected OfflineMessageAdapter offlineMessageAdapter;
	public OnlineUser() {
		userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class);
		offlineMessageAdapter = StorageManager.getInstance().getStorageAdapter(OfflineMessageAdapter.class);
	}
	
	/**
	 * Need prepare session id in this method.
	 */
//	public abstract void userCreated();
//	public abstract void userDestroyed(int close);
//	public abstract void init();
//	public abstract void initUserFollowAndBlock();
//	public abstract boolean keepOnline(); 
	
	public void initOnlineUser() {
		Collection<OnlineServiceUser> users = serviceUserMap.values();
		for(OnlineServiceUser user : users) {
			try {
				user.initOnlineUser();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static final int RECEIVED = 1;
	public static final int RECEIVED_FROZEN = 10;
	public static final int RECEIVED_NOSERVICEUSER = 20;
//	private int[] onlineUserThreadLock = new int[0];
//	private boolean userThreadIsWorking = false;
//	private Long userThreadStartTakes = null;
	public int eventReceived(Message event) {
		String receiverService = event.getReceiverService();
		if(StringUtils.isBlank(receiverService)) {
			receiverService = event.getService();
		}
		if(!StringUtils.isBlank(receiverService)) {
			OnlineServiceUser onlineServiceUser = serviceUserMap.get(receiverService);
			if(onlineServiceUser != null) {
				return onlineServiceUser.eventReceived(event);
			}
		}
		//TODO 这里的返回值可以是integer的， 信息更丰富哈
		return RECEIVED_NOSERVICEUSER;
	}

	public synchronized void destroySelf(int close) {
		Collection<OnlineServiceUser> users = serviceUserMap.values();
		for(OnlineServiceUser user : users) {
			try {
				user.destroySelf(close);
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Destroy user " + userId + " failed, " + t.getMessage() + " class " + t.getClass().getSimpleName());
			}
		}
	}
	
	public OnlineServiceUser getOnlineServiceUser(String service) {
		if(service == null)
			return null;
		OnlineServiceUser serviceUser = serviceUserMap.get(service);
		return serviceUser;
	}

//	public synchronized OnlineServiceUser addOnlineServiceUser(String service, UserInfo userInfo, String preSessionId, boolean isProxy) {
//		OnlineServiceUser serviceUser = serviceUserMap.get(service);
//		if(serviceUser != null) {
//			return serviceUser;
//		}
//		if(isProxy){
//			serviceUser = new ProxyOnlineServiceUser();
//		}else {
//			serviceUser = new OnlineServiceUser();
//		}
//		serviceUser.setService(service);
//		serviceUser.setUserInfo(userInfo);
//		serviceUser.setOnlineUser(this);
//
//		//方法上加同步了， 就不应该出现并发写的问题了
//		OnlineServiceUser old = serviceUserMap.putIfAbsent(service, serviceUser);
//		if(old != null)
//			return old;
//
//		if(preSessionId == null) {
//			preSessionId = ObjectId.get().toString();
//		}
//
//		if(serviceUser.getStatus() < OnlineServiceUser.STATUS_CREATED) {
//			serviceUser.setSessionId(preSessionId);
//			try {
//				serviceUser.userCreated();
//				serviceUser.setStatus(OnlineServiceUser.STATUS_CREATED);
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
//
//		serviceUser.initOnlineUser();
//
//		return serviceUser;
//	}
	public synchronized OnlineServiceUser addOnlineServiceUser(String service, UserInfo userInfo, String preSessionId, OnlineServiceUser onlineServiceUser) {
		OnlineServiceUser serviceUser = serviceUserMap.get(service);
		if(serviceUser != null) {
			return serviceUser;
		}
		serviceUser = onlineServiceUser;
		serviceUser.setService(service);
		serviceUser.setUserInfo(userInfo);
		serviceUser.setOnlineUser(this);
		//方法上加同步了， 就不应该出现并发写的问题了
		OnlineServiceUser old = serviceUserMap.putIfAbsent(service, serviceUser);
		if(old != null)
			return old;

		if(preSessionId == null) {
			preSessionId = ObjectId.get().toString();
		}

		if(serviceUser.getStatus() < OnlineServiceUser.STATUS_CREATED) {
			serviceUser.setSessionId(preSessionId);
			try {
				serviceUser.userCreated();
				serviceUser.setStatus(OnlineServiceUser.STATUS_CREATED);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		serviceUser.initOnlineUser();

		return serviceUser;
	}
	public synchronized void removeOnlineServiceUser(String service, OnlineServiceUser onlineServiceUser, int close) {
		boolean deleted = serviceUserMap.remove(service, onlineServiceUser);
		if(deleted) {
			try {
				onlineServiceUser.destroySelf(close);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if(serviceUserMap.isEmpty()) {
			onlineUseManager.deleteOnlineUser(this, close);
		}
	}

	public OnlineUserManager getOnlineUseManager() {
		return onlineUseManager;
	}
	public void setOnlineUseManager(OnlineUserManager onlineUseManager) {
		this.onlineUseManager = onlineUseManager;
	}
	public OnlineServerWithStatus getOnlineServer() {
		return onlineServer;
	}
	public void setOnlineServer(OnlineServerWithStatus acuServer) {
		this.onlineServer = acuServer;
	}

	public OfflineMessageSavingTask getOfflineMessageSavingTask() {
		return offlineMessageSavingTask;
	}

	public void setOfflineMessageSavingTask(OfflineMessageSavingTask offlineMessageSavingTask) {
		this.offlineMessageSavingTask = offlineMessageSavingTask;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public ConcurrentHashMap<String, OnlineServiceUser> getServiceUserMap() {
		return serviceUserMap;
	}

	public void setServiceUserMap(ConcurrentHashMap<String, OnlineServiceUser> serviceUserMap) {
		this.serviceUserMap = serviceUserMap;
	}

	public void removeChannel(Channel channel, int closeError) {
		if(channel instanceof TcpChannel) {
			TcpChannel tcpChannel = (TcpChannel) channel;
			Map<String, OnlineServiceUser> map = tcpChannel.getOnlineServiceUsers();
			if(map != null) {
				Collection<OnlineServiceUser> serviceUsers = map.values();
				for(OnlineServiceUser user : serviceUsers) {
					user.removeChannel(tcpChannel, closeError);
				}
			}
		}
	}
	
	public String description() {
		StringBuilder builder = new StringBuilder();
		builder.append("OnlineUser " + this.getUserId() + ": ");
		Collection<OnlineServiceUser> serviceUsers = serviceUserMap.values();
		for(OnlineServiceUser serviceUser : serviceUsers) {
			builder.append(serviceUser.getServiceAndVersion());
			builder.append("@");
			builder.append(serviceUser.getSessionId());
			builder.append(" has channels, ");
			Collection<Channel> channels = serviceUser.getActiveChannels();
			if(channels != null) {
				for(Channel channel : channels) {
					DeviceInfo device = serviceUser.getDeviceInfo(channel.getTerminal());
					String deviceToken = device != null ? device.getDeviceToken() : null;
					builder.append(channel.getTerminal()).append(":v").append(channel.getVersion()).append(":").append(deviceToken).append("; ");
				}
			}
			Integer unreadCount = serviceUser.getUnreadCount();
			if(unreadCount == null)
				unreadCount = 0;
			builder.append("unread: ").append(unreadCount);
			builder.append(", ");
			builder.append("isFrozen: ").append(serviceUser.getWaitClientACKMessageQueue().isFrozen());
			builder.append("||||");
		}
		return builder.toString();
	}
}
