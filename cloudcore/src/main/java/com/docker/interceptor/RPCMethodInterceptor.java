package com.docker.interceptor;

import chat.errors.CoreException;
import com.docker.rpc.method.RPCMethodInvocation;

public interface RPCMethodInterceptor {
    public Object invoke(RPCMethodInvocation rpcMethodInvocation) throws CoreException;
}
