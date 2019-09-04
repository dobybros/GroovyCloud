package com.dobybros.chat.script.annotations.gateway;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import com.alibaba.fastjson.JSONObject;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.open.data.Message;
import com.dobybros.chat.open.data.MsgResult;
import com.dobybros.chat.open.data.UserStatus;
import com.dobybros.chat.utils.SingleThreadQueue;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.channels.data.OutgoingData;
import com.dobybros.gateway.channels.data.Result;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.dobybros.gateway.open.GatewayMSGServers;
import com.dobybros.gateway.pack.Pack;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.script.MyBaseRuntime;
import com.docker.utils.SpringContextUtil;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GatewayGroovyRuntime extends MyBaseRuntime {
    private static final String TAG = GatewayGroovyRuntime.class.getSimpleName();
    private List<GroovyObjectEx<SessionListener>> sessionListeners;
    private List<GroovyObjectEx<MessageNotReceivedListener>> messageNotReceivedListeners;

//	private ReadWriteLock sessionLock = new ReentrantReadWriteLock();
//	private ReadWriteLock messageNotReceivedLock = new ReentrantReadWriteLock();
    private ConcurrentHashMap<String, SingleThreadQueue> singleThreadMap = new ConcurrentHashMap<>();
    private OnlineUserManager onlineUserManager = (OnlineUserManager) SpringContextUtil.getBean("onlineUserManager");
    public ConcurrentHashMap<String, PendingMessageContainer> channelCreatedMessage = new ConcurrentHashMap();
    @Override
    public void prepare(String service, Properties properties, String localScriptPath) {
        super.prepare(service, properties, localScriptPath);
        final GatewayGroovyRuntime instance = this;
        addClassAnnotationHandler((ClassAnnotationHandler) SpringContextUtil.getBean("rpcServer"));
        addClassAnnotationHandler((ClassAnnotationHandler) SpringContextUtil.getBean("rpcServerSsl"));
        addClassAnnotationHandler(new ClassAnnotationHandler() {
            @Override
            public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime runtime) {
                return SessionHandler.class;
            }

            @Override
            public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                               MyGroovyClassLoader cl) {
                if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
                    StringBuilder uriLogs = new StringBuilder(
                            "\r\n---------------------------------------\r\n");

                    List<GroovyObjectEx<SessionListener>> newChannelRegisteredMap = new ArrayList<>();
                    Set<String> keys = annotatedClassMap.keySet();
                    GroovyRuntime groovyRuntime = instance;
                    for (String key : keys) {
                        Class<?> groovyClass = annotatedClassMap.get(key);
                        if (groovyClass != null) {
                            SessionHandler messageReceivedAnnotation = groovyClass.getAnnotation(SessionHandler.class);
                            if (messageReceivedAnnotation != null) {
                                GroovyObjectEx<SessionListener> listeners = groovyRuntime
                                        .create(groovyClass);
                                if (listeners != null) {
                                    uriLogs.append("ChannelListener #" + groovyClass + "\r\n");
                                    newChannelRegisteredMap.add(listeners);
                                }
                            }
                        }
                    }
//					instance.sessionLock.writeLock().lock();
//					try {
                    instance.sessionListeners = newChannelRegisteredMap;
//					} finally {
//						instance.sessionLock.writeLock().unlock();
//					}
                    uriLogs.append("---------------------------------------");
                    LoggerEx.info(TAG, uriLogs.toString());
                }
            }
        });

        addClassAnnotationHandler(new ClassAnnotationHandler() {
            @Override
            public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime runtime) {
                return MessageNotReceived.class;
            }

            @Override
            public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                               MyGroovyClassLoader cl) {
                if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
                    StringBuilder uriLogs = new StringBuilder(
                            "\r\n---------------------------------------\r\n");

                    List<GroovyObjectEx<MessageNotReceivedListener>> newMessageNotReceivedMap = new ArrayList<>();
                    Set<String> keys = annotatedClassMap.keySet();
                    GroovyRuntime groovyRuntime = instance;
                    for (String key : keys) {
                        Class<?> groovyClass = annotatedClassMap.get(key);
                        if (groovyClass != null) {
                            MessageNotReceived messageNotReceivedAnnotation = groovyClass.getAnnotation(MessageNotReceived.class);
                            if (messageNotReceivedAnnotation != null) {
                                GroovyObjectEx<MessageNotReceivedListener> messageNotReceivedObj = groovyRuntime
                                        .create(groovyClass);
                                if (messageNotReceivedObj != null) {
                                    uriLogs.append("MessageNotReceivedListener #" + groovyClass + "\r\n");
                                    newMessageNotReceivedMap.add(messageNotReceivedObj);
                                }
                            }
                        }
                    }
//					instance.messageNotReceivedLock.writeLock().lock();
//					try {
                    instance.messageNotReceivedListeners = newMessageNotReceivedMap;
//					} finally {
//						instance.messageNotReceivedLock.writeLock().unlock();
//					}
                    uriLogs.append("---------------------------------------");
                    LoggerEx.info(TAG, uriLogs.toString());
                }
            }
        });
    }

    /**
     * 这个接口会被调用两次， 一次是http的authorized的时候， 另一次是从tcp通道收到identity的时候
     *
     * @param userId
     * @param terminal
     * @return 返回会被踢下线的其他通道。
     */
    public List<Integer> channelRegistered(String userId, String service, Integer terminal) {
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                try {
                    return listener.getObject().channelRegisterd(userId, service, terminal);
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.error(TAG, "Handle channel " + terminal + " regitered by " + userId + " failed, " + t.getMessage());
                }
            }
        }
