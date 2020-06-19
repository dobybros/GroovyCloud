package com.docker.rpc.impl;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.IPHolder;
import com.docker.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.RPCResponse;
import com.docker.rpc.RPCServerAdapter;
import com.docker.server.OnlineServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;

public class RMIServerHandler {
    public static final String RMIID_SSL_SUFFIX = "_SSL";
    public static final int RMI_PORT = 2222;

    private String rmiId;
    private Integer rmiPort = RMI_PORT;
    private String serverName;

//    @Resource
    private IPHolder ipHolder;
    private ConcurrentHashMap<String, RPCEntity> typeEntityMap = new ConcurrentHashMap<>();

    //both
    private Registry registry;
    private RMIServer server;
    private RMIServerImplWrapper serverImpl;

    private boolean enableSsl = false;

    private static final String TAG = "RMI";

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
        return builder.toString();
    }

    public synchronized void serverStart() {
        serverStart(ipHolder.getIp());
    }

    public synchronized void serverStart(String ip) {
        rmiId = OnlineServer.getInstance().getServer();
        try {
            if(enableSsl && !rmiId.endsWith(RMIID_SSL_SUFFIX))
                rmiId = rmiId + RMIID_SSL_SUFFIX;
            LoggerEx.info(TAG, "InetAddress host name : " + InetAddress.getLocalHost().getHostName() + ", InetAddress host address : " + InetAddress.getLocalHost().getHostAddress());
            System.setProperty("java.rmi.server.hostname", ip);
            if(enableSsl) {
                System.setProperty("java.rmi.server.sslport", String.valueOf(rmiPort)); //I made it up for pass port to somewhere else.
                setSslProp();
                registry = LocateRegistry.createRegistry(rmiPort,  new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory(null, null, true));
            }else {
                System.setProperty("java.rmi.server.port", String.valueOf(rmiPort)); //I made it up for pass port to somewhere else.
                registry = LocateRegistry.createRegistry(rmiPort);
            }
            server = serverImpl.initServer(enableSsl);

//            registry = LocateRegistry.createRegistry(rmiPort);
//            server = serverImpl.initServer();

            registry.bind(rmiId, server);
            LoggerEx.info(TAG, "RMI server IP : " + ipHolder.getIp() + " port : " + rmiPort + " started!" + " System host name : " + System.getProperty("java.rmi.server.hostname") + ", System port : " + ipHolder.getIp());
        } catch(Throwable t) {
            t.printStackTrace();
            LoggerEx.fatal(TAG, "RMIClientHandler server start failed. Server will be shutdown... " + ExceptionUtils.getFullStackTrace(t));
            OnlineServer.shutdownNow();
            System.exit(0);
        } finally {
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
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
    }

    private void resetSslProp() {
        System.getProperties().remove("javax.net.ssl.debug");
        System.getProperties().remove("javax.net.ssl.trustStore");
        System.getProperties().remove("javax.net.ssl.trustStorePassword");
        System.getProperties().remove("javax.net.ssl.keyStore");
        System.getProperties().remove("javax.net.ssl.keyStorePassword");
    }

    public synchronized void serverDestroy() {
        try {
            registry.unbind(rmiId);
            LoggerEx.info(TAG, "RMI port " + rmiPort + " server " + serverName + " server is destroyed!");
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "RMI port " + rmiPort + " server " + serverName + " server destroy failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }
    public RPCEntity getRPCEntityForServer(String requestType, Class<RPCServerAdapter> serverAdapterClass) throws CoreException {
        if(StringUtils.isBlank(requestType)) return null;

        RPCEntity entity = typeEntityMap.get(requestType);
        if(entity == null) {
            Class<? extends RPCRequest> requestClass = null;
            Class<? extends RPCResponse> responseClass = null;
            switch (requestType) {
                case "smsg":
                    try {
                        requestClass = (Class<? extends RPCRequest>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.ServerMessageRequest");
                        responseClass = (Class<? extends RPCResponse>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.ServerMessageResponse");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case "uol":
                    try {
                        requestClass = (Class<? extends RPCRequest>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.UserOnlineRequest");
                        responseClass = (Class<? extends RPCResponse>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.UserOnlineResponse");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case "proxyim":
                    try {
                        requestClass = (Class<? extends RPCRequest>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.ProxyIMRequest");
                        responseClass = (Class<? extends RPCResponse>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.ProxyIMResponse");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case "improxy":
                    try {
                        requestClass = (Class<? extends RPCRequest>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest");
                        responseClass = (Class<? extends RPCResponse>) Class.forName("com.dobybros.chat.rpc.reqres.balancer.IMProxyResponse");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
            }
            if(requestClass != null && responseClass != null) {
                entity = new RPCEntity();
                entity.requestClass = requestClass;
                entity.responseClass = responseClass;
            } else {
                throw new CoreException(CoreErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " and ResponseClass " + responseClass + " is not prepared for requestType " + requestType);
            }
            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
            if(previousEntity != null)
                entity = previousEntity;
        }
        return entity;
    }
//    RPCEntity getRPCEntityForServer(String requestType, Class<RPCServerAdapter> serverAdapterClass) throws CoreException {
//        RPCEntity entity = typeEntityMap.get(requestType);
//        if(entity == null) {
//            Class<? extends RPCRequest> requestClass = null;
//            Class<? extends RPCResponse> responseClass = null;
//            Type[] types = serverAdapterClass.getGenericInterfaces();
//            for (Type type : types) {
//                if(type instanceof ParameterizedType) {
//                    ParameterizedType pType = (ParameterizedType) type;
//                    if(pType.getRawType().equals(RPCServerAdapter.class)) {
//                        Type[] params = pType.getActualTypeArguments();
//                        if(params != null && params.length == 2) {
//                            requestClass = (Class<? extends RPCRequest>) params[0];
//                            responseClass = (Class<? extends RPCResponse>) params[1];
//                        }
//                    }
//                }
//            }
//
//            if(requestClass != null && responseClass != null) {
//                entity = new RPCEntity();
//                entity.requestClass = requestClass;
//                entity.responseClass = responseClass;
//            } else {
//                throw new CoreException(CoreErrorCodes.ERROR_RPC_ILLEGAL, "RequestClass " + requestClass + " and ResponseClass " + responseClass + " is not prepared for requestType " + requestType);
//            }
//            RPCEntity previousEntity = typeEntityMap.putIfAbsent(requestType, entity);
//            if(previousEntity != null)
//                entity = previousEntity;
//        }
//        return entity;
//    }
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
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

    public RMIServerImplWrapper getServerImpl() {
        return serverImpl;
    }

    public void setServerImpl(RMIServerImplWrapper serverImpl) {
        this.serverImpl = serverImpl;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public IPHolder getIpHolder() {
        return ipHolder;
    }

    public void setIpHolder(IPHolder ipHolder) {
        this.ipHolder = ipHolder;
    }
}