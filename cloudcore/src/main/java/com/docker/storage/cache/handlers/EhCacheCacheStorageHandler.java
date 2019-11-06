package com.docker.storage.cache.handlers;

import chat.errors.CoreException;
import com.docker.storage.ehcache.EhCacheHandler;
import com.docker.storage.ehcache.EhCacheManager;

import java.lang.reflect.Type;

public class EhCacheCacheStorageHandler extends CacheStorageAdapter{

    private EhCacheHandler ehCacheHandler;

    public EhCacheCacheStorageHandler(){
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.init();
        this.ehCacheHandler = new EhCacheHandler();
        this.ehCacheHandler.setEhCacheManager(ehCacheManager);
    }

    @Override
    public Object addCacheData(String prefix, String key, Object value, Long expired) throws CoreException {
        if (prefix != null && key != null && value != null){
          return ehCacheHandler.put(prefix, key, value, expired);
        }
        return null;
    }

    @Override
    public void deleteCacheData(String prefix, String key) throws CoreException {
        if (prefix != null && key != null)
            ehCacheHandler.remove(prefix, key);
    }

    @Override
    public <T> T getCacheData(String prefix, String key, Class<T> clazz) throws CoreException {
        if (prefix != null && key != null){
            return ehCacheHandler.get(prefix, key);
        }
        return null;
    }

    @Override
    public <T> T getCacheData(String prefix, String key, Type type) throws CoreException {
        if (prefix != null && key != null){
            return ehCacheHandler.get(prefix, key);
        }
        return null;
    }

    public EhCacheHandler getEhCacheHandler() {
        return ehCacheHandler;
    }
}
