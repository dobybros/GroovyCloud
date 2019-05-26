package com.dobybros.chat.script.annotations.gateway;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.utils.SingleThreadQueue;
import org.apache.mina.core.session.IoSession;
import script.groovy.object.GroovyObjectEx;

import java.util.List;

public class GWUserHandler extends SingleThreadQueue.Handler<GWUserParams> {
    private static final String TAG = GWUserHandler.class.getSimpleName();
    private List<GroovyObjectEx<SessionListener>> sessionListeners;
    private GatewayGroovyRuntime runtime;
    public GWUserHandler(List<GroovyObjectEx<SessionListener>> sessionListeners, GatewayGroovyRuntime runtime) {
        this.sessionListeners = sessionListeners;
        this.runtime = runtime;
    }
    @Override
    public boolean handle(GWUserParams gwUserParams) throws CoreException {
        switch (gwUserParams.action) {
            case GWUserParams.ACTION_CHANNELCLOSED:
                if(sessionListeners != null) {
                    for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
                        try {
                            listener.getObject().channelClosed(gwUserParams.userId, gwUserParams.service, gwUserParams.terminal, gwUserParams.close);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " closed by " + gwUserParams.userId + " failed, " + t.getMessage());
                        }
                        runtime.channelCreatedMessage.remove(gwUserParams.terminal);
                    }
                }
                break;
            case GWUserParams.ACTION_CHANNELCREATED:
                for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
                    try {
                        listener.getObject().channelCreated(gwUserParams.userId, gwUserParams.service, gwUserParams.terminal);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle channel " + gwUserParams.terminal + " created by " + gwUserParams.userId + " failed, " + t.getMessage());
                    }
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
                }
                break;
            case GWUserParams.ACTION_SESSIONCLOSED:
                if(sessionListeners != null) {
                    for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
                        try {
                            listener.getObject().sessionClosed(gwUserParams.userId, gwUserParams.service, gwUserParams.close);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + " service " + gwUserParams.service + " close failed, " + t.getMessage());
                        }
                    }
                }
                break;
            case GWUserParams.ACTION_SESSIONCREATED:
                if(sessionListeners != null) {
                    for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
                        try {
                            listener.getObject().sessionCreated(gwUserParams.userId, gwUserParams.service);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            LoggerEx.error(TAG, "Handle session " + gwUserParams.userId + " service " + gwUserParams.service + " sessionCreated failed, " + t.getMessage());
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
