package com.proxy.im;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.channels.Channel.ChannelListener;
import com.dobybros.gateway.channels.msgs.MessageReceivedListener;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.pack.Pack;
import com.proxy.im.mina.MinaSessionContext;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import script.groovy.object.GroovyObjectEx;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class ProxyUpStreamHandler extends IoHandlerAdapter {
    public static final String ATTRIBUTE_TIMERTASK_IDENTITY = "IDENTITY_TIMERTAKS";
    public static final String ATTRIBUTE_SESSIONCONTEXT = "SESSIONCONTEXT";
    public static final String ATTRIBUTE_CHANNEL = "CHANNEL";
    public static final String ATTRIBUTE_VERSION = "VERSION";
    public static final String ATTRIBUTE_TERMINAL = "TERMINAL";
    public static final String ATTRIBUTE_IP = "IP";
    private static final String TAG = "UpStream";
    private int readIdleTime;
    private int writeIdleTime;
    @Resource
    ProxyAnnotationHandler proxyAnnotationHandler;
    @Resource
    ProxyUpStreamAnnotationHandler proxyUpStreamAnnotationHandler;

    @Override
    public void sessionCreated(final IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, readIdleTime);
        session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, writeIdleTime);
        TimerTaskEx task = new TimerTaskEx(ProxyUpStreamHandler.class.getSimpleName()) {
            @Override
            public void execute() {
                LoggerEx.info(TAG, "Session closed by timeout after tcp session created, " + session);
                session.close(true);
            }
        };
        session.setAttribute(ATTRIBUTE_TIMERTASK_IDENTITY, task);
        // 获取ip
        String address = session.getRemoteAddress().toString();
        address = address.replace("/", "");
        String[] addresses = address.split(":");
        if (addresses.length > 0)
            session.setAttribute(ATTRIBUTE_IP, addresses[0]);
        TimerEx.schedule(task, TimeUnit.SECONDS.toMillis(8));
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            listener.getObject().sessionCreated(getSessionContext(session));
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            listener.getObject().sessionOpened(getSessionContext(session));
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            listener.getObject().sessionClosed(getSessionContext(session));
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        session.close(true);
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            listener.getObject().sessionIdle(getSessionContext(session));
        }
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause)
            throws Exception {
        session.close();
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            listener.getObject().exceptionCaught(getSessionContext(session), cause);
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        OnlineUser onlineUser = null;
        try {
            onlineUser = (OnlineUser) session.getAttribute(ATTRIBUTE_ONLINEUSER);
            if (message != null && (message instanceof Data)) {
                Data pack = (Data) message;
                Byte type = pack.getType();
                if (type != Pack.TYPE_IN_IDENTITY && onlineUser == null)
                    throw new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_MISSING_ONLINEUSER, "Online user is missing for receiving message");

                GroovyObjectEx<MessageReceivedListener> listener = upStreamAnnotationHandler.getMessageReceivedMap().get(type);
                if (listener != null) {
                    Class<? extends Data> dataClass = listener.getObject().getDataClass();
                    if (dataClass != null) {
                        listener.getObject().messageReceived(pack, session);
                    }
                }
            } else {
                if (message != null)
                    LoggerEx.error(TAG, "Unexpected message type " + message.getClass() + " message " + message + " session " + session);
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "Message " + message + " received failed, " + t + " message: " + t.getMessage());
            CoreException coreException = null;
            if (t instanceof CoreException)
                coreException = (CoreException) t;
            if (coreException == null)
                coreException = new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_UNKNOWN, "Unknown error occured while receiving message from tcp channel, channel " + session + " message " + message + " error " + t.getMessage());
            if (coreException.getCode() >= GatewayErrorCodes.TCPCHANNEL_CLOSE_START && coreException.getCode() < GatewayErrorCodes.TCPCHANNEL_CLOSE_END) {
                if (onlineUser != null) {
                    Channel channel = (Channel) session.getAttribute(ATTRIBUTE_CHANNEL);
                    if (channel != null) {
                        onlineUser.removeChannel(channel, ChannelListener.CLOSE_ERROR);
                    }
                } else {
                    session.close(false);
                }
            } else if (coreException.getCode() >= GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_START && coreException.getCode() < GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_END) {
                if (onlineUser != null) {
                    Channel channel = (Channel) session.getAttribute(ATTRIBUTE_CHANNEL);
                    if (channel != null) {
                        onlineUser.removeChannel(channel, ChannelListener.CLOSE_ERROR);
                    }
                } else {
                    session.close(true);
                }
            } else {
                session.close(true);
            }
        }
    }

    public void messageSent(IoSession session, Object message) throws Exception {

    }

    /**
     * @return the readIdleTime
     */

    public int getReadIdleTime() {
        return readIdleTime;
    }

    /**
     * @param readIdleTime the readIdleTime to set
     */

    public void setReadIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    /**
     * @return the writeIdleTime
     */

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    /**
     * @param writeIdleTime the writeIdleTime to set
     */

    public void setWriteIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

    SessionContext getSessionContext(IoSession session){
        SessionContext sessionContext = (SessionContext)session.getAttribute(ATTRIBUTE_SESSIONCONTEXT);
        if(sessionContext == null){
            sessionContext = new MinaSessionContext(session);
            session.setAttribute(ATTRIBUTE_SESSIONCONTEXT);
        }
        return sessionContext;
    }

}