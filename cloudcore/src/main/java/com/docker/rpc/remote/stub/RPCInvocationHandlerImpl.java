package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.rpc.remote.MethodMapping;
import org.springframework.beans.factory.annotation.Autowired;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.List;
import java.util.Map;

public class RPCInvocationHandlerImpl implements RPCInvocationHandler {
    @Autowired
    RpcCacheManager rpcCacheManager;
    @Autowired
    RPCInterceptorFactory rpcInterceptorFactory;

    private RemoteServerHandler remoteServerHandler;
    protected RPCInvocationHandlerImpl(RemoteServerHandler remoteServerHandler) {
        this.remoteServerHandler = remoteServerHandler;
    }

    @Override
    public Object invoke(MethodMapping methodMapping, MethodRequest methodRequest) throws CoreException {
        String methodKey = String.valueOf(methodRequest.getCrc());
        List<MethodInterceptor> methodInterceptors = null;
        if(methodRequest.getFromService() != null){
            Map<String, List<MethodInterceptor>> methodInterceptorMap = rpcInterceptorFactory.getAllMethodInterceptorMap().get(methodRequest.getFromService());
            if (methodInterceptorMap != null) {
                methodInterceptors = methodInterceptorMap.get(methodKey);
            }
        }
        handleAsyncWithHandler(rpcCacheManager, methodMapping.getAsync(), methodRequest);
        MethodInvocation methodInvocation = new RPCMethodInvocation(methodRequest, methodMapping, methodInterceptors, remoteServerHandler, methodKey);
        return methodInvocation.proceed();
    }
    private void handleAsyncWithHandler(RpcCacheManager rpcCacheManager, Boolean isAsync, MethodRequest methodRequest){
        if(isAsync){
            if(rpcCacheManager != null){
                AsyncRpcFuture asyncRpcFuture = new AsyncRpcFuture(methodRequest.getCrc());
                remoteServerHandler.setCallbackFutureId(asyncRpcFuture.getCallbackFutureId());
                rpcCacheManager.pushToAsyncRpcMap(asyncRpcFuture.getCallbackFutureId(), asyncRpcFuture);
            }
        }
    }
}
