package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.RPCClientAdapterMapFactory;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.server.OnlineServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/8/22.
 * Description：Used for this rpc service globally
 */
public class RpcCacheManager {
    private static RpcCacheManager instance;
    private Map<String, AsyncRpcFuture> asyncCallbackHandlerMap = new ConcurrentHashMap<>();
    private Map<Long, String> crcMethodMap = new ConcurrentHashMap<>();

    public AsyncRpcFuture pushToAsyncRpcMap(String callbackFutureId, AsyncRpcFuture asyncFuture){
        asyncCallbackHandlerMap.computeIfAbsent(callbackFutureId, k-> asyncFuture);
        if(asyncFuture.getTimeout() != null){
            TimerTaskEx timerTaskEx = new TimerTaskEx("AsyncDeleteFutureWhenTimeout" + "-" + asyncFuture.getCrc().toString()) {
                @Override
                public void execute() {
                    AsyncRpcFuture asyncFuture = asyncCallbackHandlerMap.get(callbackFutureId);
                    if (asyncFuture != null) {
                        asyncFuture.getFuture().completeExceptionally(new CoreException(ChatErrorCodes.ERROR_ASYNC_TIMEOUT, "Async callback timeout, Now remove the future,service_class_method: " + crcMethodMap.get(asyncFuture.getCrc())));
                        asyncCallbackHandlerMap.remove(callbackFutureId);
                    }
                }
            };
            timerTaskEx.setId(callbackFutureId);
            TimerEx.schedule(timerTaskEx, Long.valueOf(asyncFuture.getTimeout() * 1000));
            asyncFuture.setTimerTaskEx(timerTaskEx);
        }
        return asyncFuture;
    }
    public AsyncRpcFuture handlerAsyncRpcFuture(String callbackFutureId) {
        AsyncRpcFuture asyncFuture= asyncCallbackHandlerMap.remove(callbackFutureId);
        String server = OnlineServer.getInstance().getServer();
        if(server != null){
            RPCClientAdapterMap rpcClientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMap();
            RPCClientAdapter rpcClientAdapter = rpcClientAdapterMap.getClientAdapter(server);
            if(rpcClientAdapter != null){
                rpcClientAdapter.removeFromServerFutureList(callbackFutureId);
            }
        }
        if (asyncFuture != null) {
            TimerTaskEx taskEx = asyncFuture.getTimerTaskEx();
            if (taskEx != null) {
                taskEx.cancel();
            }
            return asyncFuture;
        }
        return null;
    }
    public AsyncRpcFuture getAsyncRpcFuture(String callbackFutureId){
        return asyncCallbackHandlerMap.get(callbackFutureId);
    }
    public void putCrcMethodMap(Long crc, String value){
        crcMethodMap.put(crc, value);
    }
    public String getMethodByCrc(Long crc){
        if(crc != null){
            return crcMethodMap.get(crc);
        }
        return null;
    }
    public synchronized static RpcCacheManager getInstance() {
        if(instance == null){
            instance = new RpcCacheManager();
        }
        return instance;
    }
}
