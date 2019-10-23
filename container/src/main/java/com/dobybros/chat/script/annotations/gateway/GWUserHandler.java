package com.dobybros.chat.script.annotations.gateway;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.script.annotations.handler.ServiceUserSessionAnnotationHandler;
import com.dobybros.chat.utils.SingleThreadQueue;
import org.apache.mina.core.session.IoSession;
import script.groovy.object.GroovyObjectEx;

public class GWUserHandler extends SingleThreadQueue.Handler<GWUserParams> {
    private static final String TAG = GWUserHandler.class.getSimpleName();
    private GroovyObjectEx<SessionListener> sessionListener;
    private GatewayGroovyRuntime runtime;
    public GWUserHandler(GroovyObjectEx<SessionListener> sessionListener, GatewayGroovyRuntime runtime) {
        this.sessionListener = sessionListener;
        this.runtime = runtime;
    }
    @Override
    public boolean handle(GWUserParams gwUserParams) throws CoreException {
        switch (gwUserParams.action) {
            case GWUserParams.ACTION_CHANNELCLOSED:
                if(sessionListener != null) {
                    try {
                        sessionListener.getObject().channelClosed(gwUserParams.userId, gwUserParams.service, gwUserParams.terminal, gwUserParams.close);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " closed by " + gwUserParams.userId + " failed, " + t.getMessage());
                    }
                    runtime.channelCreatedMessage.remove(gwUserParams.terminal);
                } else {
                    if (runtime != null) {
                        ServiceUserSessionAnnotationHandler handler = (ServiceUserSessionAnnotationHandler) runtime.getClassAnnotationHandler(ServiceUserSessionAnnotationHandler.class);
                        if (handler != null) {
                            ServiceUserSessionListener listener = handler.getAnnotatedListener(gwUserParams.userId, gwUserParams.service);
                            if (listener != null)
                                try {
                                    listener.channelClosed(gwUserParams.terminal, gwUserParams.close);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " closed by " + gwUserParams.userId + "@" + gwUserParams.service + " failed, " + t.getMessage());
                                }
                        }
                    }
                }
                break;
            case GWUserParams.ACTION_CHANNELCREATED:
                if (sessionListener != null) {
                    try {
                        sessionListener.getObject().channelCreated(gwUserParams.userId, gwUserParams.service, gwUserParams.terminal);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " created by " + gwUserParams.userId + " failed, " + t.getMessage());
                    }
                } else {
                    if (runtime != null) {
                        ServiceUserSessionAnnotationHandler handler = (ServiceUserSessionAnnotationHandler) runtime.getClassAnnotationHandler(ServiceUserSessionAnnotationHandler.class);
                        if (handler != null) {
                            ServiceUserSessionListener listener = handler.getAnnotatedListener(gwUserParams.userId, gwUserParams.service);
                            if (listener != null)
                                try {
                                    listener.channelCreated(gwUserParams.terminal);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " created by " + gwUserParams.userId + "@" + gwUserParams.service + " failed, " + t.getMessage());
                                }
                        }
                    }
                }
                // 发送channel还没有创建时漏发的消息
                PendingMessageContainer container = runtime.channelCreatedMessage.get(PendingMessageContainer.getKey(gwUserParams.userId, runtime.getService(), gwUserParams.terminal));
                if(container != null){
                    synchronized (container) {
                        container.type = PendingMessageContainer.CHANNELCREATED;
                        IoSession session = container.session;
                        if(session != null){
                            if(container.pendingMessages != null && container.pendingMessages.size() > 0){
                                for(Object message : container.pendingMessages){
                                    try {
                                        runtime.messageReceived((Message) message, gwUserParams.terminal, (IoSession) session, container.needTcpResult);
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        LoggerEx.error(TAG, "Handle message " + gwUserParams.terminal + " created by " + gwUserParams.userId + " failed, " + t.getMessage());
                                    }
                                }
                            }
                            if(container.pendingDatas != null && container.pendingDatas.size() > 0){
                                for(Object data : container.pendingDatas){
                                    try {
                                        runtime.dataReceived((Message) data, gwUserParams.terminal, (IoSession) session);
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        LoggerEx.error(TAG, "Handle message " + gwUserParams.terminal + " created by " + gwUserParams.userId + " failed, " + t.getMessage());
                                    }
                                }
                            }
                            container.pendingDatas = null;
                            container.pendingMessages = null;
                            container.session = null;
                        }
                    }
                }
                break;
            case GWUserParams.ACTION_SESSIONCLOSED:
                if(sessionListener != null) {
                    try {
                        sessionListener.getObject().sessionClosed(gwUserParams.userId, gwUserParams.service, gwUserParams.close);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + " service " + gwUserParams.service + " close failed, " + t.getMessage());
                    }
                } else {
                    if (runtime != null) {
                        ServiceUserSessionAnnotationHandler handler = (ServiceUserSessionAnnotationHandler) runtime.getClassAnnotationHandler(ServiceUserSessionAnnotationHandler.class);
                        if (handler != null) {
                            ServiceUserSessionListener listener = handler.getAnnotatedListener(gwUserParams.userId, gwUserParams.service);
                            if (listener != null) {
                                try {
                                    listener.sessionClosed(gwUserParams.close);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + "@" + gwUserParams.service + " close failed, " + t.getMessage());
                                }
                                try {
                                    handler.removeListeners(gwUserParams.userId, gwUserParams.service);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + "@" + gwUserParams.service + " close failed, " + t.getMessage());
                                }
                            }
                        }
                    }
                }
                break;
            case GWUserParams.ACTION_SESSIONCREATED:
                if(sessionListener != null) {
                    try {
                        sessionListener.getObject().sessionCreated(gwUserParams.userId, gwUserParams.service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + " service " + gwUserParams.service + " sessionCreated failed, " + t.getMessage());
                    }
                } else {
                    if (runtime != null) {
                        ServiceUserSessionAnnotationHandler handler = (ServiceUserSessionAnnotationHandler) runtime.getClassAnnotationHandler(ServiceUserSessionAnnotationHandler.class);
                        if (handler != null) {
                            ServiceUserSessionListener listener = handler.createAnnotatedListener(gwUserParams.userId, gwUserParams.service);
                            if (listener != null)
                                try {
                                    listener.sessionCreated();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + "@" + gwUserParams.service + " created failed, " + t.getMessage());
                                }
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }
}
