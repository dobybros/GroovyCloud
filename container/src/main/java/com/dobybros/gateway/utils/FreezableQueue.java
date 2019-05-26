package com.dobybros.gateway.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.server.OnlineServer;
import com.dobybros.chat.storage.adapters.MessageService;
import com.dobybros.chat.storage.adapters.OfflineMessageAdapter;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.utils.SingleThreadQueue;
import com.dobybros.chat.utils.TalentUtils;
import com.dobybros.gateway.channels.data.OutgoingMessage;
import com.dobybros.gateway.channels.data.Result;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.PushInfo;
import com.dobybros.gateway.onlineusers.PushInfo.SpecialHandler;
import com.docker.utils.SpringContextUtil;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.*;

/**
 * 这是一个限制内存增长的队列。 
 * 1， 队列有最大限制， 如果达到最大限制就会进入Freeze状态。 在Freeze状态下不能新增数据。
 * 2， 在Freeze状态下， 只有所有数据都被删除了， 才能解除Freeze状态， 在解除Freeze状态是需要通知调用方。   
 * 3， 能查询是否达到Freeze状态。
 * 4， 能按先后顺序得取出数据。
 * 5， 能从队列里删除任意位置的数据。 
 * 
 * @author aplomb
 *
 */
public class FreezableQueue {
	private SpecialHandler readOfflineMessageHandler;
	private SpecialHandler flushMessageHandler;
	private SpecialHandler clearReceivedMessageIdsHandler;
	
	private OfflineMessageAdapter offlineMessageAdapter;
	private Collection<String> receivedOfflineMessageIds = new ConcurrentHashSet<>();
	private OnlineServiceUser onlineUser;
	private static final String TAG = FreezableQueue.class.getSimpleName();
	
	private OfflineMessageSavingTask offlineMessageSavingTask;
	/**
	 * 在非多线程的情况下，应当尽量使用TreeMap。此外对于并发性相对较低的并行程序可以使用Collections.synchronizedSortedMap将TreeMap进行包装，也可以提供较好的效率。对于高并发程序，应当使用ConcurrentSkipListMap，能够提供更高的并发度。


		所以在多线程程序中，如果需要对Map的键值进行排序时，请尽量使用ConcurrentSkipListMap，可能得到更好的并发度。	
		注意，调用ConcurrentSkipListMap的size时，由于多个线程可以同时对映射表进行操作，所以映射表需要遍历整个链表才能返回元素个数，这个操作是个O(log(n))的操作。
	 */
//	private SortedMap<String, T> map = Collections.synchronizedSortedMap(new TreeMap<String, T>());
	private Map<String, PushInfo> map = Collections.synchronizedMap(new LinkedHashMap<String, PushInfo>());
	private SingleThreadQueue<PushInfo> acuEventQueue;
	private int freezeCount = 100;
	private int clearReceivedMessageIdCount = 200;
	private int removedMessageIdCounter = 0;
	
	private boolean isFrozen = false;
	private Long frozenTouchTime;
	
	private FreezableQueue instance;
	public static int OFFLINE_MESSAGE_RECEIVED_CODE = 11;
	public FreezableQueue() {
		readOfflineMessageHandler = getReadOfflineMessageHandler();
		flushMessageHandler = getFlushMessageHandler();
		instance = this;
		
		offlineMessageAdapter = StorageManager.getInstance().getStorageAdapter(OfflineMessageAdapter.class);
	}
	
	public synchronized void init() {
		isFrozen = true;
		frozenTouchTime = System.currentTimeMillis();
		
		PushInfo pushInfo = new PushInfo(readOfflineMessageHandler);
		acuEventQueue.offerAndStart(pushInfo);
		LoggerEx.debug(TAG, onlineUser.userInfo() + " enqueued special handler " + readOfflineMessageHandler);
	}
	

	public void flush() {
		PushInfo pushInfo = new PushInfo(flushMessageHandler);
		acuEventQueue.offerAndStart(pushInfo);
		LoggerEx.debug(TAG, onlineUser.userInfo() + " enqueued special handler(flush) " + flushMessageHandler);
	}
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	public synchronized void add(PushInfo t) {
//		if(isFrozen)
//			return false;
		String id = t.getId();
		if(id == null) {
			LoggerEx.error(TAG, onlineUser.userInfo() + " ID is null while add into FreezableQueue, " + t);
			return;
		}
		
		if(!map.containsKey(id)) {
			PushInfo old = map.put(id, t);
			if(old == null) { //
				if(!isFrozen && map.size() >= freezeCount) {
					isFrozen = true;
					frozenTouchTime = System.currentTimeMillis();
					LoggerEx.debug(TAG, onlineUser.userInfo() + " wait queue is frozen " + map.size() + " exceeded " + freezeCount);
				} 
			}
		}
//		return true;
	}
	
