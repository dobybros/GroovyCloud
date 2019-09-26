package com.docker.rpc.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.AnalyticsLogger;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.alibaba.fastjson.JSON;
import com.docker.errors.CoreErrorCodes;
import com.docker.rpc.*;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.ServerCacheManager;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.server.OnlineServer;
import com.docker.utils.SpringContextUtil;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import script.groovy.object.GroovyObjectEx;
import script.groovy.servlets.Tracker;
import script.memodb.ObjectId;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CompletableFuture;

public class RMIServerImpl extends UnicastRemoteObject implements RMIServer {
    private RMIServerImplWrapper serverWrapper;
    private ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");

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
        if (type == null)
            throw new CoreException(ChatErrorCodes.ERROR_RPC_TYPE_NOMAPPING, "No server adapter found by type " + type);

        GroovyObjectEx<RPCServerAdapter> adapter = serverWrapper.serverAdapterMap.get(type);
        if (adapter == null)
            throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

        RPCServerAdapter serverAdapter = null;
        serverAdapter = adapter.getObject();
        RPCResponse response = serverAdapter.onCall(request);
        return response;
    }

    @Override
    public byte[] call(byte[] data, String type, Byte encode)
            throws RemoteException {
        if (serverWrapper.rmiServerHandler == null)
            throw new RemoteException("RPC handler is null");

        try {
            RPCRequest request = null;
            RPCServerAdapter serverAdapter = null;
            RPCEntity entity = null;
            if (MethodRequest.RPCTYPE.equals(type)) {
                if (serverWrapper.serverMethodInvocation == null)
                    serverWrapper.serverMethodInvocation = new RPCServerMethodInvocation();
                request = new MethodRequest();

                request.setEncode(encode);
                request.setType(type);
                request.setData(data);
                request.resurrect();
                RPCResponse response = serverWrapper.serverMethodInvocation.onCall((MethodRequest) request);
                if (response != null) {
                    byte[] responseData = response.getData();
                    if (responseData == null) {
                        if (response.getEncode() == null)
                            response.setEncode(RPCBase.ENCODE_PB);
                        response.persistent();
                    }
                    return response.getData();
                }
                return null;
            } else {
                GroovyObjectEx<RPCServerAdapter> adapter = serverWrapper.serverAdapterMap.get(type);
                if (adapter == null)
                    throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

                entity = serverWrapper.rmiServerHandler.getRPCEntityForServer(type, adapter.getGroovyClass());
                serverAdapter = adapter.getObject();
                request = entity.requestClass.newInstance();

                request.setEncode(encode);
                request.setType(type);
                request.setData(data);
                request.resurrect();
                RPCResponse response = serverAdapter.onCall(request);
                if (response != null) {
                    byte[] responseData = response.getData();
                    if (responseData == null) {
                        if (response.getEncode() == null)
                            response.setEncode(RPCBase.ENCODE_PB);
                        response.persistent();
                    }
                    return response.getData();
                }
            }
            return null;
        } catch (Throwable t) {
            String message = null;
            if (t instanceof CoreException) {
                message = ((CoreException) t).getCode() + "|" + t.getMessage();
            } else {
                message = t.getMessage();
            }
            throw new RemoteException(message, t);
        }
    }

    @Override
    public void callAsync(byte[] data, String type, Byte encode, String callbackFutureId) throws RemoteException, ServerNotActiveException {
        if (serverWrapper.rmiServerHandler == null)
            throw new RemoteException("RPC handler is null");
        switch (type) {
            case MethodRequest.RPCTYPE:
                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(() -> {
                    AsyncCallbackRequest asyncCallbackRequest = new AsyncCallbackRequest();
                    asyncCallbackRequest.setEncode(encode);
                    asyncCallbackRequest.setType(asyncCallbackRequest.getType());
                    asyncCallbackRequest.setCallbackFutureId(callbackFutureId);
                    MethodMapping methodMapping = null;
                    MethodRequest request = new MethodRequest();
                    Object returnObj = null;
                    Throwable throwable = null;
                    Long time = System.currentTimeMillis();
                    StringBuilder builder = new StringBuilder();
                    try {
                        if (serverWrapper.serverMethodInvocation == null)
                            serverWrapper.serverMethodInvocation = new RPCServerMethodInvocation();
                        request.setEncode(encode);
                        request.setType(type);
                        request.setData(data);
                        request.resurrect();
                        String parentTrackId = ((MethodRequest) request).getTrackId();
                        String currentTrackId = null;
                        if (parentTrackId != null) {
                            currentTrackId = ObjectId.get().toString();
                            Tracker tracker = new Tracker(currentTrackId, parentTrackId);
                            Tracker.trackerThreadLocal.set(tracker);
                        }
                        methodMapping = serverWrapper.serverMethodInvocation.getMethodMapping(request);
                        builder.append("$$async methodrequest:: " + methodMapping.getMethod().getDeclaringClass().getSimpleName() + "#" + methodMapping.getMethod().getName() + " $$service:: " + request.getService() + " $$parenttrackid:: " + parentTrackId + " $$currenttrackid:: " + currentTrackId + " $$args:: " + request.getArgsTmpStr());
                        returnObj = serverWrapper.serverMethodInvocation.oncallAsync(request, callbackFutureId);
                        asyncCallbackRequest.setCrc((request).getCrc());
                        asyncCallbackRequest.setFromService((request).getFromService());
                    } catch (Throwable t) {
                        t.printStackTrace();
                        String message = null;
                        if (t instanceof InvokerInvocationException) {
                            t = t.getCause();
                        }
                        if (t instanceof CoreException) {
                            message = ((CoreException) t).getCode() + "|" + t.getMessage();
                        } else {
                            message = t.getMessage();
                        }
                        throwable = t;
                        LoggerEx.error(TAG, "Async call remote failed, service_class_method: " + ServerCacheManager.getInstance().getCrcMethodMap().get(ServerCacheManager.getInstance().getFutureCrcMap().get(callbackFutureId)) + ", err: " + message);
                    } finally {
                        String ip = OnlineServer.getInstance().getIp();
                        Tracker.trackerThreadLocal.remove();
                        long invokeTokes = System.currentTimeMillis() - time;
                        builder.append(" $$takes:: " + invokeTokes);
                        builder.append(" $$sdockerip:: " + ip);
                        if (throwable != null) {
                            Exception exception = null;
                            if (throwable instanceof InvokerInvocationException) {
                                Throwable t = throwable.getCause();
                                if (t != null)
                                    throwable = t;
                            }
                            if (throwable instanceof CoreException) {
                                exception = (CoreException) throwable;
                            } else {
                                exception = new CoreException(ChatErrorCodes.ERROR_ASYNC_ERROR, "Async callback err,err: " + exception.getMessage());
                            }
                            asyncCallbackRequest.setException((CoreException) exception);
                            builder.append(" $$returnobj:: " + JSON.toJSONString(exception));
                            AnalyticsLogger.error(TAG, builder.toString());
                            handlePersistent(asyncCallbackRequest, request);
                        } else {
                            if (returnObj != null && returnObj instanceof CompletableFuture) {
//                                String methodName = methodMapping.getMethod().getName();
//                                Class<?> clazz = methodMapping.getMethod().getDeclaringClass();
                                CompletableFuture completeFuture = (CompletableFuture) returnObj;
                                completeFuture.whenCompleteAsync((result, e) -> {
                                    if (result != null) {
                                        asyncCallbackRequest.setDataObject(result);
                                    }
                                    if (e != null) {
                                        Throwable throwable1 = (Throwable) e;
                                        Throwable cause = throwable1.getCause();
                                        if (cause != null) {
                                            throwable1 = cause;
                                        }
                                        throwable1.printStackTrace();
                                        if (throwable1 instanceof CoreException) {
                                            throwable1 = (CoreException) throwable1;
                                        } else {
                                            throwable1 = new CoreException(ChatErrorCodes.ERROR_ASYNC_ERROR, "Async callback err,err: " + throwable1.getMessage());
                                        }
                                        asyncCallbackRequest.setException((CoreException) throwable1);
                                    }
                                    if (asyncCallbackRequest.getException() != null) {
                                        builder.append(" $$returnobj:: " + JSON.toJSONString(asyncCallbackRequest.getException()));
                                        AnalyticsLogger.error(TAG, builder.toString());
                                    } else {
                                        builder.append(" $$returnobj:: " + JSON.toJSONString(asyncCallbackRequest.getDataObject()));
                                        AnalyticsLogger.info(TAG, builder.toString());
                                    }
                                    handlePersistent(asyncCallbackRequest, request);
                                }, ServerStart.getInstance().getCoreThreadPoolExecutor());
                            }
                        }
                    }
                });
                break;
            case AsyncCallbackRequest.RPCTYPE:
                AsyncCallbackRequest asyncCallbackRequest = null;
                try {
                    asyncCallbackRequest = new AsyncCallbackRequest();
                    asyncCallbackRequest.setData(data);
                    asyncCallbackRequest.setEncode(encode);
                    asyncCallbackRequest.setType(type);
                    asyncCallbackRequest.resurrect();
                    if(asyncCallbackRequest.getCallbackFutureId() != null){
                        CompletableFuture completableFuture = ServerCacheManager.getInstance().getCompletableFuture(asyncCallbackRequest.getCallbackFutureId());
                        if (asyncCallbackRequest.getException() != null) {
                            completableFuture.completeExceptionally(asyncCallbackRequest.getException());
                        } else {
                            /*ServerCacheManager serverCacheManager = ServerCacheManager.getInstance();
                            CacheStorageFactory cacheStorageFactory = serverCacheManager.getCacheStorageFactory();
                            Map<String, CacheObj> cacheObjMap = serverCacheManager.getCacheMethodMap();
                            CacheObj cacheObj = cacheObjMap.get(String.valueOf(asyncCallbackRequest.getCrc()));
                            CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
                            String key = serverCacheManager.getCacheId(asyncCallbackRequest.getCallbackFutureId());
                            if (key != null) {
                                cacheObj.setKey(key);
                                cacheObj.setValue(asyncCallbackRequest.getDataObject());
                                try {
                                    cacheStorageAdapter.addCacheData(cacheObj);
                                } catch (CoreException coreException) {
                                    LoggerEx.error(TAG, "Add cache data failed on async call class is service_class_method: " + ServerCacheManager.getInstance().getCrcMethodMap().get(asyncCallbackRequest.getCrc()) + ",reason is " + coreException.getMessage());
                                }
                            }*/
                            completableFuture.complete(asyncCallbackRequest.getDataObject());
                        }
                    }
                } catch (Throwable t) {
                    if (asyncCallbackRequest != null && asyncCallbackRequest.getCallbackFutureId() != null) {
                        CompletableFuture completableFuture = ServerCacheManager.getInstance().getCompletableFuture(asyncCallbackRequest.getCallbackFutureId());
                        if (completableFuture == null) {
                            LoggerEx.error(TAG, "Async callback timeout, service_class_method: " + ServerCacheManager.getInstance().getCrcMethodMap().get(asyncCallbackRequest.getCrc()));
                        } else {
                            completableFuture.completeExceptionally(t);
                        }
                    } else {
                        String message = null;
                        if (t instanceof CoreException) {
                            message = ((CoreException) t).getCode() + "|" + t.getMessage();
                        } else {
                            message = t.getMessage();
                        }
                        throw new RemoteException(message, t);
                    }
                }
                break;
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

    private RPCClientAdapter getClientAdapter(RPCRequest request) {
        if (((MethodRequest) request).getSourceIp() != null && ((MethodRequest) request).getSourcePort() != null && ((MethodRequest) request).getFromServerName() != null) {
            ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
            MyBaseRuntime baseRuntime = (MyBaseRuntime) scriptManager.getBaseRuntime(((MethodRequest) request).getFromService());
            ServiceStubManager serviceStubManager = baseRuntime.getServiceStubManager();
            if (serviceStubManager != null) {
                RPCClientAdapterMap rpcClientAdapterMap = serviceStubManager.getClientAdapterMap();
                if (rpcClientAdapterMap != null) {
                    RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(((MethodRequest) request).getSourceIp(), ((MethodRequest) request).getSourcePort(), ((MethodRequest) request).getFromServerName());
                    if (clientAdapter != null) {
                        return clientAdapter;
                    }
                }
            }
        } else {
            LoggerEx.warn(TAG, "The request cant callback async, sourceIp: " + ((MethodRequest) request).getSourceIp() + ", sourcePort: " + ((MethodRequest) request).getSourcePort() + ",fromServerName: " + ((MethodRequest) request).getFromServerName());
        }
        return null;
    }

    private void callClientAdapterAsync(RPCClientAdapter clientAdapter, AsyncCallbackRequest asyncCallbackRequest, RPCRequest request) {
        if (clientAdapter != null) {
            try {
                clientAdapter.callAsync(asyncCallbackRequest);
            } catch (CoreException c) {
                //重試
                TimerEx.schedule(new TimerTaskEx() {
                    int tryTimes = 1;

                    @Override
                    public void execute() {
                        Boolean callStatus = true;
                        try {
                            clientAdapter.callAsync(asyncCallbackRequest);
                        } catch (CoreException ex) {
                            ex.printStackTrace();
                            callStatus = false;
                            LoggerEx.error(TAG, "Async callback failed, try again, tryTimes: " + tryTimes + ", host: " + ((MethodRequest) request).getSourceIp() + ",port: " + ((MethodRequest) request).getSourcePort() + ",serverName: " + ((MethodRequest) request).getFromServerName());
                            if (tryTimes == 5) {
                                TimerEx.cancel(this);
                                LoggerEx.error(TAG, "Async callback failed, cant try again, tryTimes: " + tryTimes + ", host: " + ((MethodRequest) request).getSourceIp() + ",port: " + ((MethodRequest) request).getSourcePort() + ",serverName: " + ((MethodRequest) request).getFromServerName());
                            }
                            tryTimes++;
                        } finally {
                            if (callStatus) {
                                TimerEx.cancel(this);
                                LoggerEx.info(TAG, "Async callback success, cant try again, tryTimes: " + tryTimes + ", host: " + ((MethodRequest) request).getSourceIp() + ",port: " + ((MethodRequest) request).getSourcePort() + ",serverName: " + ((MethodRequest) request).getFromServerName());
                            }
                        }
                    }
                }, 1000L, 5000L);
            }
        }
    }

    private void handlePersistent(AsyncCallbackRequest asyncCallbackRequest, RPCRequest request) {
        boolean persistentSuccess = true;

        try {
            asyncCallbackRequest.persistent();
        } catch (CoreException ex) {
            persistentSuccess = false;
            LoggerEx.error(TAG, "AsyncCallbackRequest persistent error,service_class_method: " + ServerCacheManager.getInstance().getCrcMethodMap().get(((MethodRequest) request).getCrc()) + ",err: " + ex.getMessage());
            ex.printStackTrace();
        }
        if (persistentSuccess) {
            callClientAdapterAsync(getClientAdapter(request), asyncCallbackRequest, request);
        }
    }
}

