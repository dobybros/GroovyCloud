package com.docker.rpc.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.AverageCounter;
import chat.utils.IPHolder;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;
import com.docker.rpc.RPCServerAdapter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RMIClientHandler extends RPCClientAdapter {
    public static final String RMIID_SSL_SUFFIX = "_SSL";
    // public static final String RMI_ID = "RMIINTERFACE";
    public static final int RMI_PORT = 2222;

    private String rmiId;
    private Integer rmiPort = RMI_PORT;

    @Resource
    private IPHolder ipHolder;

    //both
    private Registry registry;
    private RMIServer server;

    private boolean enableSsl = false;

    private ConcurrentHashMap<String, RPCEntity> typeEntityMap = new ConcurrentHashMap<>();

    //Client
    private String serverHost;
    private AverageCounter averageCounter;

    private Long touch;
    private Long idleCheckPeriod = TimeUnit.SECONDS.toMillis(10);
    private static final String TAG = "RMI";
    private Long expireTime;
    private ExpireListener<RPCClientAdapter> expireListener;

    private boolean isStarted = true;

    /** rpc ssl certificate */
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
        builder.append("averageCounter: " + averageCounter + " ");
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
            if(connected.get()) {
                for(ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.connected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " connected failed, " + t.getMessage());
                    }
                }
            } else {
                for(ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.disconnected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + t.getMessage());
                    }
                }
            }
        }
        public void run() {
            LoggerEx.info(TAG, "Start monitoring RMI client connection");
            while(isStarted) {
                if(expireTime != null && expireListener != null) {
                    if(touch + expireTime < System.currentTimeMillis()) {
                        try {
                            if (expireListener.expired(RMIClientHandler.this, touch, expireTime)) {
                                break;
                            }
                        } catch (Throwable t) {
                            LoggerEx.info(TAG, "Handle server expire failed, " + t.getMessage() + " the expireListener " + expireListener + " will be ignored...");
                        }
                    }
                }
                synchronized (connected) {
                    if(connected.get()) {
                        if(touch + idleCheckPeriod < System.currentTimeMillis()) {
                            try {
                                server.alive();
                            } catch(Throwable ce) {
                                LoggerEx.info(TAG, "Check server alive failed, " + ce.getMessage() + " need reconnect..." + ", server : " + server.toString() + ", serverHost : "  + serverHost);
                                connected.compareAndSet(true, false);
                                for(ClientAdapterStatusListener statusListener : statusListeners) {
                                    try {
                                        statusListener.disconnected(rmiId);
                                    } catch (Throwable t) {
                                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + t.getMessage());
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
                        if(connected.get())
                            continue;
                    }
                }
                if(isStarted) {
                    LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " start connecting...");
                    try {
                        Registry registry = LocateRegistry.getRegistry(serverHost, rmiPort);
                        server = (RMIServer) registry.lookup(rmiId);
//                        serverImpl.initClient(server);
                        connected.set(true);
                        for(ClientAdapterStatusListener statusListener : statusListeners) {
                            try {
                                statusListener.connected(rmiId);
                            } catch (Throwable t) {
                                LoggerEx.error(TAG, "statusListener " + statusListener + " connected failed, " + t.getMessage());
                            }
                        }
                        LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " connected!");
                    } catch(Throwable t) {
                        t.printStackTrace();
                        if(t instanceof  NotBoundException) {
                            LoggerEx.error(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " not bound any more, " + t.getMessage());
                        } else {
                            LoggerEx.error(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " connect failed, " + t.getMessage());
                        }
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                for(ClientAdapterStatusListener statusListener : statusListeners) {
//                   try {
//                      statusListener.disconnected(serverName);
//                   } catch (Throwable t1) {
//                      LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + t1.getMessage());
//                   }
//                }
                    }
                }
            }

            //Will unexportObject in client mode.
            if(server != null) {
                try {
                    boolean bool = UnicastRemoteObject.unexportObject(server, true);
                    LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " is destroyed, unexport " + server + " result " + bool);
                } catch (NoSuchObjectException e) {
//             e.printStackTrace();
                    LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " is destroyed, NoSuchObject " + server + " error " + e.getMessage());
                }
            }
            LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " monitor stopped...");
            for(ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.terminated(rmiId);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "statusListener " + statusListener + " terminated failed, " + t.getMessage());
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
        averageCounter = new AverageCounter();
        if(enableSsl && !rmiId.endsWith(RMIID_SSL_SUFFIX))
            rmiId = rmiId + RMIID_SSL_SUFFIX;
        for(ClientAdapterStatusListener statusListener : statusListeners) {
            try {
                statusListener.started(rmiId);
            } catch (Throwable t) {
                LoggerEx.error(TAG, "statusListener " + statusListener + " started failed, " + t.getMessage());
            }
        }
        boolean startConnected = false;
        try {
            if(StringUtils.isBlank(serverHost))
                throw new CoreException(ChatErrorCodes.ERROR_ILLEGAL_PARAMETER, "Server host is illegal, " + serverHost);
            if (enableSsl) {
                setSslProp();
                registry = LocateRegistry.getRegistry(serverHost, rmiPort, new SslRMIClientSocketFactory());
            } else{
                registry = LocateRegistry.getRegistry(serverHost, rmiPort);
            }
            server = (RMIServer) registry.lookup(rmiId);

//            serverImpl.initClient(server);

//       System.out.println(new String(server.call("world".getBytes(), "ab1c", 1)));
            LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " server " + rmiId + " client connected!");
            startConnected = true;
        } catch(Throwable t) {
            t.printStackTrace();
            LoggerEx.error(TAG, "RMI clientStart failed, " + t.getMessage());
            startConnected = false;
//       if(t instanceof CoreException)
//          throw (CoreException)t;
//       throw new CoreException(ChatErrorCodes.ERROR_UNKNOWN, "Initialize RMIClientHandler " + serverHost + " | " + rmiPort + " faield, " + t.getMessage());
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
        if(isStarted) {
            isStarted = false;
            expireListener = null;

            if(clientMonitorThread != null) {
                synchronized (clientMonitorThread.connected) {
                    clientMonitorThread.connected.notify();
                }
            }
        }
    }

    public synchronized void serverDestroy() {
        try {
            registry.unbind(rmiId);
            LoggerEx.info(TAG, "RMI " + serverHost +  " port " + rmiPort + " rmiId " + rmiId + " server is destroyed!");
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "RMI " + serverHost +  " port " + rmiPort + " rmiId " + rmiId + " server destroy failed, " + e.getMessage());
        }
    }

    @Override
    public RPCResponse call(RPCRequest request) throws CoreException {
        try {
            RPCResponse response = callPrivate(request);
            for(ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.called(rmiId, request, response);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(called) occured error " + t.getMessage() + " for request " + request + " and response " + response);
                }
            }
            return response;
        } catch (CoreException e) {
            for(ClientAdapterStatusListener statusListener : statusListeners) {
                try {
                    statusListener.callFailed(rmiId, request, e);
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "CallListener(callFailed) occured error " + t.getMessage() + " for request " + request);
                    if(t instanceof CoreException)
                        throw t;
                }
            }
            throw e;
        }
    }

    private RPCResponse callPrivate(RPCRequest request) throws CoreException {
        touch();
        if(!clientMonitorThread.connected.get())
            throw new CoreException(ChatErrorCodes.ERROR_RPC_DISCONNECTED, "RPC (" + serverHost + ":" + rmiPort + ") is disconnected for " + request.getType() + ": " + request.toString());
        try {
            byte[] requestData = request.getData();
            String requestType = request.getType();
            if(requestType == null)
                throw new CoreException(ChatErrorCodes.ERROR_RPC_REQUESTTYPE_ILLEGAL, "RPCRequest type is null");
            if(requestData == null) {
                Byte encode = request.getEncode();
                if(encode == null)
                    request.setEncode(RPCRequest.ENCODE_PB);
                try {
                    request.persistent();
                    if(request.getData() == null)
                        throw new CoreException(ChatErrorCodes.ERROR_RPC_REQUESTDATA_NULL, "RPCRequest data is still null");
                } catch (Throwable t) {
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_PERSISTENT_FAILED, "Persistent RPCRequest " + request.getType() + " failed " + t.getMessage());
                }
            }
            long time = System.currentTimeMillis();
            byte[] data = server.call(request.getData(), requestType, request.getEncode());
            if(averageCounter != null)
                averageCounter.add((int) (System.currentTimeMillis() - time));
            if(data == null) {
                LoggerEx.error(TAG, "Server call return null for reuqest " + requestType);
                return null;
            }
            RPCEntity entity = getRPCEntityForClient(requestType, request);
            RPCResponse response = entity.responseClass.newInstance();
            response.setRequest(request);
            response.setData(data);
            response.setEncode(request.getEncode());
            response.setType(request.getType());

            try {
                response.resurrect();
            } catch (Throwable t) {
                throw new CoreException(ChatErrorCodes.ERROR_RPC_RESURRECT_FAILED, "RPCResponse " + requestType + " resurrect failed, " + t.getMessage());
            }

            return response;
        } catch(ConnectException|ConnectIOException ce) {
            if(clientMonitorThread.connected.compareAndSet(true, false)) {
                for(ClientAdapterStatusListener statusListener : statusListeners) {
                    try {
                        statusListener.disconnected(rmiId);
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "statusListener " + statusListener + " disconnected failed, " + t.getMessage());
                    }
                }
                synchronized (clientMonitorThread.connected) {
                    clientMonitorThread.connected.notify();
                }
            }
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED, "RMI call failed, " + ce.getMessage() + " start reconnecting...");
        } catch (Throwable t) {
            if(t instanceof ServerException) {
                Throwable remoteException = t.getCause();
                if(remoteException instanceof RemoteException) {
                    Throwable coreException = remoteException.getCause();
                    if(coreException instanceof CoreException) {
                        throw (CoreException)coreException;
                    }
                }
            }
            if(t instanceof CoreException)
                throw (CoreException) t;
            t.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_RMICALL_FAILED, "RMI call failed, " + t.getMessage());
        }
    }

    RPCEntity getRPCEntityForClient(String requestType, RPCRequest request) throws CoreException {
        RPCEntity entity = typeEntityMap.get(requestType);
        if(entity == null) {
            String requestClass = request.getClass().getName();
            String responseClassString = null;
            final String REQUEST_SUFIX = "Request";
            final String RESPONSE_SUFIX = "Response";
            if(!requestClass.endsWith(REQUEST_SUFIX))
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " don't contain Request as sufix. ");

            responseClassString = requestClass.substring(0, requestClass.length() - REQUEST_SUFIX.length()) + RESPONSE_SUFIX;
            Class<? extends RPCResponse> responseClass = null;
            try {
                responseClass = (Class<? extends RPCResponse>) Class.forName(responseClassString);
            } catch (ClassNotFoundException|ClassCastException e) {
                e.printStackTrace();
                throw new CoreException(ChatErrorCodes.ERROR_RPC_TYPE_REQUEST_NOMAPPING, "RPC type " + requestType + " don't have correct class name " + responseClassString + ". " + e.getMessage());
            }
            if(requestClass != null && responseClass != null) {
                entity = new RPCEntity();
                entity.requestClass = request.getClass();
                entity.responseClass = responseClass;
            }
            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
            if(previousEntity != null)
                entity = previousEntity;
        }
        return entity;
    }

    RPCEntity getRPCEntityForServer(String requestType, Class<RPCServerAdapter> serverAdapterClass) throws CoreException {
        RPCEntity entity = typeEntityMap.get(requestType);
        if(entity == null) {
            Class<? extends RPCRequest> requestClass = null;
            Class<? extends RPCResponse> responseClass = null;
            Type[] types = serverAdapterClass.getGenericInterfaces();
            for (Type type : types) {
                if(type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    if(pType.getRawType().equals(RPCServerAdapter.class)) {
                        Type[] params = pType.getActualTypeArguments();
                        if(params != null && params.length == 2) {
                            requestClass = (Class<? extends RPCRequest>) params[0];
                            responseClass = (Class<? extends RPCResponse>) params[1];
                        }
                    }
                }
            }

            if(requestClass != null && responseClass != null) {
                entity = new RPCEntity();
                entity.requestClass = requestClass;
                entity.responseClass = responseClass;
            } else {
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " and ResponseClass " + responseClass + " is not prepared for requestType " + requestType);
            }
            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
            if(previousEntity != null)
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
        if(clientMonitorThread != null && clientMonitorThread.connected != null) {
            return clientMonitorThread.connected.get();
        }
        return false;
    }

    @Override
    public Integer getAverageLatency() {
        if(averageCounter != null)
            return averageCounter.getAverage();
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