//		} finally {
//			sessionLock.readLock().unlock();
//		}
        return null;
    }

    public void channelCreated(String userId, String service, Integer terminal) {
        PendingMessageContainer container = new PendingMessageContainer();
        channelCreatedMessage.put(PendingMessageContainer.getKey(userId, getService(), terminal), container);//0为channel还没有创建,1为已经创建
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            SingleThreadQueue<GWUserParams> queue = singleThreadMap.get(userId + "@" + service);
            if (queue == null) {
                queue = new SingleThreadQueue<GWUserParams>("GWUserHandler userId " + userId + " service " + service, new ConcurrentLinkedQueue<>(), ServerStart.getInstance().getCoreThreadPoolExecutor(), new GWUserHandler(sessionListeners, this));
                SingleThreadQueue old = singleThreadMap.putIfAbsent(userId + "@" + service, queue);
                if (old != null)
                    queue = old;
            }
            queue.offerAndStart(new GWUserParams(GWUserParams.ACTION_CHANNELCREATED, userId, service, terminal));
        }
//        queue.offerAndStart(new );
//			if(sessionListeners != null) {
//				for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
//					try {
//						listener.getObject().channelCreated(userId, service, terminal);
//					} catch (Throwable t) {
//						t.printStackTrace();
//						LoggerEx.error(TAG, "Handle channel " + terminal + " created by " + userId + " failed, " + t.getMessage());
//					}
//				}
//			}
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    public void channelClosed(String userId, String service, Integer terminal, int close) {
        if (sessionListeners != null) {
            SingleThreadQueue<GWUserParams> queue = singleThreadMap.get(userId + "@" + service);
            if (queue == null) {
                queue = new SingleThreadQueue<GWUserParams>("GWUserHandler userId " + userId + " service " + service, new ConcurrentLinkedQueue<>(), ServerStart.getInstance().getCoreThreadPoolExecutor(), new GWUserHandler(sessionListeners, this));
                SingleThreadQueue old = singleThreadMap.putIfAbsent(userId + "@" + service, queue);
                if (old != null)
                    queue = old;
            }
            queue.offerAndStart(new GWUserParams(GWUserParams.ACTION_CHANNELCLOSED, userId, service, terminal, close));
        }

//		sessionLock.readLock().lock();
//		try {
//			if(sessionListeners != null) {
//				for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
//					try {
//						listener.getObject().channelClosed(userId, service, terminal, close);
//					} catch (Throwable t) {
//						t.printStackTrace();
//						LoggerEx.error(TAG, "Handle channel " + terminal + " closed by " + userId + " failed, " + t.getMessage());
//					}
//				}
//			}
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    public void sessionClosed(String userId, String service, int close) {
        if (sessionListeners != null) {
            SingleThreadQueue<GWUserParams> queue = singleThreadMap.get(userId + "@" + service);
            if (queue == null) {
                queue = new SingleThreadQueue<GWUserParams>("GWUserHandler userId " + userId + " service " + service, new ConcurrentLinkedQueue<>(), ServerStart.getInstance().getCoreThreadPoolExecutor(), new GWUserHandler(sessionListeners, this));
                SingleThreadQueue old = singleThreadMap.putIfAbsent(userId + "@" + service, queue);
                if (old != null)
                    queue = old;
            }
            queue.offerAndStart(new GWUserParams(GWUserParams.ACTION_SESSIONCLOSED, userId, service, null, close));
            singleThreadMap.remove(userId + "@" + service);
        }

//		sessionLock.readLock().lock();
//		try {
//			if(sessionListeners != null) {
//				for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
//					try {
//						listener.getObject().sessionClosed(userId, service, close);
//					} catch (Throwable t) {
//						t.printStackTrace();
//						LoggerEx.error(TAG, "Handle session " + userId + " service " + service + " close failed, " + t.getMessage());
//					}
//				}
//			}
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    public void sessionCreated(String userId, String service) {
        if (sessionListeners != null) {
            SingleThreadQueue<GWUserParams> queue = singleThreadMap.get(userId + "@" + service);
            if (queue == null) {
                queue = new SingleThreadQueue<GWUserParams>("GWUserHandler userId " + userId + " service " + service, new ConcurrentLinkedQueue<>(), ServerStart.getInstance().getCoreThreadPoolExecutor(), new GWUserHandler(sessionListeners, this));
                SingleThreadQueue old = singleThreadMap.putIfAbsent(userId + "@" + service, queue);
                if (old != null)
                    queue = old;
            }
            queue.offerAndStart(new GWUserParams(GWUserParams.ACTION_SESSIONCREATED, userId, service));
        }

//		sessionLock.readLock().lock();
//		try {
//			if(sessionListeners != null) {
//				for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
//					try {
//						listener.getObject().sessionCreated(userId, service);
//					} catch (Throwable t) {
//						t.printStackTrace();
//						LoggerEx.error(TAG, "Handle session " + userId + " service " + service + " sessionCreated failed, " + t.getMessage());
//					}
//				}
//			}
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    public Long getMaxInactiveInterval(String userId, String service) {
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                try {
                    return listener.getObject().getMaxInactiveInterval(userId, service);
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.error(TAG, "Handle session " + userId + " service " + service + " getMaxInactiveInterval failed, " + t.getMessage());
                }
            }
        }
//		} finally {
//			sessionLock.readLock().unlock();
//		}
        return null;
    }

    /**
     * 可以修改Message里的内容
     *
     * @param message
     * @return 非空就不用发送消息了， 为空时会继续发送。 默认为为空
     */
    public void messageReceived(Message message, Integer terminal, IoSession session, boolean needTcpResult) {
        OnlineUser onlineUser = (OnlineUser) session.getAttribute(UpStreamHandler.ATTRIBUTE_ONLINEUSER);
        if (onlineUser == null) {
            LoggerEx.error(TAG, "Online user " + message.getUserId() + " not found while receiving message " + message);
            return;
        }
//            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while receiving message " + message);
        PendingMessageContainer container = channelCreatedMessage.get(PendingMessageContainer.getKey(onlineUser.getUserId(), getService(), terminal));
        if (container == null) {
            LoggerEx.error(TAG, "channel is not created, terminal: " + terminal + " message: " + JSONObject.toJSONString(message) + " session: " + JSONObject.toJSONString(session));
            return;
        } else {
            if (container.type == PendingMessageContainer.CHANNELCREATED) {
                if (sessionListeners != null) {
                    for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                        ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                            try {
                                MsgResult msgResult = listener.getObject().messageReceived(message, terminal);

                                OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(message.getService());
                                if (serviceUser == null)
                                    throw new CoreException(GatewayErrorCodes.ERROR_ONLINESERVICEUSER_NULL, "Online service user " + message.getUserId() + "@" + message.getService() + " not found while sending result " + msgResult + " for message " + message);

                                if (msgResult != null && msgResult.isShouldIntercept()) {
                                    Result result = DataVersioning.getResultData(session, msgResult.getCode(), null, message.getClientId());
                                    result.setContentEncode(msgResult.getDataEncode());
                                    result.setContent(msgResult.getData());
                                    result.setTime(message.getTime());
                                    if (result != null) {
                                        Pack resultPack = DataVersioning.getDataPack(session, result);
                                        if (resultPack != null) {
                                            session.write(resultPack);
                                        }
                                    }
                                }

                                Result resultEvent = (Result) serviceUser.sendTopic(message, needTcpResult, theTopic -> {
//						onlineUserManager.sendEvent(msg, onlineUser);
                                    try {
                                        GatewayMSGServers.getInstance().sendMessage(message, terminal, null);
                                    } catch (CoreException e) {
                                        e.printStackTrace();
                                        LoggerEx.error(TAG, "Handle message " + message + " sendMessage failed, " + e.getMessage());
                                    }
                                });

                                // 组织result
                                if (msgResult != null) {
                                    if (msgResult.getCode() != null)
                                        resultEvent.setCode(msgResult.getCode());
                                    resultEvent.setContentEncode(msgResult.getDataEncode());
                                    resultEvent.setContent(msgResult.getData());
                                    resultEvent.setTime(message.getTime());
                                    if (resultEvent != null) {
                                        Pack resultPack = DataVersioning.getDataPack(session, resultEvent);
                                        if (resultPack != null) {
                                            session.write(resultPack);
                                        }
                                    }
                                }
                            } catch (CoreException e) {
                                e.printStackTrace();
                                LoggerEx.error(TAG, "Handle message " + message + " messageReceived failed, " + e.getMessage());
                            }
                        });
                    }
                }
            } else if (container.type == PendingMessageContainer.CHANNELNOTCREATED) {
                //和异步调用groovy层的channelCreated之后的更改为CREATED状态做同步处理， 防止丢消息
                synchronized (container) {
                    if(container.type == PendingMessageContainer.CHANNELNOTCREATED) {
                        if(container.pendingMessages == null) {
                            container.pendingMessages = new ArrayList<>();
                        }
                        container.pendingMessages.add(message);
                        container.session = session;
                        container.needTcpResult = needTcpResult;
                    } else {
                        //如果再添加消息过程中， container的type从NOTCREATED改变到了CREATED， 就再执行一边该方法， 确保不会丢消息
                        messageReceived(message, terminal, session, needTcpResult);
                    }
                }

//                Object channelCreateMessage = container.get("message");
//                List channelCreateMessageList = (List) channelCreateMessage;
//                container.put("session", session);
//                container.put("needTcpResult", needTcpResult);
//                if (channelCreateMessageList == null) {
//                    channelCreateMessageList = new ArrayList();
//                    channelCreateMessageList.add(message);
//                    container.put("message", channelCreateMessageList);
//                } else {
//                    channelCreateMessageList.add(message);
//                    container.put("message", channelCreateMessageList);
//                }
            }
        }
    }

    /**
     * 可以修改Message里的内容
     *
     * @param message
     * @return 非空就不用发送消息了， 为空时会继续发送。 默认为为空
     */
    public void dataReceived(Message message, Integer terminal, IoSession session) {
        OnlineUser onlineUser = (OnlineUser) session.getAttribute(UpStreamHandler.ATTRIBUTE_ONLINEUSER);
        if (onlineUser == null) {
            LoggerEx.error(TAG, "Online user " + message.getUserId() + " not found while receiving message " + message);
            return;
        }
//            throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while receiving message " + message);
        PendingMessageContainer container = channelCreatedMessage.get(PendingMessageContainer.getKey(onlineUser.getUserId(), getService(), terminal));
//        Map terminalObject = channelCreatedMessage.get(PendingMessageContainer.getKey(userId, getService()));
        if (container == null) {
            LoggerEx.error(TAG, "channel is not created, terminal: " + terminal + " message: " + JSONObject.toJSONString(message) + " session: " + JSONObject.toJSONString(session));
            return;
        } else {
            if (container.type == PendingMessageContainer.CHANNELCREATED) {
                if (sessionListeners != null) {
                    for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                        ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                            try {
                                MsgResult msgResult = listener.getObject().dataReceived(message, terminal);
//                                OnlineUser onlineUser = onlineUserManager.getOnlineUser(message.getUserId());
//                                if (onlineUser == null)
//                                    throw new CoreException(GatewayErrorCodes.ERROR_ONLINEUSER_NULL, "Online user " + message.getUserId() + " not found while sending result " + msgResult + " for message " + message);
                                OnlineServiceUser serviceUser = onlineUser.getOnlineServiceUser(message.getService());
                                if (serviceUser == null)
                                    throw new CoreException(GatewayErrorCodes.ERROR_ONLINESERVICEUSER_NULL, "Online service user " + message.getUserId() + "@" + message.getService() + " not found while sending result " + msgResult + " for message " + message);

                                if (msgResult != null && msgResult.isShouldIntercept()) {
                                    Result result = DataVersioning.getResultData(session, msgResult.getCode(), null, message.getClientId());
                                    result.setContentEncode(msgResult.getDataEncode());
                                    result.setContent(msgResult.getData());
                                    result.setTime(message.getTime());
                                    if (result != null) {
                                        Pack resultPack = DataVersioning.getDataPack(session, result);
                                        if (resultPack != null) {
                                            session.write(resultPack);
                                        }
                                    }
                                    return;
                                }

                                if (message.getService().equals(message.getReceiverService())) {
                                    Collection<String> receiverIds = message.getReceiverIds();
                                    if (receiverIds != null && receiverIds.contains(message.getUserId())) {
                                        //If sender is also the receiver, send message to sender excluded the terminal where sent the message.
                                        OutgoingData out = new OutgoingData();
                                        out.fromMessage(message);
                                        serviceUser.pushToChannels(out, terminal, null);
                                    }
                                }

                                // 组织result
                                if (msgResult != null) {
                                    Result resultEvent = DataVersioning.getResultData(session, msgResult.getCode(), null, message.getClientId());
                                    resultEvent.setContentEncode(msgResult.getDataEncode());
                                    resultEvent.setContent(msgResult.getData());
                                    resultEvent.setTime(message.getTime());
                                    if (resultEvent != null) {
                                        Pack resultPack = DataVersioning.getDataPack(session, resultEvent);
                                        if (resultPack != null) {
                                            session.write(resultPack);
                                        }
                                    }
                                }
                            } catch (CoreException e) {
                                e.printStackTrace();
                                LoggerEx.error(TAG, "Handle message " + message + " messageReceived failed, " + e.getMessage());
                            }
                        });
                    }
                }
            } else if (container.type == PendingMessageContainer.CHANNELNOTCREATED) {
                synchronized (container) {
                    if(container.type == PendingMessageContainer.CHANNELNOTCREATED) {
                        if(container.pendingDatas == null) {
                            container.pendingDatas = new ArrayList<>();
                        }
                        //多线程安全问题,如果同时进来两个线程,第二个list会把第一个覆盖掉,导致丢掉一个message
                        container.pendingDatas.add(message);
                        container.session = session;
                    } else {
                        //如果再添加消息过程中， container的type从NOTCREATED改变到了CREATED， 就再执行一边该方法， 确保不会丢消息
                        dataReceived(message, terminal, session);
                    }
                }
            }
        }
    }

