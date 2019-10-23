package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import com.docker.data.Lan;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.method.HttpInvocation;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.rpc.remote.MethodMapping;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.List;
import java.util.Map;

public class RemoteInvocationHandlerImpl implements RemoteInvocationHandler {
    private RemoteServerHandler remoteServerHandler;

    protected RemoteInvocationHandlerImpl(RemoteServerHandler remoteServerHandler) {
        this.remoteServerHandler = remoteServerHandler;
    }

    @Override
    public Object invoke(MethodMapping methodMapping, MethodRequest methodRequest) throws CoreException {
        String methodKey = String.valueOf(methodRequest.getCrc());
        List<MethodInterceptor> methodInterceptors = null;
        if (methodRequest.getFromService() != null) {
            Map<String, List<MethodInterceptor>> methodInterceptorMap = RPCInterceptorFactory.getInstance().getAllMethodInterceptorMap().get(methodRequest.getFromService());
            if (methodInterceptorMap != null) {
                methodInterceptors = methodInterceptorMap.get(methodKey);
            }
        }
        handleAsyncWithHandler(methodMapping.getAsync(), methodRequest);
        MethodInvocation methodInvocation = null;
        if(methodRequest.getServiceStubManager() != null){
            if(methodRequest.getServiceStubManager().getLanType() == null || methodRequest.getServiceStubManager().getLanType().equals(Lan.TYPE_RPC)){
                methodInvocation = new RPCMethodInvocation(methodRequest, methodMapping, methodInterceptors, remoteServerHandler, methodKey);
            }else {
                methodInvocation = new HttpInvocation(methodRequest, methodMapping, methodInterceptors, remoteServerHandler, methodKey);
            }
            return methodInvocation.proceed();
        }
        return null;
    }

    private void handleAsyncWithHandler(Boolean isAsync, MethodRequest methodRequest) {
        if (isAsync) {
            AsyncRpcFuture asyncRpcFuture = new AsyncRpcFuture(methodRequest.getCrc());
            remoteServerHandler.setCallbackFutureId(asyncRpcFuture.getCallbackFutureId());
            RpcCacheManager.getInstance().pushToAsyncRpcMap(asyncRpcFuture.getCallbackFutureId(), asyncRpcFuture);
        }
    }
}
