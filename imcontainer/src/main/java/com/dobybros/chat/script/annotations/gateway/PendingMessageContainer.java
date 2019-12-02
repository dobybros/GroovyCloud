package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.onlineusers.OnlineUser;

import java.util.List;

class PendingMessageContainer {
    public static final Integer CHANNELCREATED = 1;
    public static final Integer CHANNELNOTCREATED = 0;

    int type = CHANNELNOTCREATED;
    List<Message> pendingMessages;
    List<Message> pendingDatas;
    OnlineUser onlineUser;
    Boolean needTcpResult;

    public static String getKey(String userId, String service, Integer terminal) {
        return userId + "#" + service + "@" + terminal;
    }
}