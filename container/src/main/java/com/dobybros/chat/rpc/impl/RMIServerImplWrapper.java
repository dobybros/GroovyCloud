package com.dobybros.chat.rpc.impl;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.rpc.RPCServerAdapter;
import com.dobybros.chat.rpc.annotations.RPCServerHandler;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;
import org.apache.commons.lang3.StringUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RMIServerImplWrapper extends ClassAnnotationHandler {
//public class RMIServerImpl implements RMIServer, ClassAnnotationHandler {
	RMIHandler rmiHandler;
	GroovyRuntime groovyRuntime;
	//Server
	Map<String, GroovyObjectEx<RPCServerAdapter>> serverAdapterMap;

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

	public RPCResponse onCall(RPCRequest request) throws CoreException {
		String type = request.getType();
		if(type == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOMAPPING, "No server adapter found by type " + type);

		GroovyObjectEx<RPCServerAdapter> adapter = serverAdapterMap.get(type);
		if(adapter == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

		RPCServerAdapter serverAdapter = null;
		serverAdapter = adapter.getObject();
		RPCResponse response = serverAdapter.onCall(request);
		return response;
	}

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, CoreException {
		RMIHandler clientHandler = new RMIHandler();
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
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		this.groovyRuntime = groovyRuntime;
		return RPCServerHandler.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder(
					"\r\n---------------------------------------\r\n");

			Map<String, GroovyObjectEx<RPCServerAdapter>> newServerAdapterMap = new ConcurrentHashMap<>();
			Set<String> keys = annotatedClassMap.keySet();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);

				// Class<GroovyServlet> groovyClass =
				// groovyServlet.getGroovyClass();
				if (groovyClass != null) {
					// Handle RequestIntercepting
					RPCServerHandler requestIntercepting = groovyClass.getAnnotation(RPCServerHandler.class);
					if (requestIntercepting != null) {
						String rpcType = requestIntercepting.rpcType();
						if (!StringUtils.isBlank(rpcType)) {
							GroovyObjectEx<RPCServerAdapter> serverAdapter = groovyRuntime
									.create(groovyClass);
							if (serverAdapter != null) {
								uriLogs.append("RPCServerHandler " + rpcType + "#" + groovyClass + "\r\n");
								newServerAdapterMap.put(rpcType, serverAdapter);
							}
						}
					}
				}
			}
			this.serverAdapterMap = newServerAdapterMap;
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}

	public GroovyRuntime getGroovyRuntime() {
		return groovyRuntime;
	}

	public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
		this.groovyRuntime = groovyRuntime;
	}

	public RMIHandler getRmiHandler() {
		return rmiHandler;
	}

	public void setRmiHandler(RMIHandler rmiHandler) {
		this.rmiHandler = rmiHandler;
	}
}