/*
    public void messageReceivedAsync(Message message, Integer terminal) {
//		sessionLock.readLock().lock();
//		try {
        if(sessionListeners != null) {
            for(GroovyObjectEx<SessionListener> listener : sessionListeners) {
                try {
                    if(listener.getObject().needAsync()) {
                        ServerStart.getInstance().getThreadPool().execute(() -> {
                            try {
                                listener.getObject().messageReceivedAsync(message, terminal);
                            } catch (CoreException e) {
                                e.printStackTrace();
                                LoggerEx.error(TAG, "Handle message " + message + " messageReceived failed, " + e.getMessage());
                            }
                        });
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.error(TAG, "Handle message " + message + " messageReceived failed, " + t.getMessage());
                }
            }
        }

//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }
*/

    /**
     * 因为用户不存在导致消息没有收到的回掉， 主要用于离线消息的推送， 例如苹果的APN
     * <p>
     * 此方法面向过程，service多版本时不保证会调到相同service中去，不应发生内存共享的情况，如果不幸发生了这种情况，需要注意service多版本的问题
     *
     * @param message       Message里的receiverIds是所有没有收到消息的人
     * @param userStatusMap 这个Map是一个参考， 不等于是所有没有收到消息的人。 所有没有收到消息的人信息在Message里。
     */
    public void messageNotReceived(Message message, Map<String, UserStatus> userStatusMap) {
//		messageNotReceivedLock.readLock().lock();
//		try {
        if (messageNotReceivedListeners != null) {
            for (GroovyObjectEx<MessageNotReceivedListener> listener : messageNotReceivedListeners) {
                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                    try {
                        listener.getObject().messageNotReceived(message, userStatusMap);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle message " + message + " messageNotReceived failed, " + t.getMessage());
                    }
                });
            }
        }
