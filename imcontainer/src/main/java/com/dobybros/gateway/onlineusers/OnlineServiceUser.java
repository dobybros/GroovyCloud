package com.dobybros.gateway.onlineusers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.utils.RunnableEx;
import com.alibaba.fastjson.JSON;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.channels.Channel.ChannelListener;
import com.dobybros.chat.data.userinfo.ServerInfo;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.chat.open.data.DeviceInfo;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.UserStatus;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.storage.adapters.UserInPresenceAdapter;
import com.dobybros.chat.storage.adapters.UserInfoAdapter;
import com.dobybros.chat.utils.SingleThreadQueue;
import com.dobybros.chat.utils.SingleThreadQueue.BulkHandler;
import com.dobybros.gateway.channels.data.OutgoingMessage;
import com.dobybros.gateway.channels.data.Result;
import com.dobybros.gateway.channels.tcp.TcpChannel;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.onlineusers.PushInfo.SpecialHandler;
import com.dobybros.gateway.pack.Pack;
import com.dobybros.gateway.utils.FreezableQueue;
import com.dobybros.gateway.utils.RecentTopicMap;
import com.docker.script.BaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.server.OnlineServer;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.memodb.ObjectId;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OnlineServiceUser implements ChannelListener {
    private String service;
    private Integer serviceVersion;
    private OnlineUser onlineUser;
    private boolean mobileQuiet = false;
    protected long noChannelTime = System.currentTimeMillis();
    //	protected ConcurrentHashMap<String, PushInfo> waitClientACKMessageMap;
    protected FreezableQueue waitClientACKMessageQueue;
    protected RecentTopicMap recentTopicMap;

    protected boolean waitClientACKMessageInitialized = false;

    /**
     * Session id is managed by our code. multiple channels are sharing the same session id.
     */
    protected String sessionId;
    /**
     * Record session's active time. mainly for session expiration.
     */
    protected Long activeTime;
    protected int status = STATUS_NEW;
    public static final int STATUS_NEW = 0;
    public static final int STATUS_CREATED = 10;
    public static final int STATUS_INITED = 20;
    public static final int STATUS_DESTROYED = 30;

    protected static final String TAG = "OnlineServiceUser";

    /**
     * Key is terminal string.
     */
    private ConcurrentHashMap<Integer, Channel> channelMap = new ConcurrentHashMap<>();
    private final int[] noChannelTimeLock = new int[0];
    /**
     * OnlineUser thread will take the event from this queue.
     */
    protected final ConcurrentLinkedQueue<PushInfo> eventQueue = new ConcurrentLinkedQueue<>();
    protected RunnableEx eventReceivingThread;

    protected SingleThreadQueue<PushInfo> acuEventQueue;

    public Integer getUnreadCount() {
        return userInfo.getOfflineUnreadCount();
    }

    public void setUnreadCount(Integer unreadCount) {
        userInfo.setOfflineUnreadCount(unreadCount);
    }

    public OnlineServiceUser() {
    }

    public String userInfo(Integer terminal) {
        if (userInfo != null) {
            return userInfo.getUserId() + "@" + terminal + "|" + userInfo.getService();
        }
        return null;
    }

    public String userInfo() {
        if (userInfo != null) {
            return userInfo.getUserId() + "|" + userInfo.getService();
        }
        return null;
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
        if (status < STATUS_INITED) {
            acuEventQueue = new SingleThreadQueue<>(userInfo() + " event receiving thread. sid " + sessionId, eventQueue, ServerStart.getInstance().getGatewayThreadPoolExecutor(), new BulkHandler<PushInfo>() {
                public boolean bulkHandle(ArrayList<PushInfo> pushInfoList) {
                    pushToChannelsSync(pushInfoList);
//					Event event = pushInfo.event;
//					if(event != null) {
//						if(event.getEventType() == Event.EVENTTYPE_STOPLOOP)
//							return false;
////						eventReceivedHandler(event);
//						pushToChannelsSync(event, pushInfo.excludeTerminal);
//					} 
                    return true;
                }

                @Override
                public void error(PushInfo pushInfo, Throwable t) {
                    LoggerEx.error(TAG, userInfo() + "Event " + pushInfo.getEvent() + " sending failed, " + t.getMessage());
                }

                @Override
                public void closed() {
                    LoggerEx.info(TAG, userInfo() + " stopped receiving events, status " + status);
                }

                @Override
                public boolean validate() {
                    return status >= STATUS_INITED && status < STATUS_DESTROYED;
                }
            });
            waitClientACKMessageQueue = new FreezableQueue();
            waitClientACKMessageQueue.setAcuEventQueue(acuEventQueue);
            waitClientACKMessageQueue.setOfflineMessageSavingTask(onlineUser.getOfflineMessageSavingTask());
            waitClientACKMessageQueue.setOnlineUser(this);

            //初始化最近消息map
            recentTopicMap = new RecentTopicMap();
            recentTopicMap.init();

            init();
            status = STATUS_INITED;
            if (!acuEventQueue.isEmpty()) {
                acuEventQueue.start();
            }
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Long activeTime) {
        this.activeTime = activeTime;
    }

    private boolean handlePushInfo(PushInfo pushInfo) {
        SpecialHandler handler = pushInfo.getHandler();
        if (handler != null) {
            try {
                handler.handle();
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, userInfo() + " special handler handle failed, " + t.getMessage());
            }
            return true;
        }
        return false;
    }

    public final void pushToChannelsSync(Collection<PushInfo> pushInfoList) {
        if (pushInfoList != null && !pushInfoList.isEmpty()) {
            for (PushInfo pushInfo : pushInfoList) {
                if (!handlePushInfo(pushInfo)) {
                    Data outData = null;
                    Data data = pushInfo.getData();
                    Message message = pushInfo.getEvent();
                    if (data != null)
                        outData = data;
                    else if (message != null) {
                        OutgoingMessage outMessage = new OutgoingMessage();
                        outMessage.fromMessage(message);
                        outData = outMessage;
                    }
                    Integer excludeTerminal = pushInfo.getExcludeTerminal();
                    if (channelMap != null && outData != null) {
                        if (outData.getType() == Pack.TYPE_OUT_OUTGOINGMESSAGE && true == ((OutgoingMessage) outData).getNeedAck()) //ResultEvent don't need client ACK ，no need save offline message don't need ACK
                            waitClientACKMessageQueue.add(pushInfo);
                        pushToChannelsSync(outData, pushInfo.getExcludeTerminal(), pushInfo.getToTerminal());
                    }
                }
            }
        }
    }

    protected void pushToChannelsSync(Data event, Integer excludeTerminal, Integer toTerminal) {
        if (channelMap != null && event != null) {
            BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
            if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
                ((GatewayGroovyRuntime) runtime).messageSent(event, excludeTerminal, toTerminal, userInfo.getUserId(), service);
            }
            if (toTerminal != null) {
                Channel channel = getChannel(toTerminal);
                if (channel != null)
                    channel.send(event);
            } else {
                Set<Entry<Integer, Channel>> entries = channelMap.entrySet();
                for (Entry<Integer, Channel> entry : entries) {
                    if (excludeTerminal == null || !excludeTerminal.equals(entry.getKey())) {
                        Channel channel = entry.getValue();
                        channel.send(event);
                    }
                }
            }
        }
    }

    public void pushToCrossServer(Message message, List<Integer> toTerminals) {}

    public final void pushToChannels(Data event, Integer excludeTerminal) {
        pushToChannels(event, excludeTerminal, null);
    }

    public final void pushToChannels(Data event, Integer excludeTerminal, Integer toTerminal) {
        if (status >= STATUS_INITED && status < STATUS_DESTROYED)
            acuEventQueue.offerAndStart(new PushInfo(event, excludeTerminal, toTerminal));
        else {
            acuEventQueue.offer(new PushInfo(event, excludeTerminal, toTerminal));
            LoggerEx.error(TAG, "Try to push message " + event + " while status is not ready " + status);
        }
    }

    //	private int[] onlineUserThreadLock = new int[0];
