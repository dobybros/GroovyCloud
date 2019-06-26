package com.dobybros.chat.tasks;

import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.impl.ExpireListener;
import com.docker.rpc.impl.RMIClientHandler;
import com.docker.tasks.Task;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This task is to hold each RPCClientAdapter in map which connecting to other servers. 
 * If a RPCClientAdapter is idle too long time, the RPCClientAdapter will be disconnected and removed from the map.
 * Centralize the control to RPCClientAdapters for easily been added/removed.  
 * 
 * @author aplomb
 *
 */
public class RPCClientAdapterMapTask extends Task {
	private static final String TAG = RPCClientAdapterMapTask.class.getSimpleName();
	
	private Map<String, RPCClientAdapter> clientAdapterMap = new ConcurrentHashMap<String, RPCClientAdapter>();
	private long expireTime = TimeUnit.MINUTES.toMillis(5);

	private boolean enableSsl = false;
	/** rpc ssl certificate */
	private String rpcSslClientTrustJksPath;
	private String rpcSslServerJksPath;
	private String rpcSslJksPwd;

	public String toString() {
		return super.toString();
	}
	
	public RPCClientAdapterMapTask() {
	}

	@Override
	public String taskDescription() {
		StringBuilder builder = new StringBuilder(RPCClientAdapterMapTask.class.getSimpleName() + ": ");
		builder.append("enableSsl: " + enableSsl + " ");
		builder.append("expireTime: " + expireTime + " ");
		builder.append("clients: " + Arrays.toString(clientAdapterMap.values().toArray()) + " ");
		return builder.toString();
	}

	public synchronized RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName) {
		return registerServer(ip, rmiPort, serverName, expireTime);
	}

	public synchronized RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName, Long expireTime) {
		return registerServer(ip, rmiPort, serverName, null, expireTime);
	}

	public RPCClientAdapter getClientAdapter(String serverName) {
		return clientAdapterMap.get(serverName);
	}

	public synchronized RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName, RPCClientAdapter.ClientAdapterStatusListener statusListener) {
		return registerServer(ip, rmiPort, serverName, statusListener, expireTime);
	}
	public synchronized RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName, RPCClientAdapter.ClientAdapterStatusListener statusListener, Long expireTime) {
		if(serverName == null)
			return null;
		RPCClientAdapter clientAdapter = clientAdapterMap.get(serverName);
		if (clientAdapter != null) {
			RMIClientHandler rmiHandler = (RMIClientHandler) clientAdapter;
			String serverHost = rmiHandler.getServerHost();
			Integer theRmiPort = rmiHandler.getRmiPort();
			if ((serverHost != null && !serverHost.equals(ip)) || (theRmiPort != null && !theRmiPort.equals(rmiPort))) {
				unregisterServer(serverName);
				clientAdapter = null;
			}
		}
		if(clientAdapter == null) {
			if(ip == null) 
				return null;
			RMIClientHandler rmiClient = new RMIClientHandler();
			rmiClient.setRmiPort(rmiPort);
			rmiClient.setServerHost(ip);
//			rmiClient.setServerName(serverName);
			rmiClient.setEnableSsl(enableSsl);
			if (enableSsl) {
				rmiClient.setRpcSslClientTrustJksPath(rpcSslClientTrustJksPath);
				rmiClient.setRpcSslServerJksPath(rpcSslServerJksPath);
				rmiClient.setRpcSslJksPwd(rpcSslJksPwd);
			}
//			rmiClient.setServerAdapterMap(serverAdapterMap);
			rmiClient.setExpireTime(expireTime, new ExpireListener<RPCClientAdapter>() {
				@Override
				public boolean expired(RPCClientAdapter handler, long touch, long expireTime) {
					RPCClientAdapter removedHandler = RPCClientAdapterMapTask.this.unregisterServer(serverName);
					if(removedHandler == null) {
						handler.clientDestroy();
					}
					return true;
				}
			});
			clientAdapter = rmiClient;
			
			clientAdapter.clientStart();
			clientAdapterMap.put(serverName, clientAdapter);
		}
		clientAdapter.addStatusListener(statusListener);
		return clientAdapter;
	}
	
	public synchronized RPCClientAdapter unregisterServer(String serverName) {
		RPCClientAdapter handler = clientAdapterMap.remove(serverName);
		if(handler != null) {
			handler.clientDestroy();
		}
		return handler;
	}

	public synchronized void unregisterAllServers() {
		if (clientAdapterMap != null && !clientAdapterMap.isEmpty()) {
			for (String serverName : clientAdapterMap.keySet()) {
				RPCClientAdapter handler = clientAdapterMap.remove(serverName);
				if(handler != null) {
					handler.clientDestroy();
				}
			}
		}
	}
	
	@Override
	public void execute() {
	}
	
	@Override
	public void init() throws Throwable {
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public Map<String, RPCClientAdapter> getClientAdapterMap() {
		return clientAdapterMap;
	}

	public void setClientAdapterMap(Map<String, RPCClientAdapter> clientAdapterMap) {
		this.clientAdapterMap = clientAdapterMap;
	}

	@Override
	public void shutdown() {
		if(clientAdapterMap != null) {
			//It will block shut down when there are connection issues.
//			Set<Entry<String, RPCClientAdapter>> entries = clientAdapterMap.entrySet();
//			for(Entry<String, RPCClientAdapter> entry : entries) {
//				entry.getValue().clientDestroy();
//			}
			clientAdapterMap.clear();
		}
	}

	public boolean isEnableSsl() {
		return enableSsl;
	}

	public void setEnableSsl(boolean enableSsl) {
		this.enableSsl = enableSsl;
	}

	public String getRpcSslClientTrustJksPath() {
		return rpcSslClientTrustJksPath;
	}

	public void setRpcSslClientTrustJksPath(String rpcSslClientTrustJksPath) {
		this.rpcSslClientTrustJksPath = rpcSslClientTrustJksPath;
	}

	public String getRpcSslServerJksPath() {
		return rpcSslServerJksPath;
	}

	public void setRpcSslServerJksPath(String rpcSslServerJksPath) {
		this.rpcSslServerJksPath = rpcSslServerJksPath;
	}

	public String getRpcSslJksPwd() {
		return rpcSslJksPwd;
	}

	public void setRpcSslJksPwd(String rpcSslJksPwd) {
		this.rpcSslJksPwd = rpcSslJksPwd;
	}
}
