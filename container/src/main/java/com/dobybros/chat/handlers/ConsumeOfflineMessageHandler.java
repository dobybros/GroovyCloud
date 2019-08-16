package com.dobybros.chat.handlers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.data.userinfo.ServerInfo;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.data.userinpresence.UserInPresence;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.rpc.reqres.balancer.ServerMessageRequest;
import com.dobybros.chat.rpc.reqres.balancer.ServerMessageResponse;
import com.dobybros.chat.storage.adapters.OfflineMessageAdapter;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInPresenceAdapter;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.server.OnlineServer;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumeOfflineMessageHandler {
	private static final String TAG = ConsumeOfflineMessageHandler.class.getSimpleName();

	private ConcurrentHashMap<String, OfflineMessageReader> readerMap = new ConcurrentHashMap<>();

    @Resource
    private RPCMessageSendingTask messageSendingTask;

    @Resource
    private RPCClientAdapterMap rpcClientAdapterMap;

    @Resource
    private RPCClientAdapterMap rpcClientAdapterMapSsl;

	public ConsumeOfflineMessageHandler() {
		
	}

	public class OfflineMessageReader implements Runnable {
		private String userId;
		private String service;
        public static final int STATUS_IDLE = 0;
        public static final int STATUS_STARTED = 1;
        public static final int STATUS_ENDED = 10;
        private AtomicInteger started = new AtomicInteger(STATUS_IDLE);

		public OfflineMessageReader(String userId, String service) {
			this.userId = userId;
			this.service = service;
		}

        @Override
        public void run() {
		    try {
                LoggerEx.info(TAG, "OfflineMessageReader starts, userId " + userId + " service " + service);
                int total = 0;
                int result = handleOfflineMessage();
                while(result > 0) {
                    total += result;
                    result = handleOfflineMessage();
                }
                LoggerEx.info(TAG, "OfflineMessageReader ends, userId " + userId + " service " + service + " total " + total + " result " + (result >= 0 ? " successfully!" : "failed!"));
            } finally {
                if(started.compareAndSet(STATUS_STARTED, STATUS_ENDED)) {
                    String key = userId + "@" + service;
                    readerMap.remove(key);
                };
            }
        }

        public void start() {
            if(started.compareAndSet(STATUS_IDLE, STATUS_STARTED)) {
                ServerStart.getInstance().getThreadPool().execute(this);
            } else {
                LoggerEx.error(TAG, "OfflineMessageReader has already been started, userId " + userId + " service " + service);
            }
        }

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

        public int handleOfflineMessage() {
            AtomicInteger result = new AtomicInteger(0);
            OfflineMessageAdapter offlineMessageAdapter = StorageManager.getInstance().getStorageAdapter(OfflineMessageAdapter.class);
            try {
//                RPCMessageSendingTask theMessageSendingTask = messageSendingTask;
                List<OfflineMessage> offlineMessages = offlineMessageAdapter.readOfflineMessages(userId, service, 0, 100);
                for (OfflineMessage offlineMessage : offlineMessages) {
                    try {
                        Message message = offlineMessage.getMessage();
                        if(message == null) {
                            LoggerEx.fatal(TAG, "OfflineMessage " + offlineMessage + " don't have message in it, ignore...");
                            continue;
                        }
                        // todo 改造
                        UserInPresenceAdapter userInPresenceAdapter = StorageManager.getInstance().getStorageAdapter(UserInPresenceAdapter.class);
                        UserInPresence userInPresence = userInPresenceAdapter.getLanServer(userId);
                        if (userInPresence != null && userInPresence.getLanId() != null) {
                            String lanId = userInPresence.getLanId();
                            UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class, lanId);
                            UserInfo userInfo = userInfoAdapter.getUserInfo(userId, service);
                            if (userInfo != null) {
                                String ip = null;
                                Integer port = null;
                                ServerInfo serverInfo = userInfo.getServerInfo();
                                String server = null;
                                if (serverInfo != null) {
                                    server = serverInfo.getServer();
                                    if (lanId.equals(OnlineServer.getInstance().getLanId())) {
                                        ip = serverInfo.getIp();
                                        port = serverInfo.getRpcPort();
                                    } else {
                                        ip = serverInfo.getPublicDomain();
                                        port = serverInfo.getSslRpcPort();
                                    }
                                }
                                if(server != null && ip != null && port != null) {
                                    RPCClientAdapter gatewayClientAdapter = null;
                                    if(lanId.equals(OnlineServer.getInstance().getLanId())) {
                                        gatewayClientAdapter = rpcClientAdapterMap.registerServer(ip, port, server);
                                    } else {
                                        gatewayClientAdapter = rpcClientAdapterMapSsl.registerServer(ip, port, server);
                                    }

                                    ServerMessageRequest msgRequest = new ServerMessageRequest();
                                    msgRequest.setMessage(message);
                                    message.setReceiverIds(Arrays.asList(userId));

                                    ServerMessageResponse msgResponse = (ServerMessageResponse) gatewayClientAdapter.call(msgRequest);
                                    if(msgResponse != null) {
                                        Set<String> notReceivedIds = msgResponse.getNotReceivedIds();
                                        if(notReceivedIds == null || notReceivedIds.isEmpty()) {
                                            offlineMessageAdapter.removeOfflineMessages(userId, Arrays.asList(message.getId()));
                                        }
                                    }
                                } else {
                                    throw new CoreException(CoreErrorCodes.ERROR_USER_NOTIN_GATEWAY, "User " + userId + " service " + service + " is not online");
                                }
                            } else {
                                throw new CoreException(CoreErrorCodes.ERROR_USER_NOTIN_GATEWAY, "User " + userId + " service " + service + " is not online");
                            }
                        } else {
                            throw new CoreException(CoreErrorCodes.ERROR_USER_NOTIN_GATEWAY, "User " + userId + " service " + service + " is not online");
                        }



                        /*RPCClientAdapter clientAdapter = presenceServerRPCHandler.getBalancerClientAdapter(userId, false);
                        if(clientAdapter != null) {
                            String server = null;
                            String ip = null;
                            Integer port = null;
                            String lanId = null;

                            UserPresenceRequest request = new UserPresenceRequest();
                            request.setUserId(userId);
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
                                    clientAdapter = presenceServerRPCHandler.getBalancerClientAdapter(userId, false);
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

//                                Integer terminal = null;
                            Integer onlineStatus = null;
//                                Integer offlineUnread = null;
//                                Long lastLoginTime = null;
                            if(response != null) {
                                GatewayServer gatewayServer = response.getGateway();
                                if(gatewayServer != null) {
                                    server = gatewayServer.getServer();
                                    ip = gatewayServer.getIp();
                                    port = gatewayServer.getPort();
                                    lanId = gatewayServer.getLanId();
                                }
//                                    terminal = response.getTerminal();
                                onlineStatus = response.getOnlineStatus();
//                                    offlineUnread = response.getOfflineUnread();
//                                    lastLoginTime = response.getLastOnlineTime();
                            }

                            if(onlineStatus == OnlineInfo.STATUS_ONLINE && server != null && ip != null && port != null && lanId != null) {
                                RPCClientAdapter gatewayClientAdapter = null;
                                if(lanId.equals(OnlineServer.getInstance().getLanId())) {
                                    gatewayClientAdapter = rpcClientAdapterMapTask.registerServer(ip, port, server);
                                } else {
                                    gatewayClientAdapter = rpcClientAdapterMapTaskSsl.registerServer(ip, port, server);
                                }

                                ServerMessageRequest msgRequest = new ServerMessageRequest();
                                msgRequest.setMessage(message);
                                message.setReceiverIds(Arrays.asList(userId));

                                ServerMessageResponse msgResponse = (ServerMessageResponse) gatewayClientAdapter.call(msgRequest);
                                if(msgResponse != null) {
                                    Set<String> notReceivedIds = msgResponse.getNotReceivedIds();
                                    if(notReceivedIds == null || notReceivedIds.isEmpty()) {
                                        offlineMessageAdapter.removeOfflineMessages(userId, Arrays.asList(message.getId()));
                                    }
                                }
                            } else {
                                throw new CoreException(CoreErrorCodes.ERROR_USER_NOTIN_GATEWAY, "User " + userId + " service " + service + " is not online");
                            }
                        } else {
                            throw new CoreException(CoreErrorCodes.ERROR_BALANCERDATA_NOTFOUND, "The expected client adapter " + clientAdapter + " is not found for targetId " + userId + " service " + service + " message " + message);
                        }*/
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Send offline message " + offlineMessage.getId() + "for user " + userId + " service "  + service + " failed, " + e.getMessage() + ", offline message will stop sending...");
                        result.set(-1);
                        break;
                    }
                    result.incrementAndGet();
                }
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Send offline message for user " + userId + " service "  + service + " failed, " + e.getMessage() + ", offline message will stop sending...");
                result.set(-1);
            }
            return result.get();
        }

    }

	public void send(String userId, String service) {
        String key = userId + "@" + service;
        OfflineMessageReader reader = readerMap.get(key);
        if(reader == null) {
            reader = new OfflineMessageReader(userId, service);
            OfflineMessageReader oldReader = readerMap.putIfAbsent(key, reader);
            if(oldReader != null) {
                reader = oldReader;
            }
        }

        reader.start();
	}

}
