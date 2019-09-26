package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.rpc.remote.MethodMapping;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.List;
import java.util.Map;

public class RPCInvocationHandlerImpl implements RPCInvocationHandler {
    private RemoteServerHandler remoteServerHandler;

    protected RPCInvocationHandlerImpl(RemoteServerHandler remoteServerHandler) {
        this.remoteServerHandler = remoteServerHandler;
    }

    @Override
    public Object invoke(MethodMapping methodMapping, MethodRequest methodRequest) throws CoreException {
        String methodKey = String.valueOf(methodRequest.getCrc());
        List<MethodInterceptor> methodInterceptors = null;
        RPCInterceptorFactory rpcInterceptorFactory = RPCInterceptorFactory.getInstance();
        Map<String, List<MethodInterceptor>> methodInterceptorMap = rpcInterceptorFactory.getAllMethodInterceptorMap().get(methodRequest.getFromService());
        if (methodInterceptorMap != null) {
            methodInterceptors = methodInterceptorMap.get(methodKey);
        }
        MethodInvocation methodInvocation = new RPCMethodInvocation(methodRequest, methodMapping, methodInterceptors, remoteServerHandler, methodKey);
        return methodInvocation.proceed();
    }
}
