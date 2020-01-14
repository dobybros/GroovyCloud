package com.dobybros.chat.open;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.open.data.DeviceInfo;
import com.dobybros.chat.open.data.PNInfo;
import com.dobybros.chat.open.data.UserStatus;
import com.dobybros.chat.storage.adapters.ApnPushAdapter;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;

import java.util.Map;

public class MSGServers {
	private static final String TAG = MSGServers.class.getSimpleName();
	protected static volatile MSGServers instance;

	
	/**
	 * 其实使用MSGServers的开发人员一定会是内部人员， 不用通过key做访问限制。
	 */
	String key;

	public MSGServers init(String key) {
		instance = this;
		instance.key = key;
		return instance;
	}
	
	public static MSGServers getInstance() {
		if(instance == null) {
//			synchronized (MSGServers.class) {
//				if(instance == null) {
//					instance = new MSGServers();
//					instance.init("defaultkey");
//				}
//			}
			LoggerEx.error(TAG, "MSGServers need to be initialized first");
		}
		return instance;
	}

	/**
	 *
	 * @param pushMessageMap：key：locale，value：locale所对应的翻译
	 * @param userStatus：一个service所对应所有Device的全量信息
	 * @param userId：发送者id
	 * @param pnInfoMap: 发送各个apn所需参数
	 * @param customPropertyMap：苹果apn的附带参数
	 */
	public void sendPNMessage(Map<String, String> pushMessageMap, UserStatus userStatus, String userId, Map<Integer, PNInfo> pnInfoMap, Map<String, String> customPropertyMap){

		// todo 改造
		// 所有deviceToken不为空直接发送apn，若为空，就从redis中获取这个人的deviceToken发送apn
		if (userStatus != null) {
			UserInfo userInfo = null;
			for (Integer terminal : pnInfoMap.keySet()) {
				DeviceInfo deviceInfo = userStatus.getDeviceInfoMap().get(terminal);
				if (deviceInfo == null || deviceInfo.getDeviceToken() == null) {
					if (userInfo == null) {
						try {
							UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class, userStatus.getLanId());
							userInfo = userInfoAdapter.getUserInfo(userStatus.getUserId(), userStatus.getService());
						} catch (CoreException e) {
							e.printStackTrace();
							LoggerEx.fatal(TAG, "getUserInfo by userInfoAdapter error, eMsg : " + e.getMessage());
						}
					}
					if (userInfo != null) {
						deviceInfo = userInfo.getDeviceInfo(terminal);
					}
				}
				if (deviceInfo != null && deviceInfo.getDeviceToken() != null) {
					PNInfo pnInfo = pnInfoMap.get(terminal);
					// 获取pushMessage
					String pushMessage = null;
					if (deviceInfo.getLocale() != null)
						pushMessage = pushMessageMap.get(deviceInfo.getLocale());
					else
						pushMessage = pushMessageMap.get("default");
					// 处理逻辑
					if(pushMessage != null) {
						String deviceToken = deviceInfo.getDeviceToken();
						if(deviceToken != null) {
							// deviceToken不为空，直接发送apn
							try {
								ApnPushAdapter apnPushAdapter =  StorageManager.getInstance().getStorageAdapter(ApnPushAdapter.class);
								apnPushAdapter.sendMessage(userStatus.getService(), deviceToken, pushMessage, pnInfo.isNeedBadge() ? userStatus.getOfflineUnreadCount() : 0, pnInfo.getSoundFile(), customPropertyMap);
								LoggerEx.info(TAG, "(APN)Send pnmessage " + pushMessage + " to receiverId " + userStatus.getUserId() + " from userId " + userId);
							} catch (CoreException e) {
								e.printStackTrace();
								LoggerEx.error(TAG, "(APN)Send PNMessage " + pushMessage + " to userId " + userStatus.getUserId() + " from userId " + userId + " failed, " + e.getMessage());
							}
						}
					}
				} else {
					LoggerEx.error(TAG, "(APN)Send PNMessage  to userId " + userStatus.getUserId() + ", terminal " + terminal + " from userId " + userId + " failed, deviceinfo " + deviceInfo);
				}
			}



			/*boolean shouldSendAPN = true;
			for (Integer terminal : pnInfoMap.keySet()) {
				DeviceInfo deviceInfo = userStatus.getDeviceInfoMap().get(terminal);
				if (deviceInfo.getDeviceToken() == null) {
					shouldSendAPN = false;
					break;
				}
			}
			if (shouldSendAPN) {
				for (Integer terminal : pnInfoMap.keySet()) {
					DeviceInfo deviceInfo = userStatus.getDeviceInfoMap().get(terminal);
					PNInfo pnInfo = pnInfoMap.get(terminal);
					if (deviceInfo != null) {
						// 获取pushMessage
						String pushMessage = null;
						if (deviceInfo.getLocale() != null)
							pushMessage = pushMessageMap.get(deviceInfo.getLocale());
						else
							pushMessage = pushMessageMap.get("default");
						// 处理逻辑
						if(pushMessage != null) {
							String deviceToken = deviceInfo.getDeviceToken();
							if(deviceToken != null) {
								// deviceToken不为空，直接发送apn
								try {
									ApnPushAdapter apnPushAdapter =  StorageManager.getInstance().getStorageAdapter(ApnPushAdapter.class);
									apnPushAdapter.sendMessage(userStatus.getService(), deviceToken, pushMessage, pnInfo.isNeedBadge() ? userStatus.getOfflineUnreadCount() : 0, pnInfo.getSoundFile(), customPropertyMap);
									LoggerEx.info(TAG, "(APN)Send pnmessage " + pushMessage + " to receiverId " + userStatus.getUserId() + " from userId " + userId);
								} catch (CoreException e) {
									e.printStackTrace();
									LoggerEx.error(TAG, "(APN)Send PNMessage " + pushMessage + " to userId " + userStatus.getUserId() + " from userId " + userId + " failed, " + e.getMessage());
								}
							}
						}
					}
				}
			} else {
				// deviceToken为空，RPC至presence服务器，在presence上发送apn
				PNMessageRequest pnRequest = new PNMessageRequest();
				pnRequest.setShortMessageMap(pushMessageMap);
				pnRequest.setUserId(userStatus.getUserId());
				pnRequest.setSenderId(userId);
				pnRequest.setService(userStatus.getService());
				pnRequest.setPnInfoMap(pnInfoMap);
				LoggerEx.info(TAG, "Send pnmessages: " + pushMessageMap + " to receiverId " + userStatus.getUserId() + " from userId " + userId);
				try {
					PNMessageResponse response = (PNMessageResponse) call(userStatus.getUserId(), pnRequest);
				} catch (Throwable t) {
					t.printStackTrace();
					LoggerEx.error(TAG, "Send PNMessages: " + pushMessageMap + " to userId " + userStatus.getUserId() + " from userId " + userId + " failed, " + t.getMessage());
				}

			}*/
		}
	}
}
