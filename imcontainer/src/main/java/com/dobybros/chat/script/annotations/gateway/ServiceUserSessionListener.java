package com.dobybros.chat.script.annotations.gateway;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.open.data.IMConfig;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.MsgResult;
import com.dobybros.gateway.open.GatewayMSGServers;

import java.util.List;

public abstract class ServiceUserSessionListener extends DataServiceUserSessionListener{


    private GatewayMSGServers gatewayMSGServers = GatewayMSGServers.getInstance();

    public void sessionCreated() {
        if(backUpMemory()){
            Object data = getRoomDataFromMonitor();//get RoomData from monitor
            if(data != null){
                saveRoomData(data);
            }
            restoreData();
        }
    }

    public void sessionClosed(int close) {
        if(backUpMemory()){
            removeMonitorRoomData();
        }
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

    public IMConfig getIMConfig() {
        return null;
    }

    @Deprecated
    public Long getMaxInactiveInterval() {
        return null;
    }

    public Boolean shouldInterceptMessageReceivedFromUsers(Message message) {
        return false;
    }

    public void messageSent(Data event, Integer excludeTerminal, Integer toTerminal) {
    }

    public void messageReceivedFromUsers(Message message, String receiverId, String receiverService) {
    }

    @Deprecated
    public void pingReceived(Integer terminal) {
    }

    public void pingTimeoutReceived(Integer terminal) {
    }

    public void sendMessage(Message message, Integer excludeTerminal, Integer terminal) throws CoreException {
        gatewayMSGServers.sendMessage(message, excludeTerminal, terminal);
    }

    public void closeClusterSessions(int close) throws CoreException {
        gatewayMSGServers.closeClusterSessions(getParentUserId(), getUserId(), getService(), close);
    }

    /**
     * message's receiverIds is parentUserId
     *
     * @param message
     * @param toTerminals
     * @throws CoreException
     */
    public void sendClusterMessage(Message message, List<Integer> toTerminals) throws CoreException {
        gatewayMSGServers.sendClusterMessage(message, toTerminals);
    }

    public void sendData(Message message, Integer excludeTerminal, Integer terminal) throws CoreException {
        gatewayMSGServers.sendOutgoingData(message, excludeTerminal, terminal);
    }

    public void closeChannel(Integer terminal, int code) throws CoreException {
        if (terminal != null)
            gatewayMSGServers.closeUserChannel(getUserId(), getService(), terminal, code);
    }

    public void closeSession() throws CoreException {
        gatewayMSGServers.closeUserSession(getUserId(), getService(), Channel.ChannelListener.CLOSE_SHUTDOWN);
    }

    public boolean isSessionAlive() throws CoreException {
        return gatewayMSGServers.isUserSessionAlive(getUserId(), getService());
    }

    public boolean isChannelAlive(Integer terminal) throws CoreException {
        return gatewayMSGServers.isChannelAlive(getUserId(), getService(), terminal);
    }
}
