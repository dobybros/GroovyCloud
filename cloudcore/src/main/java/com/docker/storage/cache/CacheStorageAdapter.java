package com.docker.storage.cache;

import chat.errors.CoreException;
import com.docker.data.CacheObj;
import script.groovy.runtime.GroovyRuntime;

public abstract class CacheStorageAdapter {

//    private GroovyRuntime groovyRuntime;

    public enum CACHEMETHOD{
        REDIS,MEMCACHE,EHCACHE
    }

    public static final String DEFAULT_CACHE_METHOD = CacheStorageMethod.METHOD_REDIS;

    public abstract Object addCacheData(CacheObj cacheObj) throws CoreException;

    public abstract void deleteCacheData(CacheObj cacheObj) throws CoreException;

    public abstract Object getCacheData(CacheObj cacheObj) throws CoreException;

//    public GroovyRuntime getGroovyRuntime() {
//        return groovyRuntime;
//    }
//
//    public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
//        this.groovyRuntime = groovyRuntime;
//    }

}
