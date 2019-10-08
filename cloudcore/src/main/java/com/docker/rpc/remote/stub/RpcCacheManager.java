package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.data.CacheObj;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.storage.cache.CacheStorageFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/8/22.
 * Description：Used for this rpc service globally
 */
public class RpcCacheManager {
    private static final String TAG = RpcCacheManager.class.getSimpleName();
    public static RpcCacheManager instance;
    private Map<String, AsyncRpcFuture> asyncCallbackHandlerMap = new ConcurrentHashMap<>();
    private Map<Long, String> crcMethodMap = new ConcurrentHashMap<>();

    public AsyncRpcFuture pushToAsyncRpcMap(String callbackFutureId, AsyncRpcFuture asyncFuture){
        asyncCallbackHandlerMap.computeIfAbsent(callbackFutureId, k-> asyncFuture);
        TimerTaskEx timerTaskEx = new TimerTaskEx() {
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
        TimerEx.schedule(timerTaskEx, 65000L);
        asyncFuture.setTimerTaskEx(timerTaskEx);
        return asyncFuture;
    }
    public AsyncRpcFuture handlerAsyncRpcFuture(String callbackFutureId) {
        AsyncRpcFuture asyncFuture= asyncCallbackHandlerMap.remove(callbackFutureId);
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
}
