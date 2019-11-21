package com.proxy.im;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.data.SessionContextAttr;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.pack.Pack;
import com.proxy.im.mina.MinaSessionContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import script.groovy.object.GroovyObjectEx;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class ProxyUpStreamHandler extends IoHandlerAdapter {
    public static final String ATTRIBUTE_TIMERTASK_IDENTITY = "IDENTITY_TIMERTAKS";
    public static final String ATTRIBUTE_SESSIONCONTEXT = "SESSIONCONTEXT";
    public static final String ATTRIBUTE_VERSION = "VERSION";
    public static final String ATTRIBUTE_SESSIONCONTEXTATTR = "SESSIONCONTEXTATTR";
    public static final String ATTRIBUTE_IP = "IP";
    private static final String TAG = "UpStream";
    private int readIdleTime;
    private int writeIdleTime;
    private final int[] lock = new int[0];
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
            try {
                listener.getObject().sessionCreated(getSessionContext(session));
            }catch (Throwable throwable){
                LoggerEx.error(TAG, "SessionCreated error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionOpened(getSessionContext(session));
            }catch (Throwable throwable){
                LoggerEx.error(TAG, "SessionOpened error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionClosed(getSessionContext(session));
            }catch (Throwable throwable){
                LoggerEx.error(TAG, "SessionClosed error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionIdle(getSessionContext(session));
            }catch (Throwable throwable){
                LoggerEx.error(TAG, "SessionIdle error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause)
            throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().exceptionCaught(getSessionContext(session), cause);
            }catch (Throwable throwable){
                LoggerEx.error(TAG, "ExceptionCaught error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        SessionContextAttr sessionContextAttr = null;
        try {
            sessionContextAttr = (SessionContextAttr) getSessionContext(session).getAttribute(ATTRIBUTE_SESSIONCONTEXTATTR);
            if (message != null && (message instanceof Data)) {
                Data pack = (Data) message;
                Byte type = pack.getType();
                if (type != Pack.TYPE_IN_IDENTITY && sessionContextAttr == null)
                    throw new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_MISSING_ONLINEUSER, "Online user is missing for receiving message");

                GroovyObjectEx<ProxyMessageReceivedListener> listener = proxyUpStreamAnnotationHandler.getMessageReceivedMap().get(type);
                if (listener != null) {
                    Class<? extends Data> dataClass = listener.getObject().getDataClass();
                    if (dataClass != null) {
                        try {
                            listener.getObject().messageReceived(pack, getSessionContext(session));
                        }catch (Throwable throwable){
                            LoggerEx.error(TAG, "MessageReceived error, class: "+ listener.getObject().getClass() +",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                        }
                    }
                }
            } else {
                if (message != null)
                    LoggerEx.error(TAG, "Unexpected message type " + message.getClass() + " message " + message + " session " + session);
            }
        } catch (Throwable t) {
//            LoggerEx.error(TAG, "Message " + message + " received failed, " + t + " message: " + t.getMessage());
//            CoreException coreException = null;
//            if (t instanceof CoreException)
//                coreException = (CoreException) t;
//            if (coreException == null)
//                coreException = new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_UNKNOWN, "Unknown error occured while receiving message from tcp channel, channel " + session + " message " + message + " error " + t.getMessage());
//            if (coreException.getCode() >= GatewayErrorCodes.TCPCHANNEL_CLOSE_START && coreException.getCode() < GatewayErrorCodes.TCPCHANNEL_CLOSE_END) {
//                if (onlineUser != null) {
//                    Channel channel = (Channel) session.getAttribute(ATTRIBUTE_CHANNEL);
//                    if (channel != null) {
//                        onlineUser.removeChannel(channel, ChannelListener.CLOSE_ERROR);
//                    }
//                } else {
//                    session.close(false);
//                }
//            } else if (coreException.getCode() >= GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_START && coreException.getCode() < GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_END) {
//                if (onlineUser != null) {
//                    Channel channel = (Channel) session.getAttribute(ATTRIBUTE_CHANNEL);
//                    if (channel != null) {
//                        onlineUser.removeChannel(channel, ChannelListener.CLOSE_ERROR);
//                    }
//                } else {
//                    session.close(true);
//                }
//            } else {
//                session.close(true);
//            }
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

    private SessionContext getSessionContext(IoSession session) {
        SessionContext sessionContext = (SessionContext) session.getAttribute(ATTRIBUTE_SESSIONCONTEXT);
        if (sessionContext == null) {
            synchronized (lock) {
                sessionContext = new MinaSessionContext(session);
                SessionContext sessionContextOld = (SessionContext) session.getAttribute(ATTRIBUTE_SESSIONCONTEXT);
                if(sessionContextOld != null){
                    sessionContext = sessionContextOld;
                }else {
                    session.setAttribute(ATTRIBUTE_SESSIONCONTEXT, sessionContext);
                }
            }
        }
        return sessionContext;
    }

}
