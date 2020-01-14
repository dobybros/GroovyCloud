package com.docker.storage.cache;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.storage.cache.handlers.CacheStorageAdapter;
import com.docker.storage.cache.handlers.RedisCacheStorageHandler;
import com.docker.storage.redis.RedisHandler;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CacheStorageFactory {
    public static final String TAG = CacheStorageFactory.class.getSimpleName();
    private static volatile CacheStorageFactory instance;
    public static final String CLASS_EXTENSION = "CacheStorageHandler";
    private Map<String, Map<String, CacheStorageAdapter>> cacheAdapterMap;
    private Map<String, CacheStorageAdapter> localCacheAdapterMap;

    public CacheStorageAdapter getCacheStorageAdapter(String cacheMethod, String host) {
//        LoggerEx.info(TAG, "Cache method is " + cacheMethod + ", host is " + host);
        if (StringUtils.isBlank(cacheMethod)) {
            cacheMethod = CacheStorageAdapter.DEFAULT_CACHE_METHOD;
        }
        if (host == null){
            if (localCacheAdapterMap == null){
                localCacheAdapterMap = new ConcurrentHashMap<>();
            }
            if(localCacheAdapterMap.get(cacheMethod) != null){
                return localCacheAdapterMap.get(cacheMethod);
            }else{
                CacheStorageAdapter cacheStorageAdapter = createCacheStorage(cacheMethod,null);
                if(cacheStorageAdapter != null){
                    localCacheAdapterMap.putIfAbsent(cacheMethod,cacheStorageAdapter);
                }
                return cacheStorageAdapter;
            }
        }
        if (cacheAdapterMap != null) {
            Map<String, CacheStorageAdapter> cacheStorageAdapterMap = cacheAdapterMap.get(cacheMethod);
            if (cacheStorageAdapterMap != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageAdapterMap.get(host);
                if (cacheStorageAdapter == null) { //创建cacheStorageAdapter
                    cacheStorageAdapter = createCacheStorage(cacheMethod, host);
                    cacheStorageAdapterMap.putIfAbsent(host, cacheStorageAdapter);
                    return cacheStorageAdapter;
                } else {
                    return cacheStorageAdapter;
                }
            } else {
                cacheStorageAdapterMap = new ConcurrentHashMap<>();
                CacheStorageAdapter cacheStorageAdapter = createCacheStorage(cacheMethod, host);
                if (cacheStorageAdapter != null) {
                    cacheStorageAdapterMap.putIfAbsent(host, cacheStorageAdapter);
                }
                cacheAdapterMap.putIfAbsent(cacheMethod, cacheStorageAdapterMap);
                return cacheStorageAdapter;
            }
        } else {
            cacheAdapterMap = new ConcurrentHashMap<>();
            Map<String, CacheStorageAdapter> cacheStorageAdapterMap = new ConcurrentHashMap<>();
            cacheAdapterMap.putIfAbsent(cacheMethod, cacheStorageAdapterMap);
            CacheStorageAdapter cacheStorageAdapter = createCacheStorage(cacheMethod, host);
            cacheStorageAdapterMap.putIfAbsent(host, cacheStorageAdapter);
            return cacheStorageAdapter;
        }
    }
    public void removeCacheStorageAdapter(String cacheMethod, String host) {
        if (StringUtils.isBlank(cacheMethod)) {
            cacheMethod = CacheStorageAdapter.DEFAULT_CACHE_METHOD;
        }
        if (host != null) {
            Map<String, CacheStorageAdapter> cacheStorageAdapterMap = cacheAdapterMap.get(cacheMethod);
            if (cacheStorageAdapterMap != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageAdapterMap.get(host);
                if (cacheStorageAdapter != null) {
                    if(StringUtils.equals(cacheMethod,CacheStorageMethod.METHOD_REDIS)){
                        RedisCacheStorageHandler redisCacheStorageHandler = (RedisCacheStorageHandler)cacheStorageAdapter;
                        boolean result =  cacheStorageAdapterMap.remove(host, cacheStorageAdapter);
                        if (result) {
                            redisCacheStorageHandler.disconnect();
                        }
                    }
                }
            }
        }
    }

    private CacheStorageAdapter createCacheStorage(String cacheMethod, String host) {
        try {
            cacheMethod = cacheMethod + CLASS_EXTENSION;
            Class<?> clazz = Class.forName("com.docker.storage.cache.handlers." + cacheMethod);
            return createCacheStorage(clazz, host);
        } catch (ClassNotFoundException e) {
            LoggerEx.error(TAG, "No such class, class name is com.docker.storage.cache.handlers." + cacheMethod);
        }
        return null;
    }

    private CacheStorageAdapter createCacheStorage(Class<?> clazz, String host) {
        Constructor constructor = null;
        try {
            if(host != null){
                constructor = clazz.getConstructor(String.class);
                return (CacheStorageAdapter) constructor.newInstance(host);
            }else{
                constructor = clazz.getConstructor();
                return (CacheStorageAdapter) constructor.newInstance();
            }
        } catch (NoSuchMethodException e1) {
            LoggerEx.error(TAG, "No such method by class " + clazz.getSimpleName());
        } catch (Throwable throwable) {
            LoggerEx.error(TAG, "Get cache storage handler failed,reason is " + throwable.getMessage());
        }

        return null;
    }

    public static CacheStorageFactory getInstance() {
        if(instance == null){
            synchronized (CacheStorageFactory.class){
                if(instance == null){
                    instance = new CacheStorageFactory();
                }
            }
        }
        return instance;
    }
}
