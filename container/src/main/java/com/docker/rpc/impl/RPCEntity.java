package com.docker.rpc.impl;

import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;

public class RPCEntity {
    public Class<? extends RPCRequest> requestClass;
    public Class<? extends RPCResponse> responseClass;
}