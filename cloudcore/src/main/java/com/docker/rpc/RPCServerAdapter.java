package com.docker.rpc;

import chat.errors.CoreException;

import java.util.concurrent.CompletableFuture;

public interface RPCServerAdapter<Request extends RPCRequest, Response extends RPCResponse> {
	Response onCall(Request request) throws CoreException;
	Object oncallAsync(Request request, String callbackFutureId) throws CoreException;
}
