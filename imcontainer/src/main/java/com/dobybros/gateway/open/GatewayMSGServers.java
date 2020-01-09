package com.dobybros.gateway.open;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.open.MSGServers;
import com.dobybros.chat.open.data.Constants;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.script.annotations.gateway.ServiceUserSessionListener;
import com.dobybros.chat.script.annotations.handler.ServiceUserSessionAnnotationHandler;
import com.dobybros.gateway.channels.data.OutgoingData;
import com.dobybros.gateway.channels.data.OutgoingMessage;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import script.groovy.runtime.GroovyRuntime;
import script.memodb.ObjectId;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by aplomb on 17-7-15.
 */
public final class GatewayMSGServers extends MSGServers {


    private static final String TAG = GatewayMSGServers.class.getSimpleName();

    // 客户端channel断掉重连是关闭以前的通道
    public static final int CHANNEL_CLOSE_CHANNELEXPIRED = Channel.ChannelListener.CLOSE_CHANNELEXPIRED;
    // 踢掉其他设备上的channel
    public static final int CHANNEL_CLOSE_KICKED = Channel.ChannelListener.CLOSE_KICKED;
    // 用户登出关闭channel
    public static final int CHANNEL_CLOSE_LOGOUT = Channel.ChannelListener.CLOSE_LOGOUT;
    // 用户同设备有旧channel切换到新channel时，关闭旧channel
    public static final int CHANNEL_CLOSE_SWITCHCHANNEL = Channel.ChannelListener.CLOSE_SWITCHCHANNEL;
    // 通道发生异常
    public static final int CHANNEL_CLOSE_ERROR = Channel.ChannelListener.CLOSE_ERROR;
    // session close
    public static final int CHANNEL_CLOSE = Channel.ChannelListener.CLOSE;
    public static final int CHANNEL_CLOSE_USEREXPIRED = Channel.ChannelListener.CLOSE_USEREXPIRED;
    public static final int CHANNEL_CLOSE_DESTROYED = Channel.ChannelListener.CLOSE_DESTROYED;
    public static final int CHANNEL_CLOSE_SHUTDOWN = Channel.ChannelListener.CLOSE_SHUTDOWN;
    public static final int CHANNEL_CLOSE_MUSTUPGRADE = Channel.ChannelListener.CLOSE_MUSTUPGRADE;
    public static final int CHANNEL_CLOSE_PASSWORDCHANGED = Channel.ChannelListener.CLOSE_PASSWORDCHANGED;
    public static final int CHANNEL_CLOSE_FORBIDDEN = Channel.ChannelListener.CLOSE_FORBIDDEN;


    private OnlineUserManager onlineUserManager = (OnlineUserManager) SpringContextUtil.getBean("onlineUserManager");

    public static GatewayMSGServers getInstance() {
        if (instance == null) {
            synchronized (MSGServers.class) {
                if (instance == null) {
                    instance = new GatewayMSGServers();
                    instance.init("defaultkey");
                }
            }
//			LoggerEx.error(TAG, "MSGServers need to be initialized first");
        }
        return (GatewayMSGServers) instance;
    }

