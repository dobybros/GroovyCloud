package com.docker.rpc.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.docker.errors.CoreErrorCodes;
import com.docker.rpc.*;
import script.groovy.object.GroovyObjectEx;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerImpl extends UnicastRemoteObject implements RMIServer {
    private RMIServerImplWrapper serverWrapper;
	public RMIServerImpl(Integer port, RMIServerImplWrapper serverWrapper) throws RemoteException {
		super(port);
        this.serverWrapper = serverWrapper;
	}
	public RMIServerImpl(Integer port, RMIServerImplWrapper serverWrapper, boolean enableSsl) throws RemoteException {
        super(port, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
        this.serverWrapper = serverWrapper;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4853473944368414096L;
	private static final String TAG = RMIServerImpl.class.getSimpleName();

	public RPCResponse onCall(RPCRequest request) throws CoreException {
		String type = request.getType();
		if(type == null)
			throw new CoreException(ChatErrorCodes.ERROR_RPC_TYPE_NOMAPPING, "No server adapter found by type " + type);

		GroovyObjectEx<RPCServerAdapter> adapter = serverWrapper.serverAdapterMap.get(type);
		if(adapter == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

		RPCServerAdapter serverAdapter = null;
		serverAdapter = adapter.getObject();
		RPCResponse response = serverAdapter.onCall(request);
		return response;
	}

	@Override
	public byte[] call(byte[] data, String type, Byte encode)
			throws RemoteException {
		if(serverWrapper.rmiServerHandler == null)
			throw new RemoteException("RPC handler is null");
			
		try {
			RPCRequest request = null;
			RPCServerAdapter serverAdapter = null;
			RPCEntity entity = null;
			if(MethodRequest.RPCTYPE.equals(type)) {
				if(serverWrapper.serverMethodInvocation == null)
					serverWrapper.serverMethodInvocation = new RPCServerMethodInvocation();
				request = new MethodRequest();

				request.setEncode(encode);
				request.setType(type);
				request.setData(data);
				request.resurrect();
				RPCResponse response = serverWrapper.serverMethodInvocation.onCall((MethodRequest) request);
				if(response != null) {
					byte[] responseData = response.getData();
					if(responseData == null) {
						if(response.getEncode() == null)
							response.setEncode(RPCBase.ENCODE_PB);
						response.persistent();
					}

					return response.getData();
				}
				return null;
			} else {
				GroovyObjectEx<RPCServerAdapter> adapter = serverWrapper.serverAdapterMap.get(type);
				if(adapter == null)
					throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

				entity = serverWrapper.rmiServerHandler.getRPCEntityForServer(type, adapter.getGroovyClass());
				serverAdapter = adapter.getObject();
				request = entity.requestClass.newInstance();

				request.setEncode(encode);
				request.setType(type);
				request.setData(data);
				request.resurrect();
				RPCResponse response = serverAdapter.onCall(request);
				if(response != null) {
					byte[] responseData = response.getData();
					if(responseData == null) {
						if(response.getEncode() == null)
							response.setEncode(RPCBase.ENCODE_PB);
						response.persistent();
					}

					return response.getData();
				}
			}
			return null;
		} catch (Throwable t) {
			String message = null;
			if(t instanceof CoreException) {
				message = ((CoreException)t).getCode() + "|" + t.getMessage();
			} else {
				message = t.getMessage();
			}
			throw new RemoteException(message, t);
		}
	}

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, CoreException {
		RMIClientHandler clientHandler = new RMIClientHandler();
		clientHandler.setServerHost("localhost");
		clientHandler.clientStart();
		
		RPCRequest request = new RPCRequest(null) {
			@Override
			public void resurrect() {
				
			}
			
			@Override
			public void persistent() {
				
			}
		};
		request.setData("hello".getBytes());
		request.setEncode((byte) 1);
		request.setType("afb");
		RPCResponse response = clientHandler.call(request);
		System.out.println(new String(response.getData()));
	}

	@Override
	public boolean alive() throws RemoteException {
		return true;
	}
}

