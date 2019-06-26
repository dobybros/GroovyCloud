package com.dobybros.chat.script.annotations.gateway;

import com.dobybros.chat.open.data.Message;
import org.apache.mina.core.session.IoSession;

import java.util.List;

class PendingMessageContainer {
    public static final Integer CHANNELCREATED = 1;
    public static final Integer CHANNELNOTCREATED = 0;

    int type = CHANNELNOTCREATED;
    List<Message> pendingMessages;
    List<Message> pendingDatas;
    IoSession session;
    Boolean needTcpResult;

    public static String getKey(String userId, String service, Integer terminal) {
        return userId + "#" + service + "@" + terminal;
    }
}