package com.dobybros.chat.script.annotations.gateway;

import chat.errors.CoreException;
import com.alibaba.fastjson.JSONObject;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.MsgResult;
import com.dobybros.gateway.open.GatewayMSGServers;
import com.dobybros.gateway.pack.HailPack;
import org.apache.commons.lang.NotImplementedException;
import scala.Int;
import script.memodb.ObjectId;

import java.util.List;

public abstract class ServiceUserSessionListener {

    private String userId;

    private String service;

    public void sessionCreated() {
    }

    public void sessionClosed(int close) {
    }

    public List<Integer> channelRegistered(Integer terminal) {
        return null;
    }

    public void channelCreated(Integer terminal) {
    }

    public void channelClosed(Integer terminal, int close) {
    }

    public MsgResult messageReceived(Message message, Integer terminal) {
        return null;
    }

    public MsgResult dataReceived(Message message, Integer terminal) {
        return null;
    }

    public Long getMaxInactiveInterval() {
        return null;
    }

    public void messageSent(Data event, Integer excludeTerminal, Integer toTerminal) {
    }

    public void messageReceivedFromUsers(Message message, String receiverId, String receiverService) {
    }

    public void sendMessage(Message message, Integer excludeTerminal, Integer terminal) throws CoreException {
        GatewayMSGServers.getInstance().sendMessage(message, excludeTerminal, terminal);
    }

    public void sendData(Message message, Integer excludeTerminal, Integer terminal) throws CoreException {
        GatewayMSGServers.getInstance().sendOutgoingData(message, excludeTerminal, terminal);
    }

    public void closeChannel(Integer terminal, int code) throws CoreException {
        GatewayMSGServers.getInstance().closeUserChannel(userId, service, terminal, code);

    }

    public void pingReceived(Integer terminal) {
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
}