//	private boolean userThreadIsWorking = false;
//	private Long userThreadStartTakes = null;
    public int eventReceived(Message event) {
//		AcuLogger.info(TAG, logWho() + " receive event, " + event.getEventType() + " targetId " + event.getFilterMatch(Event.FILTERMATCH_TARGETID));
//		eventQueue.offer(event);
//		handleEvent();
        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime) runtime).messageReceivedFromUsers(event, onlineUser.getUserId(), service);
        }
        return eventReceivedHandler(event);
//		acuEventQueue.offer(event);
    }

//	private void handleEvent() {
//		synchronized (onlineUserThreadLock) {	
//			if(!userThreadIsWorking) {
//				userThreadIsWorking = true;
//				userThreadStartTakes = System.currentTimeMillis();
//				ServerStart.getInstance().getThreadPool().execute(eventReceivingThread);
//			}
//		}		
//	}

//	protected abstract boolean eventReceivedHandler(Message event);

    public Channel addChannel(Channel channel) {
		/*if(channelMap == null) {
//			initOnlineUser();
			channelMap = new ConcurrentHashMap<>();
		}*/
        Channel oldChannel = channelMap.put(channel.getTerminal(), channel);
        return oldChannel;
    }

    public void removeChannel(Channel channel, int close) {
        if (channel == null)
            return;
        Result resultEvent = new Result();
        switch (close) {
            case ChannelListener.CLOSE_KICKED:
            case ChannelListener.CLOSE_SWITCHCHANNEL:
                resultEvent.setCode(GatewayErrorCodes.ERROR_USER_KICKED_BY_LOGIN_OTHER_DEVICE);
                resultEvent.setDescription("User kicked out because of login on other terminal");
                channel.send(resultEvent);
                break;
            case ChannelListener.CLOSE_FORBIDDEN:
                resultEvent.setCode(GatewayErrorCodes.ERROR_USER_KICKED_BY_FORBIDDEN);
                resultEvent.setDescription("User has been forbiddened and kicked out.");
                channel.send(resultEvent);
                break;
        }
//		if(close == ChannelListener.CLOSE_KICKED || close == ChannelListener.CLOSE_SWITCHCHANNEL)	 {	//当因为登录另台不同terminal的设备导致的关闭时，需要发送推送事件
//			ResultEvent resultEvent = new ResultEvent();
//			resultEvent.setSequence(System.currentTimeMillis());
//			resultEvent.setId(ObjectId.get().toString());
//			resultEvent.setCode(GatewayErrorCodes.ERROR_USER_KICKED_BY_LOGIN_OTHER_DEVICE);
//			resultEvent.setDescription("User kicked out because of login on other terminal");
//			channel.send(resultEvent);
//		}
//		if(close == ChannelListener.CLOSE_FORBIDDEN) {
//			ResultEvent resultEvent = new ResultEvent();
//			resultEvent.setSequence(System.currentTimeMillis());
//			resultEvent.setId(ObjectId.get().toString());
//			resultEvent.setCode(GatewayErrorCodes.ERROR_USER_KICKED_BY_FORBIDDEN);
//			resultEvent.setDescription("User has been forbiddened and kicked out.");
//			channel.send(resultEvent);
//		}

        // 如果关闭通道的方式是kick、切换、登出，删除presence上的device
        if (close == ChannelListener.CLOSE_KICKED || close == ChannelListener.CLOSE_SWITCHCHANNEL || close == ChannelListener.CLOSE_LOGOUT) {
            List<Integer> terminals = new ArrayList<>();
            terminals.add(channel.getTerminal());
            deleteDevice(terminals);
        }

        //TODO 根据closeType判断是否需要发送登出的事件, CLOSE_KICKED. 用sendResult发出事件
        if (channelMap != null && channelMap.containsValue(channel)) {
            channel = channelMap.remove(channel.getTerminal());
            if (channel != null) {
                boolean bool = channel.close(close);
            }
            return;
        } else {
            channel.close(close);
        }
    }

    public void deleteDevice(List<Integer> terminals) {
//		DeviceDeleteRequest deleteRequest = new DeviceDeleteRequest();
//		deleteRequest.setService(userInfo.getService());
//		deleteRequest.setTerminals(terminals);
//		deleteRequest.setUserId(userInfo.getUserId());
//		RPCClientAdapter balancerHandler = onlineUser.getOnlineServer().getBalancerHandler();
//		if(balancerHandler == null)
//			LoggerEx.fatal(TAG, "Balancer is not ready yet, RPCClientAdapter is null, userInfo : " + userInfo);
//		else {
//			try {
//				DeviceDeleteResponse deviceDeleteResponse = (DeviceDeleteResponse) balancerHandler.call(deleteRequest);
//			} catch (CoreException e) {
//				e.printStackTrace();
//				LoggerEx.fatal(TAG, "delete device by deviceDeleteRequest, userInfo : " + userInfo);
//			}
//		}

        // todo 改造
        UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class);
        try {
            userInfoAdapter.deleteDevices(userInfo.getUserId(), userInfo.getService(), terminals);
        } catch (CoreException e) {
            e.printStackTrace();
            LoggerEx.fatal(TAG, "delete device by userInfoAdapter, userInfo : " + userInfo);
        }
        if (userInfo != null) {
            Map<Integer, DeviceInfo> map = userInfo.getDevices();
            if (map != null) {
                for (Integer ter : terminals) {
                    map.remove(ter);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("hello");
    }

    public Channel getChannel(Integer terminal) {
        if (channelMap != null) {
            return channelMap.get(terminal);
        }
        return null;
    }

    public boolean isChannelEmpty() {
        return channelMap.isEmpty();
    }

    public Collection<Channel> getActiveChannels() {
        if (channelMap == null)
            return null;
        return channelMap.values();
    }

    public void destroySelf(int close) {
        if (status < STATUS_DESTROYED) {
            if (channelMap != null) {
                for (Channel channel : channelMap.values()) {
                    removeChannel(channel, close);
                }
                channelMap.clear();
//				channelMap = null;
            }
            try {
                userDestroyed(close);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            status = STATUS_DESTROYED;

//			eventReceived(new StopLoopEvent());
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//	public abstract void setToken(String terminal, DeviceToken deviceToken) throws CoreException;
//	public abstract void setTokenWithSendMessage(String terminal, String token) throws CoreException;

    private UserInfo userInfo;

    public String description() {
        StringBuilder buffer = new StringBuilder();
        if (userInfo != null) {
            buffer.append("service: ");
            buffer.append(userInfo.getService());
            buffer.append("|uid: ");
            buffer.append(userInfo.getUserId());
        } else {
            buffer.append("User is null");
        }
        buffer.append("|sid: ");
        buffer.append(sessionId);
        buffer.append("|waitMsgMap: ");
        buffer.append(waitClientACKMessageQueue.description());
        buffer.append("\r\n");

        buffer.append("chanels: ");
        Collection<Channel> channels = getActiveChannels();
        if (channels != null) {
            for (Channel channel : channels) {
                buffer.append(channel.getTerminal());
                buffer.append("_v");
                buffer.append(channel.getVersion());
                buffer.append("|");
            }
//			buffer.append("\r\nDeviceToken: " + getDeviceToken() + " ");
        }
        buffer.append("\r\n");
        buffer.append("\r\n");

        if (eventReceivingThread != null) {
            buffer.append("eventReceivingThread: ");
            buffer.append(eventReceivingThread.toString());
            buffer.append("\r\n");
        }
        return buffer.toString();
    }

    public void userCreated() {
        if (sessionId == null)
            sessionId = ObjectId.get().toString();
        activeTime = System.currentTimeMillis();

        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime) runtime).sessionCreated(userInfo.getUserId(), service);
        }
    }

    public void setToken(Integer terminal, DeviceInfo deviceInfo) throws CoreException {
        if (userInfo != null) {
            LoggerEx.info(TAG, "setToken terminal " + terminal + " deviceInfo " + deviceInfo + " on thread " + Thread.currentThread());
            //token是否更新
            boolean updateToken = false;

            Map<Integer, DeviceInfo> devices = userInfo.getDevices();
            if (devices == null) {
                devices = new ConcurrentHashMap<>();
                userInfo.setDevices(devices);
                updateToken = true;
            }
            DeviceInfo old = devices.get(terminal);
            if (old == null) {
                devices.put(terminal, deviceInfo);
                if (!updateToken)
                    updateToken = true;
            } else if (!old.getDeviceToken().equals(deviceInfo.getDeviceToken())) {
                devices.put(terminal, deviceInfo);
                if (!updateToken)
                    updateToken = true;
            } else if (deviceInfo.getLocale() != null && (old.getLocale() == null || (!old.getLocale().equals(deviceInfo.getLocale())))) {
                devices.put(terminal, deviceInfo);
                if (!updateToken)
                    updateToken = true;
            }
            if (updateToken) {
                deviceInfo.setLoginTime(System.currentTimeMillis());
                UserInfo updateUser = new UserInfo();
                updateUser.setUserId(userInfo.getUserId());
                updateUser.setService(userInfo.getService());
                Map<Integer, DeviceInfo> upDevices = new HashMap<>();
                upDevices.put(terminal, deviceInfo);
                updateUser.setDevices(upDevices);
                UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class);
                userInfoAdapter.updateUserInfo(updateUser);
                LoggerEx.info(TAG, "setToken terminal " + terminal + " deviceInfo " + deviceInfo + " updated on thread " + Thread.currentThread());
                // todo 改造
//				DeviceUpdateRequest deviceUpdateRequest = new DeviceUpdateRequest();
//				deviceUpdateRequest.setUserId(userInfo.getUserId());
//				deviceUpdateRequest.setService(userInfo.getService());
//				deviceUpdateRequest.setDeviceToken(deviceInfo.getDeviceToken());
//				deviceUpdateRequest.setTerminal(deviceInfo.getTerminal());
//				deviceUpdateRequest.setTime(deviceInfo.getLoginTime());
//                if (StringUtils.isNotBlank(deviceInfo.getLocale()))
//                    deviceUpdateRequest.setLocale(deviceInfo.getLocale());
//
//				RPCClientAdapter balancerHandler = onlineUser.getOnlineServer().getBalancerHandler();
//				if(balancerHandler == null)
//					throw new CoreException(ChatErrorCodes.ERROR_BALANCER_NOT_READY, "Balancer is not ready yet");
//				DeviceUpdateResponse deviceUpdateResponse = (DeviceUpdateResponse) balancerHandler.call(deviceUpdateRequest);
            }
        }
    }

    public void init() {
        if (userInfo != null) //Already initialized.
            return;
        LoggerEx.debug(TAG, "User " + userInfo.getUserId() + " initialized!");
    }

    public synchronized void userDestroyed(int close) {
        //优化， 如果userInfo没有变化不用在写到数据库中
        if (userInfo == null) {
            LoggerEx.info(TAG, "userDestroyed userInfo is null, sessionId" + sessionId + " service " + service + " close " + close);
            return;
        }
        LoggerEx.info(TAG, "userDestroyed userInfo is " + userInfo + ", sessionId" + sessionId + " service " + service + " close " + close);
//		userInfo.setServer("");
//		userInfo.setServerInfo(new ServerInfo());
        try {
            UserInfoAdapter userInfoAdapter = StorageManager.getInstance().getStorageAdapter(UserInfoAdapter.class);
            ServerInfo serverInfo = userInfoAdapter.getServerInfo(userInfo.getUserId(), userInfo.getService());
            if (serverInfo != null && serverInfo.getServer().equals(OnlineServer.getInstance().getServer())) {
                userInfoAdapter.deleteServerInfo(userInfo.getUserId(), userInfo.getService());
            } else {
                LoggerEx.warn(TAG, "Online Service User " + JSON.toJSONString(userInfo) + " is destroyed but server is not expected, serverInfo " + (serverInfo != null ? JSON.toJSONString(serverInfo) : "null"));
            }
        } catch (CoreException e) {
            e.printStackTrace();
            LoggerEx.fatal(TAG, "Update userInfo failed, userInfo : " + userInfo);
        }

        if (waitClientACKMessageQueue != null)
            waitClientACKMessageQueue.shutdown();

        if (recentTopicMap != null)
            recentTopicMap.destroy();

        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime) runtime).sessionClosed(userInfo.getUserId(), service, close);
        }

        switch (close) {
            case ChannelListener.CLOSE_LOGOUT:
                List targetIds = new ArrayList<String>();
                targetIds.add(userInfo.getUserId());
                UserInPresenceAdapter userInPresenceAdapter = StorageManager.getInstance().getStorageAdapter(UserInPresenceAdapter.class);
                try {
                    userInPresenceAdapter.deleteMany(targetIds);
                } catch (CoreException e) {
                    e.printStackTrace();
                    LoggerEx.fatal(TAG, userInfo() + " go logout failed, " + e.getMessage());
                }
                break;
        }

        // todo 改造

		/*RPCClientAdapter balancerHandler = onlineUser.getOnlineServer().getBalancerHandler();
		switch (close) {
		case ChannelListener.CLOSE_LOGOUT:
			LogoutRequest logoutRequest = new LogoutRequest();
			logoutRequest.setUserId(userInfo.getUserId());
			if(balancerHandler != null) {
				try {
					LogoutResponse logoutResponse = (LogoutResponse) balancerHandler.call(logoutRequest);
				} catch (CoreException e) {
					e.printStackTrace();
					LoggerEx.fatal(TAG, userInfo() + " go logout failed, " + e.getMessage());
				}
			}
			break;

		default:
			OfflineRequest offOnlineRequest = new OfflineRequest();
			offOnlineRequest.setUserId(userInfo.getUserId());
			offOnlineRequest.setService(service);
//			if(userInfo != null) {
//				Map<Integer, DeviceInfo> deviceTokenMap = userInfo.getDevices();
//				if(deviceTokenMap != null) {
//					DeviceInfo token = deviceTokenMap.get(DeviceInfo.TERMINAL_IOS);
//					if(token != null) {
//						String deviceToken = token.getDeviceToken();
//						if(deviceToken != null) {
////							offOnlineRequest.setDeviceToken(deviceToken);
//							offOnlineRequest.setTerminal(DeviceInfo.TERMINAL_IOS);
//							offOnlineRequest.setUnread(unreadCount);
//						}
//					}
//				}
//			}
			offOnlineRequest.setUnread(unreadCount);
			if(balancerHandler != null) {
				try {
					OfflineResponse offOnlineResponse = (OfflineResponse) balancerHandler.call(offOnlineRequest);
				} catch (CoreException e) {
					e.printStackTrace();
					LoggerEx.fatal(TAG, userInfo() + " go offline failed, " + e.getMessage());
				}
			}
			break;
		}*/

        userInfo = null;

        LoggerEx.info(TAG, "userDestroyed finished userInfo is " + userInfo + ", sessionId" + sessionId + " service " + service + " close " + close);
    }

    private ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");

    protected final int eventReceivedHandler(Message event) {
        long time = System.currentTimeMillis();
        try {
            if (event != null) {//处于冻结状态下就不会将消息推倒终端。 但是需要处理APN消息推送。
                boolean isFrozen = waitClientACKMessageQueue.isFrozen();
                if (isFrozen)
                    return OnlineUser.RECEIVED_FROZEN;

                if (isChannelEmpty()) {
//					String service = event.getService();
                    String service = userInfo.getService();
                    if (service != null) {
                        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
                        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
                            //TODO Use latest device to handle message not received case.
                            //But should use all not online device to handle the not received case.
                            //Business layer decide how to handle with those offline devices. not here.
//							DeviceInfo latestDevice = userInfo.getLatestLoginDevice();
                            UserStatus userStatus = new UserStatus();
                            if (userInfo.getOfflineUnreadCount() == null)
                                this.setUnreadCount(0);
                            this.setUnreadCount(userInfo.getOfflineUnreadCount() + 1);
                            userStatus.setOfflineUnreadCount(userInfo.getOfflineUnreadCount());
                            userStatus.setService(userInfo.getService());
                            userStatus.setUserId(userInfo.getUserId());
                            userStatus.setLanId(OnlineServer.getInstance().getLanId());
                            userStatus.setDeviceInfoMap(new HashMap<>(userInfo.getDevices()));
                            Map<String, UserStatus> map = new HashMap<>();
                            map.put(userStatus.getUserId(), userStatus);
                            ((GatewayGroovyRuntime) runtime).messageNotReceived(event, map);
                        }
                    }
                }
                boolean intercepted = false;
                BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
                if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
                    intercepted = ((GatewayGroovyRuntime) runtime).shouldInterceptMessageReceivedFromUsers(event, onlineUser.getUserId(), service);
                }
                if (intercepted) {
                    OutgoingMessage out = new OutgoingMessage();
                    out.fromMessage(event);
                    pushToChannels(out, null);
                    LoggerEx.info(TAG, userInfo() + " handle event, " + event + " takes " + (System.currentTimeMillis() - time));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OnlineUser.RECEIVED;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userStatus) {
        this.userInfo = userStatus;
    }

    public DeviceInfo getDeviceInfo(Integer terminal) {
        if (userInfo != null) {
            Map<Integer, DeviceInfo> map = userInfo.getDevices();
            if (map != null) {
                return map.get(terminal);
            }
        }
        return null;
    }

    public void channelClosed(Channel channel, int close) {
        switch (close) {
            case ChannelListener.CLOSE_CHANNELEXPIRED:
//			if(hasIOSDeviceToken()) {
//				if(channel instanceof HttpChannel) {
//					HttpChannel httpChannel = (HttpChannel) channel;
//					ConcurrentLinkedQueue<Message> eventQueue = httpChannel.getEventQueue();
//					if(eventQueue != null) {
//						Message eventInQueue;
//						while ((eventInQueue = eventQueue.poll()) != null) {
//							if(eventInQueue instanceof AddTopicEvent) {
//								handlePushNotification((AddTopicEvent) eventInQueue);
//								break;
//							}
//						}
//					}
//				}
//			}
                break;
            case ChannelListener.CLOSE_LOGOUT:
            case ChannelListener.CLOSE_KICKED:
            case ChannelListener.CLOSE_SWITCHCHANNEL:
//			if(channel.getTerminal().equals(DeviceInfo.TERMINAL_IOS)) {
//				if(userInfo != null) {
//					Map<Integer, DeviceInfo> map = userInfo.getDevices();
//					if(map != null) {
//						DeviceInfo deviceToken = map.get(DeviceInfo.TERMINAL_IOS);
//						if(deviceToken != null && deviceToken.getDeviceToken() != null) {
//							try {
////								userStatusService.deleteDeviceToken(getUser().getId(), deviceToken.getDeviceToken());
//								map.remove(DeviceInfo.TERMINAL_IOS);
//							} catch (Exception e) {
//								e.printStackTrace();
//								LoggerEx.error(TAG, "Delete device token " + deviceToken.getDeviceToken() + " for iOS user " + userInfo.getUserId() + " failed, " + e.getMessage());
//							}
//						}
//					}
//				}
//			}
                if (userInfo != null) {
                    Map<Integer, DeviceInfo> map = userInfo.getDevices();
                    if (map != null) {
                        map.remove(channel.getTerminal());
                    }
                }
                break;
        }
        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime) runtime).channelClosed(userInfo.getUserId(), service, channel.getTerminal(), close);
        }
        if (channelMap.isEmpty()) {
            synchronized (noChannelTimeLock) {
                if (channelMap.isEmpty()) {
                    noChannelTime = System.currentTimeMillis();
                }
            }
        }
    }

    public void channelCreated(Channel channel) {
//		if(waitClientACKMessageMap != null && !waitClientACKMessageMap.isEmpty()) {
//			ArrayList<PushInfo> pushInfoList = new ArrayList<>(waitClientACKMessageMap.values());
//			Collections.sort(pushInfoList);
//			pushToChannelsSync(pushInfoList, true);
//		}
        if (waitClientACKMessageQueue != null) {
            if (!waitClientACKMessageInitialized) {
                waitClientACKMessageInitialized = true;
                waitClientACKMessageQueue.init();
            } else {
//				pushToChannelsSync(waitClientACKMessageQueue.values(), true);
                waitClientACKMessageQueue.flush();
                if (!waitClientACKMessageQueue.isFrozen()) {
                    //建立通道后，发送事件使客户端解除静音状态
//					OfflineMessagesReceivedEvent offlineMessagesReceivedEvent = new OfflineMessagesReceivedEvent();
//					pushToChannels(offlineMessagesReceivedEvent, null);
//					ResultEvent resultEvent = new ResultEvent();
//					resultEvent.setCode(FreezableQueue.OFFLINE_MESSAGE_RECEIVED_CODE);
                    //通过Result发到客户端的11， 告诉客户端离线消息已经加载完成了
                    Result data = new Result();
                    data.setCode(Result.OFFLINE_MESSAGE_RECEIVED_CODE);

                    pushToChannels(data, null, channel.getTerminal());
                    LoggerEx.info(TAG, userInfo() + " send offlineMessagesReceivedEvent while channel created.");
                }
            }
        }
        BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
        if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime) runtime).channelCreated(userInfo.getUserId(), service, channel.getTerminal());
        }
        if (!channelMap.isEmpty()) {
            synchronized (noChannelTimeLock) {
                if (!channelMap.isEmpty()) {
                    noChannelTime = -1;
                }
            }
        }
    }

    //
