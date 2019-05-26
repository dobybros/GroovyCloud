package com.docker.rpc.impl;

import com.docker.rpc.RPCServerMethodInvocation;

import java.rmi.RemoteException;

public class RMIServerImplWrapper {
	RMIServerHandler rmiServerHandler;
	//Server
	RPCServerMethodInvocation serverMethodInvocation = new RPCServerMethodInvocation();

	Integer port;

	RMIServer server;

	public RMIServer initServer(boolean enableSsl) {
		if(server == null) {
			try {
				if (enableSsl) {
					server = new RMIServerImpl(port + 1, this, enableSsl);
				} else {
					server = new RMIServerImpl(port + 1, this);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return server;
	}

	public void initClient(RMIServer server) {
		this.server = server;
	}

	public RMIServer getServer() {
		return server;
	}

	public RMIServerImplWrapper(Integer port) throws RemoteException {
//		super(port + 1);
		this.port = port;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4853473944368414096L;
	private static final String TAG = RMIServerImplWrapper.class.getSimpleName();

	public RMIServerHandler getRmiServerHandler() {
		return rmiServerHandler;
	}

	public void setRmiServerHandler(RMIServerHandler rmiServerHandler) {
		this.rmiServerHandler = rmiServerHandler;
	}
}

