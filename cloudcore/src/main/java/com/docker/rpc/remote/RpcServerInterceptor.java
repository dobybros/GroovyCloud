package com.docker.rpc.remote;

import com.docker.rpc.RPCRequest;
import com.docker.rpc.impl.RMIServer;

public interface RpcServerInterceptor {
    public Object invoke(RPCRequest rpcRequest, RMIServer rmiServer);

    public Object afterInvoke();
}
