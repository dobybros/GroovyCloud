package com.dobybros.gateway.eventhandler;

import com.dobybros.chat.data.OfflineMessage;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.dobybros.gateway.onlineusers.OnlineUsersHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;


public abstract class CommonEventHandler implements EventHandler {
    private static final String TAG = "EH";

    @Resource
    private OnlineUserManager onlineUserManager;
    @Resource
    private RPCMessageSendingTask messageSendingTask;
    @Resource
    private OfflineMessageSavingTask offlineMessageSavingTask;

    public CommonEventHandler() {
    }

    public void broadcastEvent(Message event) {
//		eventSendingTask.addEvent(event);
        messageSendingTask.addMessage(event);
    }

    protected Collection<String> handleUserReceive(Collection<String> receivingUserIds,
                                                   Message message, OnlineUser excludeUser, Collection<String> receivedUserIds, boolean isReceived) {
        Collection<String> targetIds = message.getReceiverIds();
        //Mean this message is in retry logic.
        //Or the message is sending from another server.
        if (targetIds != null && !targetIds.isEmpty())
            receivingUserIds = targetIds;
        if (receivingUserIds != null) {
            ArrayList<String> pendingTagetIds = null;
            if (!isReceived)
                pendingTagetIds = new ArrayList<>();

            OnlineUsersHolder onlineUsersHolder = onlineUserManager.getOnlineUsersHolder();
            if (onlineUsersHolder != null) {
                for (String userId : receivingUserIds) {
//					if(userId.equals(AdminOnlineUserImpl.ADMIN_ACCOUNT_ID))
                    boolean received = false;
                    OnlineUser onlineUser = onlineUsersHolder.getOnlineUser(userId);
                    if (onlineUser != null) {
                        if (excludeUser != null && excludeUser.equals(onlineUser))
                            continue;
                        int receivedByNotFreeze = onlineUser.eventReceived(message);
                        switch (receivedByNotFreeze) {
                            case OnlineUser.RECEIVED:
                                if (receivedUserIds != null)
                                    receivedUserIds.add(userId);
                                received = true;
                                break;
                            case OnlineUser.RECEIVED_FROZEN:
                                Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
                                if(notSaveOfflineMessage == null || !notSaveOfflineMessage) {
                                    OfflineMessage offlineMessage = new OfflineMessage();
                                    offlineMessage.setMessage(message.clone());
                                    offlineMessageSavingTask.addMessage(offlineMessage);
                                }
                                received = true;
                                break;
                            case OnlineUser.RECEIVED_NOSERVICEUSER:
                                break;
                        }
//                        if (receivedByNotFreeze == OnlineUser.RECEIVED) {
//                            if (receivedUserIds != null)
//                                receivedUserIds.add(userId);
//                        } else {
//                            Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
//                            if(notSaveOfflineMessage == null || !notSaveOfflineMessage) {
//                                OfflineMessage offlineMessage = new OfflineMessage();
//                                offlineMessage.setMessage(message.clone());
//                                offlineMessageSavingTask.addMessage(offlineMessage);
//                            }
//                        }
//                        received = true;
                    }
                    if (!received) {
                        if (pendingTagetIds != null)
                            pendingTagetIds.add(userId);
                    }
                }
                return pendingTagetIds;
            }
        }
        return null;
    }

    public OnlineUserManager getOnlineUserManager() {
        return onlineUserManager;
    }

    public void setOnlineUserManager(OnlineUserManager onlineUserManager) {
        this.onlineUserManager = onlineUserManager;
    }

    public RPCMessageSendingTask getMessageSendingTask() {
        return messageSendingTask;
    }

    public void setMessageSendingTask(RPCMessageSendingTask messageSendingTask) {
        this.messageSendingTask = messageSendingTask;
    }

    public OfflineMessageSavingTask getOfflineMessageSavingTask() {
        return offlineMessageSavingTask;
    }

    public void setOfflineMessageSavingTask(OfflineMessageSavingTask offlineMessageSavingTask) {
        this.offlineMessageSavingTask = offlineMessageSavingTask;
    }
}
