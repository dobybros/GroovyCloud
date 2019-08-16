package com.dobybros.chat.tasks;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.UserStatus;
import com.dobybros.chat.rpc.reqres.balancer.ServerMessageRequest;
import com.dobybros.chat.rpc.reqres.balancer.ServerMessageResponse;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;
import com.dobybros.chat.tasks.MessageSendingSingleThreadQueueWrapper.SpreadMessage;
import com.dobybros.chat.utils.SingleThreadQueue;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.script.BaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class MessageSendingSingleThreadQueueWrapper extends SingleThreadQueue<SpreadMessage> {
		protected static final String TAG = MessageSendingSingleThreadQueueWrapper.class.getSimpleName();
		private RPCClientAdapter clientAdapter;
		private String server;
		private OfflineMessageSavingTask offlineMessageSavingTask;
		private ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
		
		public MessageSendingSingleThreadQueueWrapper(final String server, String ip,
													  Integer port, RPCClientAdapterMap rpcClientAdapterMap, final ConcurrentHashMap<String, MessageSendingSingleThreadQueueWrapper> serverQueueMap, OfflineMessageSavingTask offlineMessageSavingTask) {
			super("Message sending queue on Server " + server, new ConcurrentLinkedQueue<SpreadMessage>(), ServerStart.getInstance().getThreadPool());
			
			RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(ip, port, server, new RPCClientAdapter.ClientAdapterStatusListener(){
				@Override
				public void terminated(String serverName) {
					synchronized (serverQueueMap) {
						MessageSendingSingleThreadQueueWrapper wrapper = serverQueueMap.remove(server);
						if(wrapper != null) {
							LoggerEx.info(TAG, "ClientAdapter " + MessageSendingSingleThreadQueueWrapper.this.clientAdapter + " is terminated, queueWraper is " + wrapper);
						}
					}
				}
			});
			this.offlineMessageSavingTask = offlineMessageSavingTask;
			this.clientAdapter = clientAdapter;
			this.server = server;
			setHandler(new MessageHandler());
		}
		public RPCClientAdapter getRPCClientAdapter() {
			return clientAdapter;
		}
		
		private ConcurrentHashMap<Thread, SpreadMessage> spreadMessageMap = new ConcurrentHashMap<>();
		
		public void add(String targetId, Message message, UserStatus userStatus) {
			Thread thread = Thread.currentThread();
			SpreadMessage sMessage = spreadMessageMap.get(thread);
			if(sMessage == null) {
				HashMap<String, UserStatus> targetIds = new HashMap<>();
				targetIds.put(targetId, userStatus);
				sMessage = new SpreadMessage(message, targetIds, userStatus);
				SpreadMessage theSpreadMessage = spreadMessageMap.putIfAbsent(thread, sMessage);
				if(theSpreadMessage != null) {
					sMessage = theSpreadMessage;
				}
			} 
			HashMap<String, UserStatus> theTargetIds = sMessage.getTargetIds();
			if(theTargetIds != null && !theTargetIds.containsKey(targetId)) {
				theTargetIds.put(targetId, userStatus);
			}
		}

		public void startSending() {
			Thread thread = Thread.currentThread();
			SpreadMessage sMessage = spreadMessageMap.remove(thread);
			if(sMessage != null)
				super.offerAndStart(sMessage);
		}
		
		public class SpreadMessage {
			private Message message;
			private HashMap<String, UserStatus> targetIds;
			private UserStatus userStatus;
			
			public SpreadMessage(Message message, HashMap<String, UserStatus> targetIds, UserStatus userStatus) {
				this.message = message;
				this.targetIds = targetIds;
				this.userStatus = userStatus;
			}
			
			public Message getMessage() {
				return message;
			}
			public void setMessage(Message message) {
				this.message = message;
			}
			public HashMap<String, UserStatus> getTargetIds() {
				return targetIds;
			}
			public void setTargetIds(HashMap<String, UserStatus> targetIds) {
				this.targetIds = targetIds;
			}

			public UserStatus getUserStatus() {
				return userStatus;
			}

			public void setUserStatus(UserStatus userStatus) {
				this.userStatus = userStatus;
			}
		}
		
		public class MessageHandler extends Handler<SpreadMessage> {
			public MessageHandler() {
			}
			@Override
			public boolean handle(SpreadMessage spreadMessage) throws CoreException {
				Message message = spreadMessage.getMessage();
				HashMap<String, UserStatus> targetIds = spreadMessage.getTargetIds();
				// onlineUser为空的userIds
				Set<String> notReceivedIds = targetIds.keySet();
				try {
					ServerMessageRequest request = new ServerMessageRequest();
					request.setMessage(message);
					message.setReceiverIds(notReceivedIds);
					if(MessageSendingSingleThreadQueueWrapper.this.clientAdapter != null) {
						ServerMessageResponse response = (ServerMessageResponse) MessageSendingSingleThreadQueueWrapper.this.clientAdapter.call(request);
						if(response != null) {
							notReceivedIds = response.getNotReceivedIds();
						}
					} else
						throw new CoreException(CoreErrorCodes.ERROR_MESSAGESENDING_NOTPREPARED, "MessageSendingSingleThreadQueueWrapper's clientAdapter is null");
				} catch (CoreException e) {
					e.printStackTrace();
					//TODO need handle error case, otherwise the message will be lost permanently. 
					LoggerEx.warn(TAG, "Send message " + message + " to specified server " + MessageSendingSingleThreadQueueWrapper.this.server + " failed, " + e.getMessage());
				}
//				if(message.getFromOfflineMessageId() != null || message.isInternal() || !message.isNeedOffline()) {
//					LoggerEx.info(TAG, "Message will not be saved as OfflineMessage, because fromOfflineMessageId " + message.getFromOfflineMessageId() + " isInternal " + message.isInternal() + " for message " + message + " or need offline is " + message.isNeedOffline());
//					return true;
//				}
				if(notReceivedIds != null && !notReceivedIds.isEmpty()) {
					Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
					if(notSaveOfflineMessage == null || !notSaveOfflineMessage) {
						message.setReceiverIds(notReceivedIds);
						OfflineMessage offlineMessage = new OfflineMessage();
						offlineMessage.setMessage(message);
						offlineMessageSavingTask.addMessage(offlineMessage);
						for (String targetId : notReceivedIds) {
							try {
								UserStatus userStatus = targetIds.get(targetId);
								if (userStatus != null) {
									UserInfoAdapter adapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class, userStatus.getLanId());
									if (adapter != null) {
										long unread = adapter.increaseUnreadCount(targetId, message.getReceiverService(), 1);
										userStatus.setOfflineUnreadCount(Integer.valueOf((int) unread));
									}
								}
							} catch (Throwable t) {
								LoggerEx.error(TAG, "increase unread count error, targetId: " + targetId + ", eMsg: " + t.getMessage());
							}
						}
					}
					if(notReceivedIds != null && !notReceivedIds.isEmpty()) {
						String service = message.getReceiverService();
						if(service == null) {
							service = message.getService();
						}
						if(service != null) {
							BaseRuntime runtime = scriptManager.getBaseRuntime(service);
							if(runtime != null && runtime instanceof GatewayGroovyRuntime) {
								((GatewayGroovyRuntime)runtime).messageNotReceived(message, targetIds);
							}
						}
					}
				}
				return true;
			}
		}
	}