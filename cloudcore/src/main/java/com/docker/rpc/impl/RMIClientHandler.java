package com.docker.rpc.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
//import chat.utils.AverageCounter;
import chat.utils.IPHolder;
import com.docker.storage.cache.CacheMethodInterceptor;
import com.docker.rpc.*;
import com.docker.rpc.async.AsyncCallbackRequest;
import com.docker.storage.cache.CacheMethodInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import script.memodb.ObjectId;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RMIClientHandler extends RPCClientAdapter {
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    public static final String RMIID_SSL_SUFFIX = "_SSL";
    // public static final String RMI_ID = "RMIINTERFACE";
    public static final int RMI_PORT = 2222;

    private String rmiId;
    private Integer rmiPort = RMI_PORT;
    //both
    private Registry registry;
    private RMIServer server;

    private boolean enableSsl = false;

    private ConcurrentHashMap<String, RPCEntity> typeEntityMap = new ConcurrentHashMap<>();

    //Client
    private String serverHost;
//    private AverageCounter averageCounter;

    private Long touch;
    private Long idleCheckPeriod = TimeUnit.SECONDS.toMillis(10);
    private static final String TAG = "RMI";
    private Long expireTime;
    private ExpireListener<RPCClientAdapter> expireListener;

    private boolean isStarted = true;

    /**
     * rpc ssl certificate
     */
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(RMIClientHandler.class.getSimpleName() + ": ");
        builder.append("rmiId: " + rmiId + " ");
        builder.append("rmiPort: " + rmiPort + " ");
        builder.append("server: " + server + " ");
        builder.append("isStarted: " + isStarted + " ");
//        builder.append("averageCounter: " + averageCounter + " ");
        return builder.toString();
    }

    public void setExpireTime(Long expireTime, ExpireListener<RPCClientAdapter> expireListener) {
        this.expireTime = expireTime;
        this.expireListener = expireListener;
    }

    private ClientMonitorThread clientMonitorThread;

    class ClientMonitorThread extends Thread {
        private AtomicBoolean connected;

        public ClientMonitorThread(boolean value) {
            connected = new AtomicBoolean(value);
            if (connected.get()) {
                for (ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.connected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " connected failed, " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            } else {
                for (ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.disconnected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        }

        public void run() {
            LoggerEx.info(TAG, "Start monitoring RMI client connection");
            while (isStarted) {
                if (expireTime != null && expireListener != null) {
                    if (touch + expireTime < System.currentTimeMillis()) {
                        try {
                            if (expireListener.expired(RMIClientHandler.this, touch, expireTime)) {
                                break;
                            }
                        } catch (Throwable t) {
                            LoggerEx.info(TAG, "Handle server expire failed, " + ExceptionUtils.getFullStackTrace(t) + " the expireListener " + expireListener + " will be ignored...");
                        }
                    }
                }
                synchronized (connected) {
                    if (connected.get()) {
                        if (touch + idleCheckPeriod < System.currentTimeMillis()) {
                            try {
                                server.alive();
                            } catch (Throwable ce) {
                                LoggerEx.info(TAG, "Check server alive failed, " + ExceptionUtils.getFullStackTrace(ce) + " need reconnect..." + ", server : " + server.toString() + ", serverHost : " + serverHost);
                                connected.compareAndSet(true, false);
                                for (ClientAdapterStatusListener statusListener : statusListeners) {
                                    try {
                                        statusListener.disconnected(rmiId);
                                    } catch (Throwable t) {
                                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + ExceptionUtils.getFullStackTrace(t));
                                    }
                                }
                                continue;
                            }
                        }
                        try {
                            connected.wait(idleCheckPeriod);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (connected.get())
                            continue;
                    }
                }
                if (isStarted) {
                    LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " start connecting...");
                    try {
                        Registry registry = LocateRegistry.getRegistry(serverHost, rmiPort);
                        server = (RMIServer) registry.lookup(rmiId);
//                        serverImpl.initClient(server);
                        connected.set(true);
                        for (ClientAdapterStatusListener statusListener : statusListeners) {
                            try {
                                statusListener.connected(rmiId);
                            } catch (Throwable t) {
                                LoggerEx.error(TAG, "statusListener " + statusListener + " connected failed, " + ExceptionUtils.getFullStackTrace(t));
                            }
                        }
                        LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " connected!");
                    } catch (Throwable t) {
                        t.printStackTrace();
                        if (t instanceof NotBoundException) {
                            LoggerEx.error(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " not bound any more, " + ExceptionUtils.getFullStackTrace(t));
                        } else {
                            LoggerEx.error(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " connect failed, " + ExceptionUtils.getFullStackTrace(t));
                        }
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //Will unexportObject in client mode.
            if (server != null) {
                try {
                    boolean bool = UnicastRemoteObject.unexportObject(server, true);
                    LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " is destroyed, unexport " + server + " result " + bool);
                } catch (Throwable e) {
                }
            }
            LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " monitor stopped...");
            for (ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.terminated(rmiId);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "statusListener " + statusListener + " terminated failed, " + ExceptionUtils.getFullStackTrace(t));
                }
            }
            statusListeners.clear();
        }
    }

    public void touch() {
        touch = System.currentTimeMillis();
    }

    @Override
    public synchronized void clientStart() {
//        averageCounter = new AverageCounter();
        if (enableSsl && !rmiId.endsWith(RMIID_SSL_SUFFIX))
            rmiId = rmiId + RMIID_SSL_SUFFIX;
        for (ClientAdapterStatusListener statusListener : statusListeners) {
            try {
                statusListener.started(rmiId);
            } catch (Throwable t) {
                LoggerEx.error(TAG, "statusListener " + statusListener + " started failed, " + ExceptionUtils.getFullStackTrace(t));
            }
        }
        boolean startConnected = false;
        try {
            if (StringUtils.isBlank(serverHost))
                throw new CoreException(ChatErrorCodes.ERROR_ILLEGAL_PARAMETER, "Server host is illegal, " + serverHost);
            if (enableSsl) {
                setSslProp();
                registry = LocateRegistry.getRegistry(serverHost, rmiPort, new SslRMIClientSocketFactory());
            } else {
                registry = LocateRegistry.getRegistry(serverHost, rmiPort);
            }
            server = (RMIServer) registry.lookup(rmiId);

//            serverImpl.initClient(server);

//       System.out.println(new String(server.call("world".getBytes(), "ab1c", 1)));
            LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " server " + rmiId + " client connected!");
            startConnected = true;
        } catch (Throwable t) {
            t.printStackTrace();
            LoggerEx.error(TAG, "RMI clientStart failed, " + ExceptionUtils.getFullStackTrace(t));
            startConnected = false;
        } finally {
            touch();
            clientMonitorThread = new ClientMonitorThread(startConnected);
            clientMonitorThread.start();
            resetSslProp();
        }
    }

    private void setSslProp() {
//        String pass = "liyazhou";
        System.setProperty("javax.net.ssl.debug", "all");
//        System.setProperty("javax.net.ssl.trustStore", "/Users/liyazhou/workspace/tcpssl/certificate/clientTrust.jks");
        System.setProperty("javax.net.ssl.trustStore", rpcSslClientTrustJksPath);
        System.setProperty("javax.net.ssl.trustStorePassword", rpcSslJksPwd);
//        System.setProperty("javax.net.ssl.keyStore", "/Users/liyazhou/workspace/tcpssl/certificate/server.jks");
        System.setProperty("javax.net.ssl.keyStore", rpcSslServerJksPath);
        System.setProperty("javax.net.ssl.keyStorePassword", rpcSslJksPwd);
    }

    private void resetSslProp() {
        System.getProperties().remove("javax.net.ssl.debug");
        System.getProperties().remove("javax.net.ssl.trustStore");
        System.getProperties().remove("javax.net.ssl.trustStorePassword");
        System.getProperties().remove("javax.net.ssl.keyStore");
        System.getProperties().remove("javax.net.ssl.keyStorePassword");
    }

    @Override
    public synchronized void clientDestroy() {
        if (isStarted) {
            isStarted = false;
            expireListener = null;

            if (clientMonitorThread != null) {
                synchronized (clientMonitorThread.connected) {
                    clientMonitorThread.connected.notify();
                }
            }
        }
    }

    public synchronized void serverDestroy() {
        try {
            registry.unbind(rmiId);
            LoggerEx.info(TAG, "RMI " + serverHost + " port " + rmiPort + " rmiId " + rmiId + " server is destroyed!");
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "RMI " + serverHost + " port " + rmiPort + " rmiId " + rmiId + " server destroy failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public void callAsync(RPCRequest request) throws CoreException {
        beanFactory.autowireBean(request);
        try {
            callPrivateAsync(request);
            for (ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.called(rmiId, request, null);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(called) occured error " + ExceptionUtils.getFullStackTrace(t) + " for request " + request + " and response " + null);
                }
            }
        } catch (CoreException e) {
            for (ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.callFailed(rmiId, request, e);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(callFailed) occured error " + ExceptionUtils.getFullStackTrace(t) + " for request " + request);
                    if (t instanceof CoreException)
                        throw t;
                }
            }
            throw e;
        }
    }

    private void callPrivateAsync(RPCRequest request) throws CoreException {
        touch();
        if (!clientMonitorThread.connected.get())
            throw new CoreException(ChatErrorCodes.ERROR_RPC_DISCONNECTED, "RPC (" + serverHost + ":" + rmiPort + ") is disconnected for " + request.getType() + ": " + request.toString());
        try {

            if (request instanceof MethodRequest) {
                handleRequest(request);
                server.callAsync(request.getData(), request.getType(), request.getEncode(), ((MethodRequest) request).getCallbackFutureId());

            } else if (request instanceof AsyncCallbackRequest) {
                server.callAsync(request.getData(), request.getType(), request.getEncode(), null);
            }
            long time = System.currentTimeMillis();
//            if (averageCounter != null)
//                averageCounter.add((int) (System.currentTimeMillis() - time));
        } catch (ConnectException | ConnectIOException ce) {
            if (clientMonitorThread.connected.compareAndSet(true, false)) {
                for (ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.disconnected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
                synchronized (clientMonitorThread.connected) {
                    clientMonitorThread.connected.notify();
                }
            }
            LoggerEx.error(TAG, "RMI call failed, " + ExceptionUtils.getFullStackTrace(ce) + " start reconnecting...");
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED, "RMI call failed, " + ce.getMessage() + " start reconnecting...");
        } catch (Throwable t) {
            if (t instanceof ServerException) {
                Throwable remoteException = t.getCause();
                if (remoteException instanceof RemoteException) {
                    Throwable coreException = remoteException.getCause();
                    if (coreException instanceof CoreException) {
                        throw (CoreException) coreException;
                    }
                }
            }
            if (t instanceof CoreException)
                throw (CoreException) t;
            t.printStackTrace();
            LoggerEx.error(TAG, "RMI call failed, " + ExceptionUtils.getFullStackTrace(t));
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_FAILED, "RMI call failed, " + t.getMessage());
        }
    }

    @Override
    public RPCResponse call(RPCRequest request) throws CoreException {
        beanFactory.autowireBean(request);
        try {
            RPCResponse response = callPrivate(request);
            for (ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.called(rmiId, request, response);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(called) occured error " + ExceptionUtils.getFullStackTrace(t) + " for request " + request + " and response " + response);
                }
            }
            return response;
        } catch (CoreException e) {
            for (ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.callFailed(rmiId, request, e);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(callFailed) occured error " + ExceptionUtils.getFullStackTrace(t) + " for request " + request);
                    if (t instanceof CoreException)
                        throw t;
                }
            }
            throw e;
        }
    }

    private RPCResponse callPrivate(RPCRequest request) throws CoreException {
        touch();
        if (!clientMonitorThread.connected.get())
            throw new CoreException(ChatErrorCodes.ERROR_RPC_DISCONNECTED, "RPC (" + serverHost + ":" + rmiPort + ") is disconnected for " + request.getType() + ": " + request.toString());
        try {
            handleRequest(request);
//            long time = System.currentTimeMillis();
            byte[] data = server.call(request.getData(), request.getType(), request.getEncode());
//            if (averageCounter != null)
//                averageCounter.add((int) (System.currentTimeMillis() - time));
            if (data == null) {
                LoggerEx.error(TAG, "Server call return null for reuqest " + request.getType());
                return null;
            }
            RPCResponse response = handleResponse(request.getType(), request, data);
            return response;
        } catch (ConnectException | ConnectIOException ce) {
            if (clientMonitorThread.connected.compareAndSet(true, false)) {
                for (ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.disconnected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
                synchronized (clientMonitorThread.connected) {
                    clientMonitorThread.connected.notify();
                }
            }
            LoggerEx.error(TAG, "RMI call failed, " + ExceptionUtils.getFullStackTrace(ce) + " start reconnecting...");
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED, "RMI call failed, " + ce.getMessage() + " start reconnecting...");
        } catch (Throwable t) {
            if (t instanceof ServerException) {
                Throwable remoteException = t.getCause();
                if (remoteException instanceof RemoteException) {
                    Throwable coreException = remoteException.getCause();
                    if (coreException instanceof CoreException) {
                        throw (CoreException) coreException;
                    }
                }
            }
            if (t instanceof CoreException)
                throw (CoreException) t;
            t.printStackTrace();
            LoggerEx.error(TAG, "RMI call failed, " + ExceptionUtils.getFullStackTrace(t));
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_FAILED, "RMI call failed, " + t.getMessage());
        }
    }

    RPCEntity getRPCEntityForClient(String requestType, RPCRequest request) throws CoreException {
        RPCEntity entity = typeEntityMap.get(requestType);
        if (entity == null) {
            String requestClass = request.getClass().getName();
            String responseClassString = null;
            final String REQUEST_SUFIX = "Request";
            final String RESPONSE_SUFIX = "Response";
            if (!requestClass.endsWith(REQUEST_SUFIX))
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " don't contain Request as sufix. ");

            responseClassString = requestClass.substring(0, requestClass.length() - REQUEST_SUFIX.length()) + RESPONSE_SUFIX;
            Class<? extends RPCResponse> responseClass = null;
            try {
                responseClass = (Class<? extends RPCResponse>) Class.forName(responseClassString);
            } catch (ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "RPC type " + requestType + " don't have correct class name " + responseClassString + ". " + ExceptionUtils.getFullStackTrace(e));
                throw new CoreException(ChatErrorCodes.ERROR_RPC_TYPE_REQUEST_NOMAPPING, "RPC type " + requestType + " don't have correct class name " + responseClassString + ". " + e.getMessage());
            }
            if (requestClass != null && responseClass != null) {
                entity = new RPCEntity();
                entity.requestClass = request.getClass();
                entity.responseClass = responseClass;
            }
            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
            if (previousEntity != null)
                entity = previousEntity;
        }
        return entity;
    }

    private void handleRequest(RPCRequest request) throws CoreException {
        byte[] requestData = request.getData();
        String requestType = request.getType();
        if (requestType == null)
            throw new CoreException(ChatErrorCodes.ERROR_RPC_REQUESTTYPE_ILLEGAL, "RPCRequest type is null");
        if (requestData == null) {
            Byte encode = request.getEncode();
            if (encode == null)
                request.setEncode(RPCRequest.ENCODE_PB);
            try {
                request.persistent();
                if (request.getData() == null)
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_REQUESTDATA_NULL, "RPCRequest data is still null");
            } catch (Throwable t) {
                LoggerEx.error(TAG, "Persistent RPCRequest " + request.getType() + " failed " + ExceptionUtils.getFullStackTrace(t));
                throw new CoreException(ChatErrorCodes.ERROR_RPC_PERSISTENT_FAILED, "Persistent RPCRequest " + request.getType() + " failed " + t.getMessage());
            }
        }
    }

    private RPCResponse handleResponse(String requestType, RPCRequest request, byte[] data) throws CoreException, IllegalAccessException, InstantiationException {
        RPCEntity entity = getRPCEntityForClient(requestType, request);
        RPCResponse response = entity.responseClass.newInstance();
        response.setRequest(request);
        response.setData(data);
        response.setEncode(request.getEncode());
        response.setType(request.getType());

        try {
            response.resurrect();
        } catch (Throwable t) {
            LoggerEx.error(TAG, "RPCResponse " + requestType + " resurrect failed, " + ExceptionUtils.getFullStackTrace(t));
            throw new CoreException(ChatErrorCodes.ERROR_RPC_RESURRECT_FAILED, "RPCResponse " + requestType + " resurrect failed, " + t.getMessage());
        }
        return response;
    }

    RPCEntity getRPCEntityForServer(String requestType, Class<RPCServerAdapter> serverAdapterClass) throws CoreException {
        RPCEntity entity = typeEntityMap.get(requestType);
        if (entity == null) {
            Class<? extends RPCRequest> requestClass = null;
            Class<? extends RPCResponse> responseClass = null;
            Type[] types = serverAdapterClass.getGenericInterfaces();
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    if (pType.getRawType().equals(RPCServerAdapter.class)) {
                        Type[] params = pType.getActualTypeArguments();
                        if (params != null && params.length == 2) {
                            requestClass = (Class<? extends RPCRequest>) params[0];
                            responseClass = (Class<? extends RPCResponse>) params[1];
                        }
                    }
                }
            }

            if (requestClass != null && responseClass != null) {
                entity = new RPCEntity();
                entity.requestClass = requestClass;
                entity.responseClass = responseClass;
            } else {
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " and ResponseClass " + responseClass + " is not prepared for requestType " + requestType);
            }
            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
            if (previousEntity != null)
                entity = previousEntity;
        }
        return entity;
    }

    public String getRmiId() {
        return rmiId;
    }

    // public void setRmiId(String rmiId) {
//    this.rmiId = rmiId;
// }
    public Integer getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(Integer rmiPort) {
        this.rmiPort = rmiPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Long getExpireTime() {
        return expireTime;
    }
// public RMIServer getServer() {
//    return server;
// }
// public void setServer(RMIServer server) {
//    this.server = server;
// }


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

    @Override
    public boolean isConnected() {
        if (clientMonitorThread != null && clientMonitorThread.connected != null) {
            return clientMonitorThread.connected.get();
        }
        return false;
    }

    @Override
    public Integer getAverageLatency() {
//        if (averageCounter != null)
//            return averageCounter.getAverage();
        return null;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public void setRmiId(String rmiId) {
        this.rmiId = rmiId;
    }
}