package com.dobybros.chat.tasks;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.data.userinfo.ServerInfo;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.data.userinpresence.UserInPresence;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.UserStatus;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInPresenceAdapter;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.script.BaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.server.OnlineServer;
import com.docker.tasks.Task;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RPCMessageSendingTask extends Task {
	private static final String TAG = RPCMessageSendingTask.class.getSimpleName();

//	private BalancerDataCacheCallListener callListener;
	private LinkedBlockingQueue<Message> messageQueue;

	private static RPCMessageSendingTask instance;
	
	private ConcurrentHashMap<String, MessageSendingSingleThreadQueueWrapper> serverQueueMap = new ConcurrentHashMap<>();
	
	@Resource
	private OfflineMessageSavingTask offlineMessageSavingTask;
	
	@Resource
	private RPCClientAdapterMap rpcClientAdapterMap;

	@Resource
	private RPCClientAdapterMap rpcClientAdapterMapSsl;
	
	@Resource
	private OnlineServerWithStatus onlineServer;
	
	@Resource
	private ScriptManager scriptManager;
	
	private boolean isStarted = true;
	
	public RPCMessageSendingTask() {
		instance = this;
	}
	
	public static RPCMessageSendingTask getInstance() {
		return instance;
	}
	
	public MessageSendingSingleThreadQueueWrapper getServerQueue(String server, String ip, Integer port) {
		return getServerQueue(server, ip, port, null);
	}
	
	public MessageSendingSingleThreadQueueWrapper getServerQueue(String server, String ip, Integer port, String lanId) {
		MessageSendingSingleThreadQueueWrapper serverQueue = serverQueueMap.get(server);
//		IMessageQueueService messageQueueService = messageQueueMap.get(server);
		if(serverQueue == null) {
			long time = System.currentTimeMillis();
			synchronized (serverQueueMap) {
				serverQueue = serverQueueMap.get(server);
				if(serverQueue == null) {
					RPCClientAdapterMap clientAdapterMap = null;
					if(lanId != null && !lanId.equals(OnlineServer.getInstance().getLanId())) {
						clientAdapterMap = rpcClientAdapterMapSsl;
					} else {
						clientAdapterMap = rpcClientAdapterMap;
					}
					serverQueue = new MessageSendingSingleThreadQueueWrapper(server, ip, port, clientAdapterMap, serverQueueMap, offlineMessageSavingTask);
					MessageSendingSingleThreadQueueWrapper actual = serverQueueMap.putIfAbsent(server, serverQueue);
					if(actual != null) {
						serverQueue = actual;
					}
					LoggerEx.info(TAG, "Create new serverQueue " + serverQueue + " for server " + server + " takes " + (System.currentTimeMillis() - time));
					return serverQueue;
				}
			}
		} 
		return serverQueue;
	}
	
	@Override
	public void execute() {
		while(isStarted) {
			LoggerEx.info(TAG, "Message sending task started " + Thread.currentThread());
			try {
				Message message = null;
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(message != null) {
					if(message instanceof ShutdownMessage) {
						break;
					}
					Collection<String> targetIds = message.getReceiverIds();
					HashMap<String, UserStatus> userStatusMap = new HashMap<>();
					if(targetIds != null && !targetIds.isEmpty()) {
						HashSet<MessageSendingSingleThreadQueueWrapper> wrappers = new HashSet<>();
						HashSet<String> offlineTargetIds = null;
						Map<String, UserInfoAdapter> offlineUserInfoAdapterMap = null;
						// 遍历接收方的id
						UserInPresenceAdapter userInPresenceAdapter = StorageManager.getInstance().getStorageAdapter(UserInPresenceAdapter.class);
						for(String targetId : targetIds) {
							// todo 改造
							UserInPresence userInPresence = userInPresenceAdapter.getLanServer(targetId);
							if (userInPresence != null && StringUtils.isNotBlank(userInPresence.getLanId())) {
								UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class, userInPresence.getLanId());
								UserInfo userInfo = null;
								String receiverService = message.getReceiverService();
								if(StringUtils.isBlank(receiverService))
									receiverService = message.getService();
								try {
									userInfo = userInfoAdapter.getUserInfo(targetId, receiverService);
									if (userInfo != null) {
										String lanId = userInPresence.getLanId();
										String server = null;
										String ip = null;
										Integer port = null;
										ServerInfo serverInfo = userInfo.getServerInfo();
										if (serverInfo != null) {
											server = serverInfo.getServer();
											if (lanId.equals(onlineServer.getLanId())) {
												ip = serverInfo.getIp();
												port = serverInfo.getRpcPort();
											} else {
												ip = serverInfo.getPublicDomain();
												port = serverInfo.getSslRpcPort();
											}
										}
										UserStatus userStatus = new UserStatus();
										userStatus.setUserId(targetId);
										userStatus.setService(receiverService);
										userStatus.setLanId(userInPresence.getLanId());
										userStatus.setOfflineUnreadCount(userInfo.getOfflineUnreadCount());
										userStatus.setDeviceInfoMap(userInfo.getDevices());
										if(server != null && ip != null && port != null) {
											// 用户在线，发送消息
											if(onlineServer.getServer().equals(server))
												throw new CoreException(CoreErrorCodes.ERROR_SEND_MESSAGE_TO_OWN, "Send message error, the message is sending to users own chat.");
											MessageSendingSingleThreadQueueWrapper serverTransporter = getServerQueue(server, ip, port, lanId);
											if(serverTransporter != null) {
//											message.setSequence(System.currentTimeMillis());
												message.setServer(onlineServer.getServer());
//											serverTransporter.offerAndStart(message);
												serverTransporter.add(targetId, message.cloneWithEmptyReceiveIds(), userStatus);
												if(!wrappers.contains(serverTransporter)) {
													wrappers.add(serverTransporter);
												}
											}
										} else {
											// 用户离线，将用户id放在离线容器内
											//TODO inc unread
											if(offlineTargetIds == null)
												offlineTargetIds = new HashSet<>();
											offlineTargetIds.add(targetId);
											if (offlineUserInfoAdapterMap == null)
												offlineUserInfoAdapterMap = new HashMap<>();
											offlineUserInfoAdapterMap.put(targetId, userInfoAdapter);
											userStatusMap.put(targetId, userStatus);
										}
									} else {
										if(offlineTargetIds == null)
											offlineTargetIds = new HashSet<>();
										offlineTargetIds.add(targetId);
										if (offlineUserInfoAdapterMap == null)
											offlineUserInfoAdapterMap = new HashMap<>();
										offlineUserInfoAdapterMap.put(targetId, userInfoAdapter);
									}
								} catch (Throwable e) {
									e.printStackTrace();
									LoggerEx.fatal(TAG, "get userIfo by userInfoAdapter, userId : " + targetId + ", service : " + receiverService);
									if (e instanceof CoreException) {
										if(offlineTargetIds == null)
											offlineTargetIds = new HashSet<>();
										offlineTargetIds.add(targetId);
										if (offlineUserInfoAdapterMap == null)
											offlineUserInfoAdapterMap = new HashMap<>();
										offlineUserInfoAdapterMap.put(targetId, userInfoAdapter);
									}
								}
							} else {
								if(offlineTargetIds == null)
									offlineTargetIds = new HashSet<>();
								offlineTargetIds.add(targetId);
							}



							/*try {
								// 根据usetId获取user所在的presence所对应的clientAdapter
								RPCClientAdapter clientAdapter = presenceServerRPCHandler.getBalancerClientAdapter(targetId, false);
								if(clientAdapter != null) {
									String server = null;
									String ip = null;
									Integer port = null;
									String lanId = null;

									// RPC到presence上面获取接收者在线状态、所在gateWay、消息未读数、最后一次登录时间、所用语言
									UserPresenceRequest request = new UserPresenceRequest();
									request.setUserId(targetId);
									String receiverService = message.getReceiverService();
									if(StringUtils.isBlank(receiverService))
										receiverService = message.getService();
									request.setService(receiverService);
									request.setFromLanId(OnlineServer.getInstance().getLanId());
//									request.setTargetIds(Arrays.asList(targetId));
									UserPresenceResponse response;
									try {
										response = (UserPresenceResponse) clientAdapter.call(request);
									} catch (CoreException ce) {
										ce.printStackTrace();
										if(ce.getCode() == CoreErrorCodes.ERROR_RMICALL_RETRY) {
											//TODO 待观察，若无需要retry，应干掉
											LoggerEx.info(TAG, "RMI call failed, so try call again. TODO, " + ce.getMessage());
											clientAdapter = presenceServerRPCHandler.getBalancerClientAdapter(targetId, false);
											if(clientAdapter != null) {
												response = (UserPresenceResponse) clientAdapter.call(request);
												LoggerEx.info(TAG, "Retry call successfully " + response);
											} else {
												LoggerEx.error(TAG, "Retry call failed(clientAdapter is null),  " + ce.getMessage());
												throw ce;
											}
										} else {
											throw ce;
										}
									}

									// 根据response获取gateWay相关信息，并转化UserStatus
									UserStatus userStatus = new UserStatus();
									userStatus.setUserId(targetId);
									userStatus.setService(request.getService());
									if(response != null) {  
										GatewayServer gatewayServer = response.getGateway();
										if(gatewayServer != null) {
											server = gatewayServer.getServer();
											ip = gatewayServer.getIp();
											port = gatewayServer.getPort();
											lanId = gatewayServer.getLanId();
										}

										userStatus.setOfflineUnreadCount(response.getOfflineUnread());
										userStatus.setDeviceInfoMap(response.getDeviceMap());

									}

									if(server != null && ip != null && port != null) {
										// 用户在线，发送消息
										if(onlineServer.getServer().equals(server))
											throw new CoreException(CoreErrorCodes.ERROR_SEND_MESSAGE_TO_OWN, "Send message error, the message is sending to users own chat.");
										MessageSendingSingleThreadQueueWrapper serverTransporter = getServerQueue(server, ip, port, lanId);
										if(serverTransporter != null) {
//											message.setSequence(System.currentTimeMillis());
											message.setServer(onlineServer.getServer());
//											serverTransporter.offerAndStart(message);
											serverTransporter.add(targetId, message.cloneWithEmptyReceiveIds(), userStatus);
											if(!wrappers.contains(serverTransporter)) {
												wrappers.add(serverTransporter);
											}
										}
									} else {
										// 用户离线，将用户id放在离线容器内
										if(offlineTargetIds == null)
											offlineTargetIds = new HashSet<>();
										offlineTargetIds.add(targetId);
										userStatusMap.put(targetId, userStatus);
									}
								} else {
									throw new CoreException(CoreErrorCodes.ERROR_BALANCERDATA_NOTFOUND, "The expected client adapter " + clientAdapter + " is not found for targetId " + targetId + " message " + message);
								}
							} catch (Throwable t) {
								t.printStackTrace();
								LoggerEx.error(TAG, "Find balancer or chat server failed " + t.getMessage() + " for send message " + message);
								
								if(t instanceof CoreException) {
//									OfflineMessage offlineMessage = new OfflineMessage();
//									offlineMessage.setMessage(message.clone());
//									offlineMessageSavingTask.addMessage(offlineMessage);

									//User wasn't be found, save offline message only
									if(offlineTargetIds == null)
										offlineTargetIds = new HashSet<>();
									offlineTargetIds.add(targetId);
								}
								
							}*/
						}
						if(offlineTargetIds != null && !offlineTargetIds.isEmpty()) {
							// 如果有离线的人并且这条消息需要存离线，则进行离线存储
							Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
							if(notSaveOfflineMessage == null || !notSaveOfflineMessage) {
								Message clonedMessage = message.cloneWithEmptyReceiveIds();
								clonedMessage.setReceiverIds(offlineTargetIds);
								OfflineMessage offlineMessage = new OfflineMessage();
								offlineMessage.setMessage(clonedMessage);
								offlineMessageSavingTask.addMessage(offlineMessage);
								for (String targetId : offlineTargetIds) {
									try {
										UserInfoAdapter adapter = offlineUserInfoAdapterMap.get(targetId);
										if (adapter != null) {
											long unread = adapter.increaseUnreadCount(targetId, clonedMessage.getReceiverService(), 1);
											UserStatus userStatus = userStatusMap.get(targetId);
											if (userStatus != null)
												userStatus.setOfflineUnreadCount(Integer.valueOf((int) unread));
										}
									} catch (Throwable t) {
										LoggerEx.error(TAG, "increase unread count error, targetId: " + targetId + ", eMsg: " + t.getMessage());
									}
								}
							}
							// 通知业务对离线消息进行处理
							String service = message.getReceiverService();
							if(service == null) {
								service = message.getService();
							}
							if(service != null) {
								BaseRuntime runtime = scriptManager.getBaseRuntime(service);
								if(runtime != null && runtime instanceof GatewayGroovyRuntime) {
									((GatewayGroovyRuntime)runtime).messageNotReceived(message, userStatusMap);
								}
							}
						}
						if(!wrappers.isEmpty()) {
							for(MessageSendingSingleThreadQueueWrapper wrapper : wrappers) {
								wrapper.startSending();
							}
						}
						/*serverPresentService.queryPresentServers(cid, new IteratorEx<String>() {
							@Override
							public boolean iterate(String server) {
								if(onlineServer.getServerName().equals(server))
									return true;
								try {
									SingleThreadQueue<Message> serverTransporter = getServerQueue(server);
									if(serverTransporter != null) {
										theMessage.setSequence(System.currentTimeMillis());
										theMessage.setServer(onlineServer.getServerName());
										serverTransporter.offerAndStart(theMessage);
									}
								} catch (CoreException e) {
									e.printStackTrace();
									LoggerEx.fatal(TAG, "Create ServerTransporter for server " + server + " failed, " + e.getMessage());
								}
								return true;
							}
						}, targetIds);*/
					} else {
						LoggerEx.error(TAG, "Event don't have targetIds, " + message);
					}
				}
			}catch(Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Event sending failed, " + t.getMessage());
			}
		}
		LoggerEx.info(TAG, RPCMessageSendingTask.class.getSimpleName() + " is shutted down, " + Thread.currentThread());
	}
	
	public void destroy() {
	}
	
	public void addMessage(Message message) {
		try {
			if(messageQueue != null)
				messageQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() throws Throwable {
		messageQueue = new LinkedBlockingQueue<>();
//		callListener = new BalancerDataCacheCallListener();
	}

	public LinkedBlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	public ConcurrentHashMap<String, MessageSendingSingleThreadQueueWrapper> getServerQueueMap() {
		return serverQueueMap;
	}

	public void setServerQueueMap(ConcurrentHashMap<String, MessageSendingSingleThreadQueueWrapper> serverQueueMap) {
		this.serverQueueMap = serverQueueMap;
	}

	class ShutdownMessage extends Message {
	}
	
	@Override
	public void shutdown() {
		LoggerEx.info(TAG, RPCMessageSendingTask.class.getSimpleName() + " is shutting down, " + (messageQueue != null ? messageQueue.size() : 0) + " is still left. ");
		isStarted = false;
		Integer numOfThread = getNumOfThreads();
		if(numOfThread == null) 
			numOfThread = 1;
		for(int i = 0; i < numOfThread; i++) {
			addMessage(new ShutdownMessage());
		}
	}

}