//		} finally {
//			messageNotReceivedLock.readLock().unlock();
//		}
    }

    public void messageSent(Data data, Integer excludeTerminal, Integer toTerminal, String userId, String service) {
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                    try {
                        listener.getObject().messageSent(data, excludeTerminal, toTerminal, userId, service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle classroom " + data + " excludeTerminal " + excludeTerminal + " toTerminal " + toTerminal + " messageSent failed, " + t.getMessage());
                    }
                });
            }
        }
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    public void messageReceivedFromUsers(Message message, String receiverId, String receiverService) {
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                    try {
                        listener.getObject().messageReceivedFromUsers(message, receiverId, receiverService);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle message " + message + " messageReceivedFromUsers failed, " + t.getMessage());
                    }
                });
            }
        }
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }

    /**
     * @param userId
     * @return 非空就不用发送消息了， 为空时会继续发送。 默认为为空
     */
    public void pingReceived(String userId, String service, Integer terminal) {
//		sessionLock.readLock().lock();
//		try {
        if (sessionListeners != null) {
            for (GroovyObjectEx<SessionListener> listener : sessionListeners) {
                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                    try {
                        listener.getObject().pingReceived(userId, service, terminal);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle pingReceived failed, " + t.getMessage());
                    }
                });
            }
        }
//		} finally {
//			sessionLock.readLock().unlock();
//		}
    }
}
