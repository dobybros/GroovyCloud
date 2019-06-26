package com.docker.rpc;

import chat.errors.CoreException;

public interface RPCServerAdapter<Request extends RPCRequest, Response extends RPCResponse> {
	Response onCall(Request request) throws CoreException;
}