//	public boolean hasIOSDeviceToken() {
//		Map<Integer, DeviceInfo> map = userInfo.getDevices();
//		if(map != null && !map.isEmpty()) {
//			DeviceInfo deviceToken = map.get(DeviceInfo.TERMINAL_IOS);
//			return deviceToken != null;
//		}
//		return false;
//	}
//
    public static interface TopicSendHandler {
        public void send(Message topic) throws CoreException;
    }

    public Data sendTopic(Message topic, boolean needTcpResult, TopicSendHandler topicSendHandler) {
        Result resultEvent;
        if (topic.getClientId() != null) {
            resultEvent = recentTopicMap.getExistEvent(topic.getClientId());
            if (resultEvent != null) {
                pushToChannels(resultEvent, null);
                return resultEvent;
            }
        }
        topic.setId(ObjectId.get().toString());
        if (topic.getTime() == null)
            topic.setTime(System.currentTimeMillis());
        topic.setUserId(userInfo.getUserId());

        Result errorResultEvent = null;
        if (topicSendHandler != null) {
            try {
                topicSendHandler.send(topic);
            } catch (Throwable e) {
                errorResultEvent = new Result();
                if (e instanceof CoreException) {
//					errorResultEvent.setDescription("Send message failed, error code : " + ((CoreException)e).getCode() + ", description : " + e.getMessage());
                    errorResultEvent.setCode(((CoreException) e).getCode());
                } else {
//					errorResultEvent.setDescription("Send message failed.");
                    e.printStackTrace();
                    LoggerEx.error(TAG, "Send message " + topic + " failed, " + e.getMessage());
                    errorResultEvent.setCode(IMCoreErrorCodes.ERROR_SEND_MESSAGE_FAILED);
                }
                errorResultEvent.setForId(topic.getClientId());
            }
        }

        if (errorResultEvent != null) {
            resultEvent = errorResultEvent;
        } else {
//			Document result = new Document();
//			result.put("id", topic.getId());
//			result.put("type", topic.getType());
////		result.put(Topic.FIELD_TOPIC_CLIENTID, topic.getClientId());
//			result.put(Topic.FIELD_TOPIC_CREATETIME, topic.getCreateTime());
//			
//			resultEvent = new ResultEvent();
//			resultEvent.setSequence(System.currentTimeMillis());
//			resultEvent.setId(ObjectId.get().toString());
//			resultEvent.setCode(ResultEvent.CODE_SUCCESS);
//			resultEvent.setReply(Short.toString(HailPack.TYPE_IN_RECEIVEMESSAGE));
//			resultEvent.setDescription(null);
//			resultEvent.setRelateId(topic.getClientId());
//			resultEvent.setResult(result);

            resultEvent = new Result();
            resultEvent.setCode(Result.CODE_SUCCESS);
            resultEvent.setForId(topic.getClientId());
            resultEvent.setServerId(topic.getId());
            resultEvent.setTime(topic.getTime());
        }
        if (needTcpResult)
            pushToChannels(resultEvent, null);
        if (errorResultEvent == null) {
            recentTopicMap.add(resultEvent);
        }
        return resultEvent;
    }

    public void clientAckMessageIds(Set<String> ids) {
        if (waitClientACKMessageQueue != null && ids != null)
            for (String id : ids) {
                waitClientACKMessageQueue.remove(id);
            }
    }

    public void sent(Message event) {
        //TODO tmp code before client really send client ack for the message is received.
//		String offlineMessageId = event.getFromOfflineMessageId();
//		if(offlineMessageId != null)
//			receivedOfflineMessageIds.add(offlineMessageId);
//		LoggerEx.info(TAG, userInfo() + "'s message has sent " + event + " from offlineMessageId " + offlineMessageId);
    }

    public void exceptionCaught(Throwable cause) {
        LoggerEx.error(TAG, userInfo() + "'s tcp channel occur error in exceptionCaught,errMsg: " + ExceptionUtils.getFullStackTrace(cause));
    }

    /////////////impl

    public FreezableQueue getWaitClientACKMessageQueue() {
        return waitClientACKMessageQueue;
    }

    public void setWaitClientACKMessageQueue(
            FreezableQueue waitClientACKMessageQueue) {
        this.waitClientACKMessageQueue = waitClientACKMessageQueue;
    }

    public RecentTopicMap getRecentTopicMap() {
        return recentTopicMap;
    }

    public void setRecentTopicMap(RecentTopicMap recentTopicMap) {
        this.recentTopicMap = recentTopicMap;
    }

    public OnlineUser getOnlineUser() {
        return onlineUser;
    }

    public void setOnlineUser(OnlineUser onlineUser) {
        this.onlineUser = onlineUser;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceAndVersion() {
        return this.getService() + (this.getServiceVersion() == null ? "" : ("_v" + this.getServiceVersion()));
    }

    public Integer getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(Integer serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public void sent(Data data) {

    }

    private Long maxInactiveIntervalCache = null;

    public Long getMaxInactiveInterval() {
        if (maxInactiveIntervalCache == null && userInfo != null && service != null) {
            BaseRuntime runtime = scriptManager.getBaseRuntime(getServiceAndVersion());
            if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
                maxInactiveIntervalCache = ((GatewayGroovyRuntime) runtime).getMaxInactiveInterval(userInfo.getUserId(), service);
                if (maxInactiveIntervalCache == null) {
                    maxInactiveIntervalCache = ((GatewayGroovyRuntime) runtime).getIMConfig(userInfo.getUserId(), service).getMaxInactiveInterval();
                }
            }
        }
        return maxInactiveIntervalCache;
    }

    public String getIp(Integer terminal) {
        if (terminal != null) {
            TcpChannel channel = (TcpChannel) getChannel(terminal);
            if (channel != null) {
                return channel.getIp();
            }
        }
        return null;
    }

    public ConcurrentHashMap<Integer, Channel> getChannelMap() {
        return channelMap;
    }

    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    public long getNoChannelTime() {
        return noChannelTime;
    }
}
