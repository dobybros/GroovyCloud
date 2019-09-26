package com.docker.storage.cache.handlers;

import chat.errors.CoreException;
import com.docker.storage.cache.CacheStorageMethod;

public abstract class CacheStorageAdapter {

    public enum CACHEMETHOD {
        REDIS, MEMCACHE, EHCACHE
    }

    public static final String DEFAULT_CACHE_METHOD = CacheStorageMethod.METHOD_REDIS;

    public abstract Object addCacheData(String key, Object value, Long expired) throws CoreException;

    public abstract void deleteCacheData(String key) throws CoreException;

    public abstract <T> T getCacheData(String key, Class<T> clazz) throws CoreException;

}