    public boolean isUserSessionAlive(String userId, String service) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
        if (onlineUser != null) {
            OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
            if (serviceUser != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isChannelAlive(String userId, String service, Integer terminal) throws CoreException {
        if (terminal == null) return false;
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
        if (onlineUser != null) {
            OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
            if (serviceUser != null) {
                return serviceUser.getChannel(terminal) != null;
            }
        }
        return false;
    }

    public void closeUserSession(String userId, String service, int close) throws CoreException {

        OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
        if (onlineUser != null) {
            OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
            if (serviceUser != null) {
                onlineUserManager.deleteOnlineServiceUser(serviceUser, close);
            }
        }
    }

    public void closeUserChannel(String userId, String service, Integer terminal, int close) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
        if (onlineUser != null) {
            OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
            if (serviceUser != null) {
                Channel channel = serviceUser.getChannel(terminal);
                if (channel != null) {
                    serviceUser.removeChannel(channel, close);
                }
            }
        }
    }

    public void setChannelAttribute(String userId, String service, Integer terminal, String key, String value) throws CoreException {
        if (StringUtils.isNotBlank(userId)
                && StringUtils.isNotBlank(service)
                && terminal != null
                && StringUtils.isNotBlank(key)
                && StringUtils.isNotBlank(value)) {
            OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
            if (onlineUser != null) {
                OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
                if (serviceUser != null) {
                    Channel channel = serviceUser.getChannel(terminal);
                    if (channel != null) {
                        channel.setAttribute(key, value);
                    }
                }
            }
        }
    }

    public String getChannelAttribute(String userId, String service, Integer terminal, String key) throws CoreException {
        if (StringUtils.isNotBlank(userId)
                && StringUtils.isNotBlank(service)
                && terminal != null
                && StringUtils.isNotBlank(key)) {
            OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
            if (onlineUser != null) {
                OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
                if (serviceUser != null) {
                    Channel channel = serviceUser.getChannel(terminal);
                    if (channel != null) {
                        return channel.getAttribute(key);
                    }
                }
            }
        }
        return null;
    }

    public void sendMessage(Message message, Integer excludeTerminal, Integer toTerminal) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(message.getUserId());
        if (onlineUser == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while sending message " + message);
        OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(message.getService());
        if (serviceUser == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ONLINESERVICEUSER_NULL, "Online service user " + message.getUserId() + "@" + message.getService() + " not found while sending message " + message);

        if (message.getService().equals(message.getReceiverService())) {
            Collection<String> receiverIds = message.getReceiverIds();
            if (receiverIds != null && receiverIds.contains(message.getUserId())) {
                //If sender is also the receiver, send message to sender excluded the terminal where sent the message.
                OutgoingMessage out = new OutgoingMessage();
                out.fromMessage(message);
                serviceUser.pushToChannels(out, excludeTerminal, toTerminal);

                if (receiverIds.size() == 1)
                    return; //if send to user himself, then don't need to send across servers.
            }
        }

        onlineUserManager.sendEvent(message, onlineUser);
    }
    //userId is parentId
    public void closeClusterSessions(String parentId, String userId, String service, int close) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(userId);
        if (onlineUser == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + userId + " not found while closeClusterSessions ");
        OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(service);
        if (serviceUser == null) {
            LoggerEx.error(TAG, "Online service user " + userId + "@" + service + " not found while closeClusterSessions");
            return;
        }
        Message message = new Message();
        message.setId(ObjectId.get().toString());
        message.setUserId(userId);
        message.setReceiverService(service);
        message.setService(service);
        message.setType(Constants.MESSAGE_TYPE_CLOSECLUSTERSESSION);
        message.setTime(System.currentTimeMillis());
        List<String> receiverIds = new ArrayList<>();
        receiverIds.add(parentId);
        message.setReceiverIds(receiverIds);
        Map<String, Integer> contentMap = new HashMap<>();
        contentMap.put("close", close);
        message.setData(JSON.toJSONString(contentMap).getBytes(Charset.defaultCharset()));
        serviceUser.pushToCrossServer(message, null);
    }

    public void sendClusterMessage(Message message, List<Integer> toTerminals) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(message.getUserId());
        if (onlineUser == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while sending message " + message);
        OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(message.getService());
        if (serviceUser == null) {
            LoggerEx.error(TAG, "Online service user " + message.getUserId() + "@" + message.getService() + " not found while sending message " + message);
            return;
        }
        if (message.getService().equals(message.getReceiverService())) {
            serviceUser.pushToCrossServer(message, toTerminals);
        }
    }

    public void sendOutgoingData(Message message, Integer excludeTerminal, Integer toTerminal) throws CoreException {
        OnlineUser onlineUser = onlineUserManager.getOnlineUser(message.getUserId());
        if (onlineUser == null)
            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while sending message " + message);
        OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(message.getService());
        if (serviceUser == null) {
            LoggerEx.error(TAG, "Online service user " + message.getUserId() + "@" + message.getService() + " not found while sending message " + message);
            return;
        }
        if (message.getService().equals(message.getReceiverService())) {
            Collection<String> receiverIds = message.getReceiverIds();
            if (receiverIds != null && receiverIds.contains(message.getUserId())) {
                //If sender is also the receiver, send message to sender excluded the terminal where sent the message.
                OutgoingData out = new OutgoingData();
                out.fromMessage(message);
                serviceUser.pushToChannels(out, excludeTerminal, toTerminal);

                if (receiverIds.size() == 1)
                    return; //if send to user himself, then don't need to send across servers.
            }
        }

//        onlineUserManager.sendEvent(message, onlineUser);
    }

    public ServiceUserSessionListener getServiceUserSession(GroovyRuntime runtime, String userId, String service) {
        ServiceUserSessionAnnotationHandler handler = (ServiceUserSessionAnnotationHandler) runtime.getClassAnnotationHandler(ServiceUserSessionAnnotationHandler.class);
        if (handler != null) {
            ServiceUserSessionListener listener = handler.getAnnotatedListener(userId, service);
            return listener;
        }
        return null;
    }
}
