package com.dobybros.chat.tasks;

import chat.logs.LoggerEx;
import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.storage.adapters.OfflineMessageAdapter;
import com.dobybros.chat.storage.adapters.StorageManager;
import com.dobybros.chat.utils.TalentUtils;
import com.docker.tasks.Task;

import java.util.concurrent.LinkedBlockingQueue;

public class OfflineMessageSavingTask extends Task {
	private static final String TAG = OfflineMessageSavingTask.class.getSimpleName();

	private LinkedBlockingQueue<OfflineMessage> messageQueue;
	
	boolean isStarted = true;
	
	public OfflineMessageSavingTask() {
		
	}
	
	@Override
	public String taskDescription() {
		StringBuilder builder = new StringBuilder(super.taskDescription());
		builder.append(": Offline messages pending count ").append(messageQueue.size());
		return builder.toString();
	}
	
	@Override
	public void execute() {
		while(isStarted) {
			OfflineMessage message = null;
			try {
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(message != null) {
					if(message instanceof ShutdownOfflineMessage) {
						break;
					}
					Message msg = message.getMessage();
					if(msg != null) {
						OfflineMessageAdapter offlineMessageAdapter = StorageManager.getInstance().getStorageAdapter(OfflineMessageAdapter.class);
						offlineMessageAdapter.saveOfflineMessage(message);
						
						LoggerEx.info(TAG, "Save offlineMessage Id " + message.getId() + " messageId " + msg.getId() + " targetIds " + TalentUtils.toString(msg.getReceiverIds()));
					}
				}
			} catch(Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "OfflineMessage " + message + " saving failed, " + t.getMessage());
			}
		}
		LoggerEx.info(TAG, OfflineMessageSavingTask.class.getSimpleName() + " is shutted down. " + Thread.currentThread());
	}
	
//	protected void saveDomainMessage(IMessageQueueService messageQueueService, Message event) throws CoreException {
//		messageQueueService.addMessage(event);		
//	}
	
	public void destroy() {
	}
	
	public void addMessage(OfflineMessage message) {
		if(message == null)
			return;
		if(message.getTime() == null)
			message.setTime(System.currentTimeMillis());
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
	}

	public LinkedBlockingQueue<OfflineMessage> getMessageQueue() {
		return messageQueue;
	}

	class ShutdownOfflineMessage extends OfflineMessage {
	}
	
	@Override
	public void shutdown() {
		LoggerEx.info(TAG, OfflineMessageSavingTask.class.getSimpleName() + " is shutting down, " + (messageQueue != null ? messageQueue.size() : 0) + " is still left. ");
		isStarted = false;
		Integer numOfThread = getNumOfThreads();
		if(numOfThread == null) 
			numOfThread = 1;
		for(int i = 0; i < numOfThread; i++) {
			addMessage(new ShutdownOfflineMessage());
		}
	}

//	private ConcurrentHashMap<String, OfflineMessageWrapper> offlineMessagePendingMap = new ConcurrentHashMap<>();
	
//	@Override
//	public void messageWillSend(Message message) {
//		if(message.getFromOfflineMessageId() != null || message.isInternal())
//			return;
//		OfflineMessage offlineMessage = new OfflineMessage();
//		offlineMessage.setMessage(message);
//		offlineMessage.setTime(System.currentTimeMillis());
//		offlineMessage.setId(ObjectId.get().toString());
//		message.setFromOfflineMessageId(offlineMessage.getId());
//		
//		OfflineMessageWrapper wraper = new OfflineMessageWrapper(offlineMessage, newScheduledThreadPool, this, onlineUserManager);
//		OfflineMessageWrapper oldWraper = offlineMessagePendingMap.putIfAbsent(message.getId(), wraper);
//		if(oldWraper == null) {
//			wraper.start();
//		} 
//	}

//	@Override
//	public boolean filter(Message t) {
//		if(t instanceof AckMessage) {
//			AckMessage ack = (AckMessage) t;
//			String messageId = ack.getMessageId();
//			Collection<String> hitIds = ack.getHitIds();
//			updateOfflineMessagePendingMap(messageId, hitIds);
//			return t.isInternal();
//		}
//		return false;
//	}

//	public void updateOfflineMessagePendingMap(String messageId,
//			Collection<String> hitIds) {
//		if(messageId != null && hitIds != null && !hitIds.isEmpty()) {
//			OfflineMessageWrapper offlineMessageWrapper = offlineMessagePendingMap.get(messageId);
//			if(offlineMessageWrapper != null) {
//				OfflineMessage offlineMessage = offlineMessageWrapper.getOfflineMessage();
//				if(offlineMessage != null) {
//					Message message = offlineMessage.getMessage();
//					Collection<String> pendingUserIds = message.getTargetIds();
//					if(pendingUserIds != null && !pendingUserIds.isEmpty()) {
//						boolean changed = pendingUserIds.removeAll(hitIds);
//						if(changed) {
//							if(pendingUserIds.isEmpty()) {
//								offlineMessagePendingMap.remove(messageId);
//								offlineMessageWrapper.stop();
//							} 
//						}
//					}
//				}
//			}
//		}		
//	}

}
