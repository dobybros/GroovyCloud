package com.docker.rpc.async;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.rpc.remote.stub.ServerCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public class AsyncManager {
    private static AsyncManager instance;
    private Map<String, AsyncFuture> asyncCallbackHandlerMap = new ConcurrentHashMap<>();
    private Map<Long, String> crcMethodMap = new ConcurrentHashMap<>();
    private Map<String, Long> futureCrcMap = new ConcurrentHashMap<>();
    public static synchronized AsyncManager getInstance() {
        if (instance == null) {
            instance = new AsyncManager();
        }
        return instance;
    }
    public CompletableFuture<?> pushToAsyncMap(String callbackFutureId, AsyncFuture asyncFuture){
        asyncCallbackHandlerMap.computeIfAbsent(callbackFutureId, k-> asyncFuture);
        if (asyncFuture.getCrc() != null) {
            futureCrcMap.put(callbackFutureId, asyncFuture.getCrc());
        }
        TimerTaskEx timerTaskEx = new TimerTaskEx() {
            @Override
            public void execute() {
                AsyncFuture asyncFuture = asyncCallbackHandlerMap.get(callbackFutureId);
                if (asyncFuture != null) {
                    asyncFuture.getFuture().completeExceptionally(new CoreException(ChatErrorCodes.ERROR_ASYNC_TIMEOUT, "Async callback timeout, Now remove the future,service_class_method: " + crcMethodMap.get(futureCrcMap.get(callbackFutureId))));
                    asyncCallbackHandlerMap.remove(callbackFutureId);
                }
            }
        };
        timerTaskEx.setId(callbackFutureId);
        TimerEx.schedule(timerTaskEx, 60000L);
        asyncFuture.setTimerTaskEx(timerTaskEx);
        return asyncFuture.getFuture();
    }
}
