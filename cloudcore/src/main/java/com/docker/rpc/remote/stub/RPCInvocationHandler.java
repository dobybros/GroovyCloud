package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.remote.MethodMapping;

import java.util.concurrent.CompletableFuture;

interface RPCInvocationHandler {
    /*
MethodMapping methodMapping = serviceStubManager.getMethodMapping(crc);
        if(methodMapping.getAsync()){
            CompletableFuture completableFuture = null;
            try {
                completableFuture = remoteServerHandler.callAsync(request);
            }catch (Throwable t){
                completableFuture = new CompletableFuture();
                completableFuture.completeExceptionally(t);
            }
            return completableFuture;
        }else {

            MethodResponse response = remoteServerHandler.call(request);
            return Proxy.getReturnObject(request, response);

        }
     */

    public Object invoke(MethodMapping methodMapping, MethodRequest methodRequest) throws CoreException;
}