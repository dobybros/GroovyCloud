package com.dobybros.chat.rpc;

import chat.errors.CoreException;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;

public interface RPCServerAdapter<Request extends RPCRequest, Response extends RPCResponse> {
	Response onCall(Request request) throws CoreException;
}
