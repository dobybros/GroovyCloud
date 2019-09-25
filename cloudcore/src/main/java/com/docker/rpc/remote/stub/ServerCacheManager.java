package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.data.CacheObj;
import com.docker.storage.cache.CacheStorageFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/8/22.
 * Description：Used for this server globally
 */
public class ServerCacheManager {
    private static final String TAG = ServerCacheManager.class.getSimpleName();
    public static ServerCacheManager instance;
    private Map<String, CompletableFuture<?>> callbackFutureMap = new ConcurrentHashMap<>();
    private Map<String, TimerTaskEx> callbackFutureTimerMap = new ConcurrentHashMap<>();
    private Map<Long, String> crcMethodMap = new ConcurrentHashMap<>();
    private Map<String, Long> futureCrcMap = new ConcurrentHashMap<>();
    private CacheStorageFactory cacheStorageFactory = new CacheStorageFactory();
    private Map<String, CacheObj> cacheMethodMap = new ConcurrentHashMap<>();

    public static synchronized ServerCacheManager getInstance() {
        if (instance == null) {
            instance = new ServerCacheManager();
        }
        return instance;
    }


    public CompletableFuture pushToCallbackFutureMap(String id, Long crc) {
        CompletableFuture<?> future = new CompletableFuture<>();
        callbackFutureMap.put(id, future);
        if (crc != null) {
            futureCrcMap.put(id, crc);
        }
        TimerTaskEx timerTaskEx = new TimerTaskEx() {
            @Override
            public void execute() {
                CompletableFuture completableFuture = callbackFutureMap.get(id);
                if (completableFuture != null) {
                    completableFuture.completeExceptionally(new CoreException(ChatErrorCodes.ERROR_ASYNC_TIMEOUT, "Async callback timeout, Now remove the future,service_class_method: " + crcMethodMap.get(futureCrcMap.get(id))));
                    callbackFutureMap.remove(id);
                    futureCrcMap.remove(id);
                    callbackFutureTimerMap.remove(id);
                }
            }
        };
        timerTaskEx.setId(id);
        TimerEx.schedule(timerTaskEx, 60000L);
        callbackFutureTimerMap.put(id, timerTaskEx);
        return future;
    }

    public CompletableFuture getCompletableFuture(String id) {
        CompletableFuture future = callbackFutureMap.get(id);
        if (future != null) {
            TimerTaskEx taskEx = callbackFutureTimerMap.get(id);
            if (taskEx != null) {
                TimerEx.cancel(taskEx);
            }
            callbackFutureTimerMap.remove(id);
            callbackFutureMap.remove(id);
            futureCrcMap.remove(id);
        }
        return future;
    }

    public void addCacheMethodMap(String key, CacheObj cacheObj) {
        if (key != null && cacheObj != null) {
            if (!cacheMethodMap.containsKey(key)) {
                cacheMethodMap.put(key, cacheObj);
            }
        }
    }

    public Map<String, CacheObj> getCacheMethodMap() {
        return cacheMethodMap;
    }

    public Map<Long, String> getCrcMethodMap() {
        return crcMethodMap;
    }

    public Map<String, Long> getFutureCrcMap() {
        return futureCrcMap;
    }

    public CacheStorageFactory getCacheStorageFactory() {
        return cacheStorageFactory;
    }
}
