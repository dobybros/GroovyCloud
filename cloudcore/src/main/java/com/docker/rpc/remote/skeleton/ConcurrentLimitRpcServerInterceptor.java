package com.docker.rpc.remote.skeleton;

import chat.logs.LoggerEx;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.impl.RMIServer;
import com.docker.rpc.remote.RpcServerInterceptor;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLimitRpcServerInterceptor implements RpcServerInterceptor {
    private final String TAG = ConcurrentLimitRpcServerInterceptor.class.getSimpleName();
    private Integer concurrentLimit;
    private CopyOnWriteArrayList<InternalRequestServer> waitingList = new CopyOnWriteArrayList<>();
    private AtomicInteger counter = new AtomicInteger(0);

    public ConcurrentLimitRpcServerInterceptor(Integer concurrentLimit) {
        this.concurrentLimit = concurrentLimit;
    }

    @Override
    public Object invoke(RPCRequest rpcRequest, RMIServer rmiServer) {
        if (counter.get() >= concurrentLimit) {
            InternalRequestServer internalRequestServer = new InternalRequestServer(rpcRequest, rmiServer);
            waitingList.add(internalRequestServer);
            LoggerEx.info(TAG, "Current limiting");
            return false;
        }
        counter.incrementAndGet();
        return true;
    }

    @Override
    public Object afterInvoke() {
        counter.decrementAndGet();
        if (counter.get() < concurrentLimit && !waitingList.isEmpty()) {
            counter.incrementAndGet();
            try {
                InternalRequestServer internalRequestServer = waitingList.remove(0);
                MethodRequest request = (MethodRequest) internalRequestServer.rpcRequest;
                internalRequestServer.rmiServer.callAsync(request.getData(), request.getType(), request.getEncode(), request.getCallbackFutureId());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LoggerEx.error(TAG, "afterInvoke error, errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }

        return null;
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
