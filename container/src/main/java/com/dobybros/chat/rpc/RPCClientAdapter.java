package com.dobybros.chat.rpc;

import chat.errors.CoreException;
import chat.utils.ConcurrentHashSet;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;

public abstract class RPCClientAdapter {
	protected ConcurrentHashSet<ClientAdapterStatusListener> statusListeners = new ConcurrentHashSet<>();
	
	public static abstract class ClientAdapterStatusListener {

		public void started(String serverName) {
		}

		public void connected(String serverName) {
		}

		public void disconnected(String serverName) {
		}

		public void terminated(String serverName) {
		}
		
		public void called(String serverName, RPCRequest request,
				RPCResponse response) {
		}

		public void callFailed(String serverName, RPCRequest request,
				CoreException e) throws CoreException {
		}
	}

	public abstract RPCResponse call(RPCRequest request) throws CoreException;

	public abstract void touch();

	public abstract void clientStart();

	public abstract void clientDestroy();

	public abstract boolean isConnected();
	
//	public CallListener getCallListener() {
//		return callListener;
//	}

//	public ClientAdapterStatusListener getStatusListener() {
//		return statusListener;
//	}

	public void addStatusListener(ClientAdapterStatusListener statusListener) {
		if(statusListener != null && !statusListeners.contains(statusListener)) {
			statusListeners.add(statusListener);
		}
	}

	public abstract Integer getAverageLatency();
}
