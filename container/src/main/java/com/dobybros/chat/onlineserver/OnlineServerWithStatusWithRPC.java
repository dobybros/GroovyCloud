package com.dobybros.chat.onlineserver;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.rpc.RPCClientAdapter;
import com.dobybros.chat.tasks.RPCClientAdapterMapTask;

import javax.annotation.Resource;

public class OnlineServerWithStatusWithRPC extends OnlineServerWithStatus {
	
	private String balancerRpcIp;
	private Integer balancerRpcPort;
	private String balancerServer;
	
	//rpc server parameters
	
	@Resource
    RPCClientAdapterMapTask rpcClientAdapterMapTask;
	
	OnlineServerWithStatusWithRPC() {
		super();
	}
	
	public void prepareBalancerHandler(String rpcIp, Integer rpcPort, String server) throws CoreException {
		if(rpcIp == null || rpcPort == null) 
			throw new CoreException(CoreErrorCodes.ERROR_ILLEGAL_PARAMETER, "Balancer rmp ip or port is null, " + rpcIp + " | " + rpcPort);
		if(balancerRpcIp == null || balancerRpcPort == null || !rpcIp.equals(balancerRpcIp) || !rpcPort.equals(balancerRpcPort)) {
//			if(balancerHandler != null) {
//				balancerHandler.clientDestroy();
//				balancerHandler = null;
//			}
			
			balancerRpcIp = rpcIp;
			balancerRpcPort = rpcPort;
			balancerServer = server;
			
//			RMIHandler balancerHandler = new RMIHandler();
//			balancerHandler.setRmiPort(balancerRpcPort);
//			balancerHandler.setServerHost(balancerRpcIp);
//			balancerHandler.setTypeClassMap(balancerTypeClassMap);
//			balancerHandler.setServerName(server);
//			balancerHandler.clientStart();
//			this.balancerHandler = balancerHandler;
			RPCClientAdapter clientAdapter = rpcClientAdapterMapTask.getClientAdapter(balancerServer);
			if(clientAdapter != null) {
				rpcClientAdapterMapTask.unregisterServer(balancerServer);
			}
			rpcClientAdapterMapTask.registerServer(balancerRpcIp, balancerRpcPort, balancerServer);
		}
	}
	
	public String getBalancerRpcIp() {
		return balancerRpcIp;
	}
	public void setBalancerRpcIp(String balancerRpcIp) {
		this.balancerRpcIp = balancerRpcIp;
	}
	public Integer getBalancerRpcPort() {
		return balancerRpcPort;
	}
	public void setBalancerRpcPort(Integer balancerRpcPort) {
		this.balancerRpcPort = balancerRpcPort;
	}
	public RPCClientAdapter getBalancerHandler() {
		return rpcClientAdapterMapTask.registerServer(balancerRpcIp, balancerRpcPort, balancerServer);
	}

	public String getBalancerServer() {
		return balancerServer;
	}

	public void setBalancerServer(String balancerServer) {
		this.balancerServer = balancerServer;
	}
}
