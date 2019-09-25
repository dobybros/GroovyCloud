package com.docker.rpc.method;

import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.Proxy;
import com.docker.rpc.remote.stub.RemoteServerHandler;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RPCMethodInvocation extends MethodInvocation {
    private MethodRequest methodRequest;
    private MethodMapping methodMapping;
    private RemoteServerHandler remoteServerHandler;
    private Boolean isAsync;

    public RPCMethodInvocation(MethodRequest methodRequest, MethodMapping methodMapping, List<MethodInterceptor> methodInterceptors, RemoteServerHandler remoteServerHandler, String methodKey) {
        super(null, methodMapping.getMethod().getDeclaringClass(), methodMapping.getMethod().getName(), methodRequest.getArgs(), methodInterceptors, methodKey);
        this.methodRequest = methodRequest;
        this.methodMapping = methodMapping;
        this.isAsync = methodMapping.getAsync();
        this.remoteServerHandler = remoteServerHandler;
    }

    @Override
    public Object invoke() throws CoreException {
        if (this.isAsync) {
            return this.handleAsync();
        } else {
            return this.handleSync();
        }
    }


    public Object handleSync() throws CoreException {
        MethodResponse response = remoteServerHandler.call(methodRequest);
        return Proxy.getReturnObject(methodRequest, response);
    }

    public CompletableFuture<?> handleAsync() {
        CompletableFuture completableFuture = null;
        try {
            completableFuture = remoteServerHandler.callAsync(methodRequest);
        } catch (Throwable t) {
            completableFuture = new CompletableFuture();
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }

    public MethodRequest getMethodRequest() {
        return methodRequest;
    }

    public MethodMapping getMethodMapping() {
        return methodMapping;
    }

    public RemoteServerHandler getRemoteServerHandler() {
        return remoteServerHandler;
    }

    public int getCurrentInterceptorIndex() {
        return currentInterceptorIndex;
    }

    public Boolean getAsync() {
        return isAsync;
    }
}
