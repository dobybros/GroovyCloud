package com.docker.rpc.impl;

import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;

public class RPCEntity {
    Class<? extends RPCRequest> requestClass;
    Class<? extends RPCResponse> responseClass;
}