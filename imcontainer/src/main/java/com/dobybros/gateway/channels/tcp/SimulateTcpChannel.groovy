package com.dobybros.gateway.channels.tcp

import chat.logs.LoggerEx
import com.dobybros.chat.binary.data.Data
import com.dobybros.chat.handlers.ProxyContainerDuplexSender
import com.dobybros.chat.handlers.imextention.IMExtensionCache
import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest
import com.dobybros.gateway.channels.data.OutgoingData
import com.dobybros.gateway.channels.data.OutgoingMessage
import com.dobybros.gateway.onlineusers.OnlineServiceUser
import com.dobybros.gateway.pack.Pack
import com.docker.rpc.BinaryCodec
import com.docker.rpc.remote.stub.RemoteServers
import com.docker.utils.GroovyCloudBean
import com.docker.utils.SpringContextUtil
import org.apache.commons.lang.exception.ExceptionUtils

/**
 * @author lick* @date 2019/11/18
 */
public class SimulateTcpChannel extends TcpChannel {
    private ProxyContainerDuplexSender proxyContainerDuplexSender = (ProxyContainerDuplexSender) SpringContextUtil.getBean("proxyContainerDuplexSender");
    private IMExtensionCache imExtensionCache = (IMExtensionCache) GroovyCloudBean.getBean(GroovyCloudBean.IMEXTENSIONCACHE);

    private RemoteServers.Server server;
    private String userId;
    private String service;
    private boolean closed = false;
    private int[] lock = new int[0];
    private Short encodeVersion;
    private String ip

    public SimulateTcpChannel(Integer terminal) {
        super(terminal);
    }

    @Override
    public void offer(Data event) {
        if (server != null) {
            if (!closed) {
                this.sendEvent(event);
            }
        } else {
            LoggerEx.error(TAG, "Channel send data failed, server is null, userId: " + userId + ",service: " + service + ",terminal: " + getTerminal());
        }
    }

    @Override
    public void send(Data event) {
        if (server != null) {
            if (!closed) {
                this.sendEvent(event);
            }
        } else {
            LoggerEx.error(TAG, "Channel send data failed, server is null, userId: " + userId + ",service: " + service + ",terminal: " + getTerminal());
        }
    }

    private void sendEvent(final Data event) {
        if (event != null) {
            try {
                IMProxyRequest request = buildIMProxyRequest();
                if (event.getData() == null) {
                    try {
                        event.persistent();
                    } catch (Throwable throwable) {
                        LoggerEx.error(TAG, "Event persistent error,errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                    }
                }
                if (event.getData() != null) {
                    request.setTheData(event.getData());
                }
                request.setTheType(event.getType());
                String contentType = null;
                switch (event.getType()) {
                    case Pack.TYPE_OUT_OUTGOINGMESSAGE:
                        contentType = ((OutgoingMessage) event).getContentType();
                        break;
                    case Pack.TYPE_OUT_OUTGOINGDATA:
                        contentType = ((OutgoingData) event).getContentType();
                        break;
                    default:
                        break
                }
                sendProxy(request, contentType);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public boolean close(int close) {
        if (server != null) {
            boolean canExecute = false;
            if (!closed) {
                synchronized (lock) {
                    if (!closed) {
                        closed = true;
                        canExecute = true;
                    }
                }
            }
            if (canExecute) {
                try {
                    IMProxyRequest request = buildIMProxyRequest();
                    if (close == ChannelListener.CLOSE_IMMEDIATELY) {
                        request.setChannelStatus(IMProxyRequest.CHANNELSTATUS_CLOSE_IMMEDIATELY)
                    } else {
                        request.setChannelStatus(IMProxyRequest.CHANNELSTATUS_CLOSE);
                    }
                    sendProxy(request, null);
                    imExtensionCache.delNewUserId(userId, service, getTerminal())
                    channelClosed(close);
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.error(TAG, "Close " + close + " failed, " + t.getMessage());
                }
            }
        } else {
            LoggerEx.error(TAG, "Close channel failed, server is null, userId: " + userId + ",service: " + service + ",terminal: " + getTerminal());
        }
        return true;
    }

    public void setImMessageSendInvoke(ProxyContainerDuplexSender imMessageSendInvoke) {
        this.imMessageSendInvoke = imMessageSendInvoke;
    }

    private void sendProxy(IMProxyRequest request, String contentType) {
        try {
            proxyContainerDuplexSender.sendProxy(request, contentType, server, null);
        } catch (Throwable throwable) {
            for (OnlineServiceUser onlineServiceUser : getOnlineServiceUsers().values()) {
                onlineServiceUser.removeChannel(this, ChannelListener.CLOSE_ERROR);
            }
            ChannelListener channelListener = this.getChannelListener();
            if (channelListener != null) {
                channelListener.exceptionCaught(throwable);
            }
        }
    }

    private IMProxyRequest buildIMProxyRequest() {
        IMProxyRequest request = new IMProxyRequest();
        request.setChannelId(getId());
        request.setService(service);
        request.setUserId(userId);
        request.setEncode(BinaryCodec.ENCODE_JAVABINARY)
        return request;
    }

    @Override
    public Short getEncodeVersion() {
        return encodeVersion
    }

    void setEncodeVersion(Short encodeVersion) {
        this.encodeVersion = encodeVersion
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setServer(RemoteServers.Server server) {
        this.server = server;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    String getIp() {
        return ip
    }

    void setIp(String ip) {
        this.ip = ip
    }
}
