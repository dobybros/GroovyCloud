package com.proxy.im;

import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.binary.data.Data;
import com.proxy.im.mina.MinaSessionContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import script.groovy.object.GroovyObjectEx;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProxyUpStreamHandler extends IoHandlerAdapter {
    public static final String ATTRIBUTE_TIMERTASK_IDENTITY = "IDENTITY_TIMERTAKS";
    public static final String ATTRIBUTE_SESSIONCONTEXT = "SESSIONCONTEXT";
    public static final String ATTRIBUTE_VERSION = "VERSION";
    public static final String ATTRIBUTE_SESSIONCONTEXTATTR = "SESSIONCONTEXTATTR";
    public static final String ATTRIBUTE_IP = "IP";
    private static final String TAG = "ProxyUpStreamHandler";
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
        if(session.getAttribute(ATTRIBUTE_IP) == null){
            // 获取ip
            String address = session.getRemoteAddress().toString();
            address = address.replace("/", "");
            String[] addresses = address.split(":");
            if (addresses.length > 0)
                session.setAttribute(ATTRIBUTE_IP, addresses[0]);
        }
        TimerEx.schedule(task, TimeUnit.SECONDS.toMillis(8));
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionCreated(getSessionContext(session));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionCreated error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionOpened(getSessionContext(session));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionOpened error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionClosed(getSessionContext(session));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionClosed error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionIdle(getSessionContext(session));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionIdle error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause)
            throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().exceptionCaught(getSessionContext(session), cause);
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "ExceptionCaught error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
            if (message != null && (message instanceof Data)) {
                Data pack = (Data) message;
                List<GroovyObjectEx<ProxyMessageReceivedListener>> listeners = proxyUpStreamAnnotationHandler.getProxyMessageReceivedListeners();
                if (listeners != null && !listeners.isEmpty()) {
                    for (GroovyObjectEx<ProxyMessageReceivedListener> listener : listeners){
                        try {
                            listener.getObject().messageReceived(pack, getSessionContext(session));
                        }catch (Throwable throwable){
                            LoggerEx.error(TAG, "MessageReceived error, class: " + listener.getObject().getClass() + ",data: " + message + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                        }
                    }
                }
            } else {
                if (message != null)
                    LoggerEx.error(TAG, "Unexpected message type " + message.getClass() + " message " + message + " session " + session);
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
                if (sessionContextOld != null) {
                    sessionContext = sessionContextOld;
                } else {
                    session.setAttribute(ATTRIBUTE_SESSIONCONTEXT, sessionContext);
                }
            }
        }
        return sessionContext;
    }

}
