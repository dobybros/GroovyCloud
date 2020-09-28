package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.open.data.IMConfig;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.MsgResult;

import java.util.List;

public abstract class SessionListener extends DataSessionListener {

    public void sessionCreated(String userId, String service) {
//        if(backUpMemory()){
//            Object data = getRoomDataFromMonitor(userId, service);//get RoomData from monitor
//            if(data != null){
//                saveRoomData(userId, service, data);
//            }
//            restoreData(userId, service);
//        }
    }

    public void sessionClosed(String userId, String service, int close) {
//        if(backUpMemory()){
//            removeMonitorRoomData(userId, service, close);
//        }
    }

    public List<Integer> channelRegisterd(String userId, String service, Integer terminal) {
        return null;
    }

    public void channelCreated(String userId, String service, Integer terminal) {
    }

    public void channelClosed(String userId, String service, Integer terminal, int close) {
    }

    public MsgResult messageReceived(Message message, Integer terminal) {
        return null;
    }

    public MsgResult dataReceived(Message message, Integer terminal) {
        return null;
    }

    //	public void messageReceivedAsync(Message message, Integer terminal) {
//	}
    public IMConfig getIMConfig(String userId, String service) {
        return null;
    }

    @Deprecated
    public Long getMaxInactiveInterval(String userId, String service) {
        return null;
    }

    public boolean shouldInterceptMessageReceivedFromUsers(Message message, String userId, String service) {
        return false;
    }

    public void messageSent(Data event, Integer excludeTerminal, Integer toTerminal, String userId, String service) {
    }

    public void messageReceivedFromUsers(Message message, String receiverId, String receiverService) {
    }
//	public void messageReceivedFromUsersAsync(Message message, String receiverId, String receiverService) {
//	}

    public void pingReceived(String userId, String service, Integer terminal) {
    }

    public void pingTimeoutReceived(String userId, String service, Integer terminal) {
    }
//	public void pingReceivedAsync(String userId, String service, Integer terminal) {}
}
