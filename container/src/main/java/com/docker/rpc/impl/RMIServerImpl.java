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
import com.docker.rpc.async.AsyncCallbackRequest;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.async.AsyncRuntimeException;
import com.docker.rpc.remote.RpcServerInterceptor;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.server.OnlineServer;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        RPCResponse response = null;
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
                response = serverWrapper.serverMethodInvocation.onCall((MethodRequest) request);
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
                response = serverAdapter.onCall(request);
            }
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
        } catch (Throwable t) {
            LoggerEx.error(TAG, "RPC call type " + type + " occur error on server side, " + ExceptionUtils.getFullStackTrace(t));
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
                ServerStart.getInstance().getAsyncThreadPoolExecutor().execute(() -> {
                    AsyncCallbackRequest asyncCallbackRequest = new AsyncCallbackRequest();
                    asyncCallbackRequest.setFromServerName(OnlineServer.getInstance().getServer());
                    asyncCallbackRequest.setEncode(encode);
                    asyncCallbackRequest.setType(asyncCallbackRequest.getType());
                    asyncCallbackRequest.setCallbackFutureId(callbackFutureId);
                    ServiceSkeletonAnnotationHandler.SkelectonMethodMapping methodMapping = null;
                    MethodRequest request = new MethodRequest();
                    Object returnObj = null;
                    Throwable throwable = null;
                    Long time = System.currentTimeMillis();
                    StringBuilder builder = new StringBuilder();
                    List<RpcServerInterceptor> rpcServerInterceptors = null;
                    try {
                        if (serverWrapper.serverMethodInvocation == null)
                            serverWrapper.serverMethodInvocation = new RPCServerMethodInvocation();
                        request.setEncode(encode);
                        request.setType(type);
                        request.setData(data);
                        request.resurrect();
                        methodMapping = serverWrapper.serverMethodInvocation.getMethodMapping(request);
                        rpcServerInterceptors = methodMapping.getRpcServerInterceptors();
                        if (!handleInterceptors(data, type, encode, callbackFutureId, rpcServerInterceptors)) {
                            return;
                        }
                        String parentTrackId = ((MethodRequest) request).getTrackId();
                        String currentTrackId = null;
                        if (parentTrackId != null) {
                            currentTrackId = ObjectId.get().toString();
                            Tracker tracker = new Tracker(currentTrackId, parentTrackId);
                            Tracker.trackerThreadLocal.set(tracker);
                        }
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
                        LoggerEx.error(TAG, "Async call remote failed, service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()) == null ? null : RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()).getCrc()) + ", err: " + message + " stack: " + ExceptionUtils.getFullStackTrace(t));
                    } finally {
                        String ip = OnlineServer.getInstance().getIp();
                        Tracker.trackerThreadLocal.remove();
                        long invokeTokes = System.currentTimeMillis() - time;
                        builder.append(" $$takes:: " + invokeTokes);
                        builder.append(" $$sdockerip:: " + ip);
                        if (throwable != null) {
                            try {
                                Exception exception = null;
                                if (throwable instanceof InvokerInvocationException) {
                                    Throwable t = throwable.getCause();
                                    if (t != null)
                                        throwable = t;
                                }
                                if (throwable instanceof CoreException) {
                                    exception = (CoreException) throwable;
                                } else if(throwable instanceof AsyncRuntimeException){
                                    LoggerEx.error(TAG, "Async callback err,err: " + ExceptionUtils.getFullStackTrace(throwable));
                                    exception = new CoreException(ChatErrorCodes.ERROR_ASYNC_NEEDRETRY, "Async callback err,err: " + ExceptionUtils.getFullStackTrace(throwable));
                                }
                                asyncCallbackRequest.setException((CoreException) exception);
                                builder.append(" $$returnobj:: " + JSON.toJSONString(exception));
                                AnalyticsLogger.error(TAG, builder.toString());
                                handlePersistent(asyncCallbackRequest, request);
                            } finally {
                                handleInterceptorsAfter(rpcServerInterceptors);
                            }
                        } else {
                            if (returnObj != null && returnObj instanceof CompletableFuture) {
                                CompletableFuture completeFuture = (CompletableFuture) returnObj;
                                List<RpcServerInterceptor> theRpcServerInterceptors = rpcServerInterceptors;
                                completeFuture.whenCompleteAsync((result, e) -> {
                                    try {
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
                                            } else if(throwable1 instanceof AsyncRuntimeException){
                                                LoggerEx.error(TAG, "Async callback err,err: " + ExceptionUtils.getFullStackTrace(throwable1));
                                                throwable1 = new CoreException(ChatErrorCodes.ERROR_ASYNC_NEEDRETRY, "Async callback err,err: " + ExceptionUtils.getFullStackTrace(throwable1));
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
                                    } finally {
                                        handleInterceptorsAfter(theRpcServerInterceptors);
                                    }
                                }, ServerStart.getInstance().getAsyncThreadPoolExecutor());
                            } else {
                                LoggerEx.error(TAG, "Async return object is not CompletableFuture");
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
                    if (asyncCallbackRequest.getCallbackFutureId() != null) {
                        AsyncRpcFuture asyncRpcFuture = null;
                        if (asyncCallbackRequest.getException() != null) {
                            if (asyncCallbackRequest.getException().getCode() == ChatErrorCodes.ERROR_ASYNC_NEEDRETRY) {
                                asyncRpcFuture = RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId());
                                if (asyncRpcFuture != null) {
                                    LoggerEx.error(TAG, "Async excute error with AsyncRuntimeException, errMsg: " + ExceptionUtils.getFullStackTrace(asyncCallbackRequest.getException()));
                                    asyncRpcFuture.callNextServer(asyncCallbackRequest.getFromServerName());
                                }
                            } else {
                                asyncRpcFuture = RpcCacheManager.getInstance().handlerAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId());
                                if (asyncRpcFuture != null) {
                                    CompletableFuture completableFuture = asyncRpcFuture.getFuture();
                                    if (completableFuture != null) {
                                        completableFuture.completeExceptionally(asyncCallbackRequest.getException());
                                        LoggerEx.error(TAG, "Async excute error, errMsg: " + ExceptionUtils.getFullStackTrace(asyncCallbackRequest.getException()));
                                    }
                                }
                            }

                        } else {
                            asyncRpcFuture = RpcCacheManager.getInstance().handlerAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId());
                            if (asyncRpcFuture != null) {
                                CompletableFuture completableFuture = asyncRpcFuture.getFuture();
                                if (completableFuture != null) {
                                    completableFuture.complete(asyncCallbackRequest.getDataObject());
                                    asyncRpcFuture.handleAsyncHandler(asyncCallbackRequest.getDataObject(), null);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    if (asyncCallbackRequest != null && asyncCallbackRequest.getCallbackFutureId() != null) {
                        CompletableFuture completableFuture = new CompletableFuture();
                        if (completableFuture == null) {
                            LoggerEx.error(TAG, "Async callback timeout, service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()) == null ? null : RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()).getCrc()));
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
                        LoggerEx.error(TAG, "AsyncCallbackRequest occur error, " + ExceptionUtils.getFullStackTrace(t));
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
                RPCClientAdapterMap clientAdapterMap = null;
                if (serviceStubManager.getUsePublicDomain()) {
                    clientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMapSsl();
                } else {
                    clientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMap();
                }
                RPCClientAdapter clientAdapter = clientAdapterMap.registerServer(((MethodRequest) request).getSourceIp(), ((MethodRequest) request).getSourcePort(), ((MethodRequest) request).getFromServerName());
                if (clientAdapter != null) {
                    return clientAdapter;
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
                TimerEx.schedule(new TimerTaskEx("RetryCallAsync") {
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
            LoggerEx.error(TAG, "AsyncCallbackRequest persistent error,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()) == null ? null : RpcCacheManager.getInstance().getAsyncRpcFuture(asyncCallbackRequest.getCallbackFutureId()).getCrc()) + ",err: " + ExceptionUtils.getFullStackTrace(ex));
            ex.printStackTrace();
        }
        if (persistentSuccess) {
            callClientAdapterAsync(getClientAdapter(request), asyncCallbackRequest, request);
        }
    }

    private boolean handleInterceptors(byte[] data, String type, Byte encode, String callbackFutureId, List<RpcServerInterceptor> rpcServerInterceptors) throws RemoteException, ServerNotActiveException, CoreException {
        if (rpcServerInterceptors != null && !rpcServerInterceptors.isEmpty()) {
            MethodRequest methodRequest = new MethodRequest();
            methodRequest.setData(data);
            methodRequest.setType(type);
            methodRequest.setEncode(encode);
            methodRequest.setCallbackFutureId(callbackFutureId);
            for (RpcServerInterceptor rpcServerInterceptor : rpcServerInterceptors) {
                Object result = rpcServerInterceptor.invoke(methodRequest, this);
                if (result instanceof Boolean) {
                    return (boolean) result;
                }
            }
        }
        return true;
    }

    private void handleInterceptorsAfter(List<RpcServerInterceptor> rpcServerInterceptors) {
        if (rpcServerInterceptors != null && !rpcServerInterceptors.isEmpty()) {
            for (RpcServerInterceptor rpcServerInterceptor : rpcServerInterceptors) {
                rpcServerInterceptor.afterInvoke();
            }
        }
    }
}

