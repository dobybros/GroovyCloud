package com.docker.storage.cache;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存路由 缓存由CacheRouter分发调用
 */
public class CacheStorageFactory {

    public static final String CLASS_EXTENSION = "CacheStorageHandler";

    private Map<String, CacheStorageAdapter> cacheAdapterMap;

    public CacheStorageAdapter getCacheStorageAdapter(String cacheMethod){
        if(cacheAdapterMap == null || cacheAdapterMap.size() == 0){
            return null;
        }
        if(StringUtils.isBlank(cacheMethod)){
            cacheMethod = CacheStorageAdapter.DEFAULT_CACHE_METHOD;
        }
        cacheMethod = cacheMethod + CLASS_EXTENSION;
        return cacheAdapterMap.get(cacheMethod);
    }

    public void registerCacheStorageAdapter(CacheStorageAdapter cacheStorageAdapter){
        if(cacheStorageAdapter != null){
            String className = cacheStorageAdapter.getClass().getSimpleName();
            if(cacheAdapterMap == null){
                cacheAdapterMap = new HashMap<>();
            }
            cacheAdapterMap.putIfAbsent(className, cacheStorageAdapter);
        }
    }


}