	/**
	 * 在Freeze状态下， 删空map就会导致解冻， 返回true， 其余情况放回false
	 * 
	 * @param key
	 * @return
	 */
	public synchronized void remove(String key) {
		PushInfo removed = map.remove(key);
		receivedOfflineMessageIds.add(key);
		
		if(clearReceivedMessageIdsHandler == null && ++removedMessageIdCounter >= clearReceivedMessageIdCount) {
			LoggerEx.debug(TAG, onlineUser.userInfo() + " removed messageIds " + removedMessageIdCounter + " has exceeded " + clearReceivedMessageIdCount + ". activate a special handler for clear received messageids");
			removedMessageIdCounter = 0;//用自己的计数器取代receivedOfflineMessageIds.size()， 避免效率低的问题。 
			clearReceivedMessageIdsHandler = new SpecialHandler() {
				@Override
				public void handle() {
					removeReceivedOfflineMessageIds(); //删除掉客户端已经成功接收到的离线消息。 
					clearReceivedMessageIdsHandler = null; //完成任务后才允许下一次任务。 
				}
			};
			
			PushInfo pushInfo = new PushInfo(clearReceivedMessageIdsHandler);
			acuEventQueue.offerAndStart(pushInfo);
		}
		
		if(isFrozen) {
			if(removed != null && map.isEmpty() && isFrozen) {//在冻结的情况下， 删除map里的数据导致map为空时， 在这个用户的单线程处理队列里放入一个特殊任务， （由于当前线程是Mina的TCP线程， 避免做重活）用以从离线数据库里读取消息再放入map里。 
				PushInfo pushInfo = new PushInfo(readOfflineMessageHandler);
				acuEventQueue.offerAndStart(pushInfo);
				LoggerEx.debug(TAG, onlineUser.userInfo() + " enqueued special handler " + readOfflineMessageHandler);
			} else {
				frozenTouchTime = System.currentTimeMillis();
			}
		}
	}
	
