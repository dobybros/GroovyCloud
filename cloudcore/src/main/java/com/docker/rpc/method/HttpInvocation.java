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

public class HttpInvocation extends MethodInvocation {
    private MethodRequest methodRequest;
    private MethodMapping methodMapping;
    private RemoteServerHandler remoteServerHandler;
    private Boolean isAsync;
    public HttpInvocation(MethodRequest methodRequest, MethodMapping methodMapping, List<MethodInterceptor> methodInterceptors, RemoteServerHandler remoteServerHandler, String methodKey) {
        super(null, methodMapping.getMethod().getDeclaringClass(), methodMapping.getMethod().getName(), methodRequest.getArgs(), methodInterceptors, methodKey);
        this.methodRequest = methodRequest;
        this.methodMapping = methodMapping;
        this.isAsync = methodMapping.getAsync();
        this.remoteServerHandler = remoteServerHandler;
    }

    @Override
    public Object invoke() throws CoreException {
        if(this.isAsync){
            //not support Cross-cluster access by async
        }else {
            return this.handleSync();
        }
        return null;
    }
    public Object handleSync() throws CoreException {
        MethodResponse response = remoteServerHandler.callHttp(methodRequest);
        return Proxy.getReturnObject(methodRequest, response);
    }

    public MethodRequest getMethodRequest() {
        return methodRequest;
    }

    public void setMethodRequest(MethodRequest methodRequest) {
        this.methodRequest = methodRequest;
    }

    public MethodMapping getMethodMapping() {
        return methodMapping;
    }

    public void setMethodMapping(MethodMapping methodMapping) {
        this.methodMapping = methodMapping;
    }

    public RemoteServerHandler getRemoteServerHandler() {
        return remoteServerHandler;
    }

    public void setRemoteServerHandler(RemoteServerHandler remoteServerHandler) {
        this.remoteServerHandler = remoteServerHandler;
    }

    public Boolean getAsync() {
        return isAsync;
    }

    public void setAsync(Boolean async) {
        isAsync = async;
    }
}
