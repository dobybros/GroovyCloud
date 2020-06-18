package com.dobybros.gateway.onlineusers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.IteratorEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.channels.Channel.ChannelListener;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.utils.SingleThreadQueue;
import com.dobybros.chat.utils.SingleThreadQueue.Handler;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.docker.onlineserver.OnlineServerWithStatus;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class OnlineUserManager {
    private static final String TAG = "OUM";
    @Resource
    protected OnlineServerWithStatus onlineServer;
    @Resource
    private OfflineMessageSavingTask offlineMessageSavingTask;

    private Class<? extends OnlineUser> adminOnlineUserClass;        //admin

    private UserExpireTimer expireTimerTask = new UserExpireTimer();

    private ConcurrentHashMap<String, SingleThreadQueue<EventEntity>> queueMap = new ConcurrentHashMap<>();
    private final Long maxNoChannelTimeout = TimeUnit.MINUTES.toMillis(5);
    /**
     * String is userid
     */
//	private ConcurrentHashMap<String, OnlineUser> onlineUserSessionMap = new ConcurrentHashMap<>();

    private OnlineUsersHolder onlineUserHolder;

//	private int inactiveCheckPeriod = 60;


    private class UserExpireTimer extends TimerTaskEx {
        private final String TAG = UserExpireTimer.class.getSimpleName();

        @Override
        public void execute() {
            if (onlineUserHolder != null) {
                for (OnlineUser user : onlineUserHolder.onlineUsers()) {
                    ConcurrentHashMap<String, OnlineServiceUser> serviceUserMap = user.getServiceUserMap();
                    Collection<OnlineServiceUser> values = serviceUserMap.values();
                    for (OnlineServiceUser serviceUser : values) {
                        long noChannelTime = serviceUser.getNoChannelTime();
                        if (noChannelTime != -1) {
                            if (/*!user.keepOnline() && */System.currentTimeMillis() - noChannelTime > maxNoChannelTimeout) {
                                try {
                                    UserInfo info = serviceUser.getUserInfo();
                                    deleteOnlineServiceUser(serviceUser, ChannelListener.CLOSE_USEREXPIRED);
                                    LoggerEx.info(TAG, "User " + info.getUserId() + "|" + info.getService() + " expired");
                                } catch (Throwable e) {
                                    LoggerEx.error(TAG, "User " + user + " onDestroy(DestroyType.Expired) failed, " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * OnlineUserManager thread will take the event from this queue for sending
     */
//	protected final LinkedBlockingQueue<EventEntity> eventQueue = new LinkedBlockingQueue<>();
//	protected AcuRunnable eventSendingThread;

    /**
     * 由于使用每个人的userId作为队列标示， 相当于发送消息对于一个人是一个队列操作。
     * 这个类就是这个队列的处理者。
     *
     * @author aplomb
     */
    class MessageHandler extends Handler<EventEntity> {
        @Override
        public boolean handle(EventEntity ee) throws CoreException {
            if (ee != null && ee.event != null) {
//				if(ee.event.getEventType() == Event.EVENTTYPE_STOPLOOP)
//					return false;
//				AcuLogger.info(TAG, getQueue().getDescription() + " OnlineUserManager handle event, type " + ee.event.getEventType() + (ee.excludedUser != null ? "excludedUser " + ee.excludedUser.getUserId() : "") + " targetId " + ee.event.getFilterMatch(Event.FILTERMATCH_TARGETID));
                sendEventToOnlineUsers(ee.event, ee.excludedUser);
            }
            return true;
        }

        @Override
        public void error(EventEntity message, Throwable t) {
            LoggerEx.error(TAG, getQueue().getDescription() + "Message sending failed, " + t.getMessage());
        }

        @Override
        public void closed() {
            LoggerEx.info(TAG, getQueue().getDescription() + " stopped receiving events");
        }

        @Override
        public boolean validate() {
            return true;
        }
    }
	/*class MessageHandler extends BulkHandler<EventEntity> {
		@Override
		public boolean bulkHandle(ArrayList<EventEntity> bulk) {
			for(EventEntity ee : bulk) {
				//combine addTopicEvent to updateTopicEntityEvent with latestSequence.
			}
			if(ee != null && ee.event != null) {
				if(ee.event.getEventType() == Event.EVENTTYPE_STOPLOOP)
					return false;
				AcuLogger.info(TAG, getQueue().getDescription() + " OnlineUserManager handle event, type " + ee.event.getEventType() + (ee.excludedUser != null ? "excludedUser " + ee.excludedUser.getUserId() : "") + " targetId " + ee.event.getFilterMatch(Event.FILTERMATCH_TARGETID));
				sendEventToOnlineUsers(ee.event, ee.excludedUser);
			}
			return true;
		}
		@Override
		public void error(EventEntity message, Throwable t) {
			AcuLogger.error(TAG, getQueue().getDescription() + "Event sending failed, " + t.getMessage());
		}
		@Override
		public void closed() {
			AcuLogger.info(TAG, getQueue().getDescription() + " stopped receiving events");
		}
		@Override
		public boolean validate() {
			return true;
		}
	}*/

    public void init() {
        onlineUserHolder = new OnlineUsersHolder();
        onlineUserHolder.setOnlineServer(onlineServer);
        onlineUserHolder.setOnlineUserManager(this);
        onlineUserHolder.init();

        TimerEx.schedule(expireTimerTask, TimeUnit.SECONDS.toMillis(10), maxNoChannelTimeout);
    }

    public OnlineUserManager() {
    }

    public OnlineUser getOnlineUser(String userId) throws CoreException {
        if (onlineUserHolder != null) {
            return onlineUserHolder.getOnlineUser(userId);
        }
        return null;
    }

    public OnlineUser addOnlineServiceUser(UserInfo user, String preSessionId) throws CoreException {
        if (user == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ILLEGAL_PARAMETER, "user can not be null");
        OnlineUser onlineUser = addOnlineUser(user);
        if (onlineUser != null) {
            OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(user.getService());
            if (serviceUser == null) {
                serviceUser = new OnlineServiceUser();
                serviceUser = onlineUser.addOnlineServiceUser(user.getService(), user, preSessionId, serviceUser);
            }
        }
        return onlineUser;
    }

    public OnlineUser addOnlineUser(UserInfo user) throws CoreException {
        if (user == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ILLEGAL_PARAMETER, "user can not be null");
        String userId = user.getUserId();
        OnlineUser onlineUser = onlineUserHolder.getOnlineUser(userId);
        if (onlineUser == null) {
//				if(userId.equals(AdminOnlineUserImpl.ADMIN_ACCOUNT_ID) || userId.equals(AdminOnlineUserImpl.ALLUSERS_ACCOUNT_ID) || userId.equals(AdminOnlineUserImpl.PAIDUSERS_ACCOUNT_ID)) {
//					onlineUser = adminOnlineUserClass.newInstance();
//				} else {
//					onlineUser = onlineUserClass.newInstance();
//				}
            onlineUser = new OnlineUser();
            onlineUser.setUserId(userId);
            onlineUser.setOnlineUseManager(this);
            onlineUser.setOnlineServer(onlineServer);
            onlineUser.setOfflineMessageSavingTask(offlineMessageSavingTask);
            OnlineUser actualOnlineUser = onlineUserHolder.addOnlineUserIfAbsent(onlineUser);
            if (actualOnlineUser != null)
                onlineUser = actualOnlineUser;
            try {
                onlineUser.initOnlineUser();
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "initOnlineUser " + user + " failed, " + t.getMessage());
            }
        }
        return onlineUser;
    }

    public void shutdown() {
        LoggerEx.info(TAG, "I will shutdown!!!");
        if (onlineUserHolder != null) {
            for (String userId : onlineUserHolder.onlineUserIds()) {
                try {
                    deleteOnlineUser(userId);
                    LoggerEx.info(TAG, "Close user success, userId: " + userId);
                } catch (CoreException e) {
                    LoggerEx.error(TAG, "Close user onlineUser err," + userId + "errMsg: " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
    }

    public OnlineUser deleteOnlineUser(String userId) throws CoreException {
        if (onlineUserHolder != null) {
            OnlineUser removedUser = onlineUserHolder.deleteOnlineUser(userId);
            if (removedUser != null)
                removedUser.destroySelf(ChannelListener.CLOSE_DESTROYED);
            return removedUser;
        }
        return null;
    }

    public OnlineUser deleteOnlineUser(OnlineUser removedUser, int close) {
        if (onlineUserHolder != null) {
            boolean bool = onlineUserHolder.deleteOnlineUser(removedUser);
            if (bool)
                removedUser.destroySelf(close);
        }
        return removedUser;
    }

    public void deleteOnlineServiceUser(OnlineServiceUser removedUser, int close) throws CoreException {
        if (removedUser == null)
            return;
        UserInfo userInfo = removedUser.getUserInfo();
        if (userInfo == null)
            return;
        String userId = userInfo.getUserId();
        String service = userInfo.getService();
        if (userId == null || service == null)
            return;
        OnlineUser onlineUser = onlineUserHolder.getOnlineUser(userId);
        if (onlineUser != null) {
            onlineUser.removeOnlineServiceUser(service, removedUser, close);
        }
    }

    /**
     * This is used for receiving events from queues between servers.
     * As the queue has already provided single thread control.
     * So this method will still be synced.
     *
     * @param event
     * @param excludeUser
     * @return
     * @throws CoreException
     */
    public abstract Collection<String> eventReceived(Message event, OnlineUser excludeUser) throws CoreException;


    private class EventEntity {
        private EventEntity(Message e, OnlineUser ou) {
            event = e;
            excludedUser = ou;
        }

        private Message event;
        private OnlineUser excludedUser;
    }

    public void sendEvent(Message t, OnlineUser excludeUser) {
//		AcuLogger.info(TAG, "Send event, " + t.getEventType() + " targetId " + t.getFilterMatch(Event.FILTERMATCH_TARGETID));
//		try {
//			eventQueue.put(new EventEntity(event, excludeUser));
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			AcuLogger.error(TAG, "Send event failed, " + e.getMessage() + " event " + event.toString());
//		}

//		Collection<String> targetIds = t.getTargetIds();
//		if(targetIds != null) {
//			if(targetIds.contains(AdminOnlineUserImpl.ADMIN_ACCOUNT_ID)) {
////				targetIds.remove(AdminOnlineUserImpl.ADMIN_ACCOUNT_ID);
//				LoggerEx.info(TAG, "Event send to admin has been remove.");
//				//
//				return;
//			}
//		}
        //XXX new fix, removed queue implementation
        try {
            sendEventToOnlineUsers(t, excludeUser);
        } catch (CoreException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Message " + t + " sending failed, " + e.getMessage());
        }
//		String queueId = t.getFilterMatch(Message.FILTERMATCH_QUEUEID);
//		String queueId = t.getUserId() + t.getService();
//		if(queueId != null) {
//			SingleThreadQueue<EventEntity> queue = queueMap.get(queueId);
//			if(queue == null) {
//				queue = new SingleThreadQueue<>("Message Queue " + queueId, new ConcurrentLinkedQueue<EventEntity>(), ServerStart.getInstance().getThreadPool(), new MessageHandler());
//				queueMap.putIfAbsent(queueId, queue);
//				queue = queueMap.get(queueId);
//			}
//			queue.offerAndStart(new EventEntity(t, excludeUser));
//		} else {
//			LoggerEx.error(TAG, "Message don't have queueId, " + t);
//		}
    }

    protected abstract void sendEventToOnlineUsers(Message event, OnlineUser excludeUser) throws CoreException;


    public Class<? extends OnlineUser> getAdminOnlineUserClass() {
        return adminOnlineUserClass;
    }


    public void setAdminOnlineUserClass(Class<? extends OnlineUser> adminOnlineUserClass) {
        this.adminOnlineUserClass = adminOnlineUserClass;
    }

    public void getOnlineUsers(IteratorEx<OnlineUser> iterator) {
        if (onlineUserHolder != null) {
            for (OnlineUser onlineUser : onlineUserHolder.onlineUsers()) {
                if (!iterator.iterate(onlineUser))
                    break;
            }
        }
    }

    public OnlineUsersHolder getOnlineUsersHolder() {
        return onlineUserHolder;
    }

}
