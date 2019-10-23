package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.remote.MethodMapping;

import java.util.concurrent.CompletableFuture;

interface RemoteInvocationHandler {

    public Object invoke(MethodMapping methodMapping, MethodRequest methodRequest) throws CoreException;
}