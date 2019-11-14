package com.docker.rpc.async;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.TimerTaskEx;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.RPCRequest;
import com.docker.rpc.remote.stub.RemoteServers;
import com.docker.rpc.remote.stub.RpcCacheManager;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public class AsyncRpcFuture {
    private String callbackFutureId;
    private Long crc;
    private CompletableFuture<?> future;
    private List<AsyncCallbackHandler> asyncCallbackHandlers;
    private Integer timeout; //s
    private TimerTaskEx timerTaskEx;
    private List<RemoteServers.Server> remoteServers = new ArrayList<>();
    private RPCClientAdapterMap rpcClientAdapterMap;
    private RPCRequest rpcRequest;
    private Random random = new Random();
    private int maxCount;
    private int count;
    private List<String> failedServers = new ArrayList<>();

    public AsyncRpcFuture(Long crc, Integer timeout) {
        this.callbackFutureId = ObjectId.get().toString();
        this.asyncCallbackHandlers = new ArrayList<>();
        this.crc = crc;
        if (timeout == null) {
            this.timeout = 24 * 60 * 60;
        }
        this.future = new CompletableFuture<>();
    }

    public TimerTaskEx getTimerTaskEx() {
        return timerTaskEx;
    }

    public void setTimerTaskEx(TimerTaskEx timerTaskEx) {
        this.timerTaskEx = timerTaskEx;
    }

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public CompletableFuture<?> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<?> future) {
        this.future = future;
    }

    public void addHandler(AsyncCallbackHandler asyncCallbackHandler) {
        if (!this.asyncCallbackHandlers.contains(asyncCallbackHandler)) {
            this.asyncCallbackHandlers.add(asyncCallbackHandler);
        }
    }

    public List<AsyncCallbackHandler> getAsyncCallbackHandlers() {
        return asyncCallbackHandlers;
    }

    public String getCallbackFutureId() {
        return callbackFutureId;
    }

    public void setCallbackFutureId(String callbackFutureId) {
        this.callbackFutureId = callbackFutureId;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public List<RemoteServers.Server> getRemoteServers() {
        return remoteServers;
    }

    public void setRemoteServers(String serverName, List<RemoteServers.Server> remoteServers, RPCClientAdapterMap rpcClientAdapterMap, RPCRequest rpcRequest) {
        this.remoteServers = remoteServers;
        this.rpcRequest = rpcRequest;
        this.rpcClientAdapterMap = rpcClientAdapterMap;
        int size = this.remoteServers.size() - 1;
        maxCount = size < 5 ? size : 5;
    }

    public void callNextServer(String fromServerName) {
        boolean hasServer = false;
        if (count < maxCount) {
            this.failedServers.add(fromServerName);
            for (RemoteServers.Server server : this.remoteServers) {
                if(!this.failedServers.contains(server.getServer())){
                    hasServer = true;
                    String ip = null;
                    Integer port = null;
                    if (rpcClientAdapterMap.isEnableSsl()) {
                        ip = server.getPublicDomain();
                        port = server.getSslRpcPort();
                    } else {
                        ip = server.getIp();
                        port = server.getRpcPort();
                    }
                    if (ip != null && port != null) {
                        RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(ip, port, server.getServer());
                        if (clientAdapter != null) {
                            try {
                                clientAdapter.callAsync(rpcRequest);
                                count++;
                            } catch (CoreException e) {
                                count++;
                                callNextServer(server.getServer());
                            }
                        }
                    }
                    break;
                }
            }

        }
        if(!hasServer){
            RpcCacheManager.getInstance().handlerAsyncRpcFuture(this.callbackFutureId);
            this.getFuture().completeExceptionally(new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "Call request " + crc + " outside failed with several retries."));
        }
    }

    public RPCClientAdapterMap getRpcClientAdapterMap() {
        return rpcClientAdapterMap;
    }

    public void setRpcClientAdapterMap(RPCClientAdapterMap rpcClientAdapterMap) {
        this.rpcClientAdapterMap = rpcClientAdapterMap;
    }

    public void handleAsyncHandler(Object result, List<String> exceptHandlerClass) {
        List<AsyncCallbackHandler> asyncCallbackHandlers = getAsyncCallbackHandlers();
        if (!asyncCallbackHandlers.isEmpty()) {
            for (AsyncCallbackHandler asyncCallbackHandler : asyncCallbackHandlers) {
                if (exceptHandlerClass != null && exceptHandlerClass.contains(asyncCallbackHandler.getClass().getSimpleName())) {
                    continue;
                }
                asyncCallbackHandler.setResult(result);
                asyncCallbackHandler.handle();
            }
        }
    }

}