	private SpecialHandler getReadOfflineMessageHandler() {
		return new SpecialHandler() {
			@Override
			public void handle() {
				try {
					removeReceivedOfflineMessageIds(); //删除掉客户端已经成功接收到的离线消息。 
					
					final ArrayList<PushInfo> pushInfoList = new ArrayList<>();
					List<OfflineMessage> offlineMessageList = offlineMessageAdapter.readOfflineMessages(onlineUser.getUserInfo().getUserId(), onlineUser.getService(), 0, freezeCount);
					for (OfflineMessage offlineMessage : offlineMessageList) {
						Message message = offlineMessage.getMessage();
						if(message != null) {
//								message.setFromOfflineMessageId(t.getId());
//								onlineUser.eventReceived(message);
							pushInfoList.add(new PushInfo(message, null));
						}
					}
					if(pushInfoList.isEmpty()) {
						isFrozen = false;
						frozenTouchTime = null;
						LoggerEx.debug(TAG, onlineUser.userInfo() + " unfreeze now");
						//发送已经获取完毕所有离线消息的事件，客户端根据该消息来判断是否开始响声震动
//						OfflineMessagesReceivedEvent offlineMessagesReceivedEvent = new OfflineMessagesReceivedEvent();
//						onlineUser.pushToChannels(offlineMessagesReceivedEvent, null);
						Result result = new Result();
						result.setCode(OFFLINE_MESSAGE_RECEIVED_CODE);
//						ResultEvent resultEvent = new ResultEvent();
//						resultEvent.setCode(OFFLINE_MESSAGE_RECEIVED_CODE);
						onlineUser.pushToChannels(result, null);
						LoggerEx.info(TAG, onlineUser.userInfo() + " send offlineMessagesReceivedEvent.");


						//Notify other lans to send him the offline messages.
						GlobalLansProperties globalLansProperties = (GlobalLansProperties) SpringContextUtil.getBean("globalLansProperties");
						if (globalLansProperties != null) {
							Map<String, GlobalLansProperties.Lan> map = globalLansProperties.getLanMap();
							for (String lanId : map.keySet()) {
								if(lanId.equals(OnlineServer.getInstance().getLanId())) {
									continue;
								}
								MessageService service = StorageManager.getInstance().getStorageAdapter(MessageService.class, lanId);
								if (service != null)
									service.consumeOfflineMessages(onlineUser.getUserInfo().getUserId(), onlineUser.getUserInfo().getService());
							}
						}

					} else {
						LoggerEx.debug(TAG, onlineUser.userInfo() + " is frozen, push " + pushInfoList.size());
						synchronized (instance) {
							onlineUser.pushToChannelsSync(pushInfoList);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
					LoggerEx.error(TAG, onlineUser.userInfo() + " specialHandler for frozen queue failed, " + e.getMessage());
				}
			}
		};
	}
	
	private SpecialHandler getFlushMessageHandler() {
		return new SpecialHandler() {
			@Override
			public void handle() {
				Collection<PushInfo> values = values();
				if(values != null && !values.isEmpty()) {
					LoggerEx.debug(TAG, onlineUser.userInfo() + " flush " + values.size());
					synchronized (instance) {
						onlineUser.pushToChannelsSync(values);
					}
				}
			}
		};
	}
	
	private void removeReceivedOfflineMessageIds() {
		if(receivedOfflineMessageIds != null) {
			ConcurrentHashSet<String> removeIds = new ConcurrentHashSet<>(receivedOfflineMessageIds);//receivedOfflineMessageIds 是一个动态变化的数据， 所以先复制一份到另外一个set中进行删除操作。 
			if(removeIds.isEmpty())
				return;
			try {
				offlineMessageAdapter.removeOfflineMessages(onlineUser.getUserInfo().getUserId(), removeIds);
				receivedOfflineMessageIds.removeAll(removeIds); //然后再做set的减法， 以确保不出错误。 
				LoggerEx.debug(TAG, onlineUser.userInfo() + " remove received offline messageIds " + TalentUtils.toString(removeIds));
			} catch (CoreException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, onlineUser.userInfo() + " remove offlineMessages " + TalentUtils.toString(removeIds) + " by userId " + onlineUser.getUserInfo().getUserId() + " failed, " + e.getMessage());
			}
		}
	}
	
	public synchronized void shutdown() {
		LoggerEx.debug(TAG, onlineUser.userInfo() + " wait queue is shutting down");
		removeReceivedOfflineMessageIds();
		
		for(PushInfo pushInfo : values()) {
			Message message = pushInfo.getEvent();
			if(message == null) {
				Data data = pushInfo.getData();
				if(data != null && data instanceof OutgoingMessage) {
					message = ((OutgoingMessage) data).getMessage();
				}
			}
			if(message == null)
				continue;
			OfflineMessage offlineMessage = new OfflineMessage();
//			offlineMessage.setSaveListener(this);
			Message msg = message.cloneWithEmptyReceiveIds();
//			msg.setTargetIds(Arrays.asList(onlineUser.getUserId()));
			offlineMessage.setMessage(msg);
			msg.setReceiverIds(Arrays.asList(onlineUser.getUserInfo().getUserId()));
			offlineMessageSavingTask.addMessage(offlineMessage);
		}
	}
	
	public int size() {
		return map.size();
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public synchronized Collection<PushInfo> values() {
//		ArrayList<T> list = new ArrayList<>(map.values());
//		Collections.sort(list);
//		return list;
		return map.values();
	}
	
	public static void main(String[] args) {
//		SortedMap<String, Object> map = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
//		ConcurrentSkipListMap<String, Object> map = new ConcurrentSkipListMap<>();
//		ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		
		map.put("33", new Object());
		map.put("323", new Object());
		map.put("133", new Object());
		map.put("333", new Object());
		map.put("3133", new Object());
		map.put("1", new Object());
		for(String key : map.keySet()) {
			System.out.println(key);
		}
	}

	public int getFreezeCount() {
		return freezeCount;
	}

	public void setFreezeCount(int freezeCount) {
		this.freezeCount = freezeCount;
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	public void setFrozen(boolean isFrozen) {
		this.isFrozen = isFrozen;
	}

	public Long getFrozenTouchTime() {
		return frozenTouchTime;
	}

	public void setFrozenTouchTime(Long frozenTouchTime) {
		this.frozenTouchTime = frozenTouchTime;
	}

	public void clear() {
		map.clear();
	}

	public Collection<String> getReceivedOfflineMessageIds() {
		return receivedOfflineMessageIds;
	}

	public void setReceivedOfflineMessageIds(
			Collection<String> receivedOfflineMessageIds) {
		this.receivedOfflineMessageIds = receivedOfflineMessageIds;
	}

	public SingleThreadQueue<PushInfo> getAcuEventQueue() {
		return acuEventQueue;
	}

	public void setAcuEventQueue(SingleThreadQueue<PushInfo> acuEventQueue) {
		this.acuEventQueue = acuEventQueue;
	}

	public int getClearReceivedMessageIdCount() {
		return clearReceivedMessageIdCount;
	}

	public void setClearReceivedMessageIdCount(int clearReceivedMessageIdCount) {
		this.clearReceivedMessageIdCount = clearReceivedMessageIdCount;
	}

	public String description() {
		return isFrozen + " ";
	}

	public OfflineMessageSavingTask getOfflineMessageSavingTask() {
		return offlineMessageSavingTask;
	}

	public void setOfflineMessageSavingTask(OfflineMessageSavingTask offlineMessageSavingTask) {
		this.offlineMessageSavingTask = offlineMessageSavingTask;
	}

	public OnlineServiceUser getOnlineUser() {
		return onlineUser;
	}

	public void setOnlineUser(OnlineServiceUser onlineUser) {
		this.onlineUser = onlineUser;
	}

}
