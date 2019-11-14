package com.docker.rpc.remote.skeleton;

import chat.errors.ChatErrorCodes;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.async.AsyncRuntimeException;
import com.docker.rpc.impl.RMIServer;
import com.docker.rpc.remote.RpcServerInterceptor;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLimitRpcServerInterceptor implements RpcServerInterceptor {
    private final String TAG = ConcurrentLimitRpcServerInterceptor.class.getSimpleName();
    private Integer concurrentLimit;
    private ConcurrentLinkedQueue<InternalRequestServer> waitingList = new ConcurrentLinkedQueue<>();
    private Map<String, InternalRequestServer> retryMap = new ConcurrentHashMap<>();
    private int counter = 0;
    private int[] lock = new int[0];
    private AtomicInteger counterWaiting = new AtomicInteger(0);
    private Integer queueLimit;
    private String classMethod;

    public ConcurrentLimitRpcServerInterceptor(Integer concurrentLimit, Integer queueLimit, String classMethod) {
        this.concurrentLimit = concurrentLimit;
        this.classMethod = classMethod;
        this.queueLimit = queueLimit;
    }

    @Override
    public Object invoke(RPCRequest rpcRequest, RMIServer rmiServer) {
        boolean beyond = false;
        if (counter >= concurrentLimit) {
            synchronized (lock) {
                if(counter >= concurrentLimit) {
                    beyond = true;
                }
            }
        }
        String callbackId = ((MethodRequest) rpcRequest).getCallbackFutureId();
        if(retryMap.containsKey(callbackId) || !beyond) {
            if (callbackId != null) {
                if (!retryMap.containsKey(callbackId)) {
                    synchronized (lock) {
                        if(counter < concurrentLimit) {
                            counter++;
                            return true;
                        } else {
                            beyond = true;
                        }
                    }
                } else {
                    retryMap.remove(callbackId);
                    return true;
                }
            }
        }
        if(beyond) {
            InternalRequestServer internalRequestServer = new InternalRequestServer(rpcRequest, rmiServer);
            if (counterWaiting.get() < queueLimit) {
                waitingList.offer(internalRequestServer);
                counterWaiting.incrementAndGet();
            } else {
                LoggerEx.warn(TAG, "The waiting queue has already full, queueLimit: " + queueLimit + " !!!classMethod: " + classMethod);
                throw new AsyncRuntimeException(ChatErrorCodes.ERROR_REMOTEERVICE_CONCURRENTLIMIT, "The waiting queue has already full!!!classMethod: " + classMethod);
            }
        }
        return false;
    }

    @Override
    public Object afterInvoke() {
        synchronized (lock) {
            counter--;
        }
        if (counterWaiting.get() > 0) {
            callAgain();
        }
        return null;
    }

    private void callAgain() {
        try {
            if(!waitingList.isEmpty()) {
                if (counter < concurrentLimit) {
                    while(true) {
                        boolean canExcute = false;
                        synchronized (lock) {
                            if(counter < concurrentLimit) {
                                counter++;
                                canExcute = true;
                            }
                        }
                        if(canExcute) {
                            InternalRequestServer internalRequestServer = waitingList.poll();
                            if (internalRequestServer != null) {
                                counterWaiting.decrementAndGet();
                                MethodRequest request = (MethodRequest) internalRequestServer.rpcRequest;
                                String callbackId = request.getCallbackFutureId();
                                if (callbackId != null) {
                                    retryMap.put(callbackId, internalRequestServer);
                                    internalRequestServer.rmiServer.callAsync(request.getData(), request.getType(), request.getEncode(), callbackId);
                                } else {
                                    LoggerEx.fatal(TAG, "Not expected callbackId is null, internalRequestServer throw away, " + JSON.toJSONString(request) + ", classMethod: " + classMethod);
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            LoggerEx.error(TAG, "AfterInvoke error, classMethod : " + classMethod + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
        }
    }

    private class InternalRequestServer {
        private RPCRequest rpcRequest;
        private RMIServer rmiServer;

        private InternalRequestServer(RPCRequest rpcRequest, RMIServer rmiServer) {
            this.rpcRequest = rpcRequest;
            this.rmiServer = rmiServer;
        }

    }
}
