package com.docker.storage.cache;

import chat.errors.CoreException;
import com.docker.data.CacheObj;
import com.docker.storage.redis.RedisHandler;

public class RedisCacheStorageHandler extends CacheStorageAdapter {


    private RedisHandler redisHandler;

    @Override
    public Object addCacheData(CacheObj cacheObj) throws CoreException {
        if (cacheObj != null && !cacheObj.isEmpty()) {
           return redisHandler.setObject(cacheObj.getPrefix(), cacheObj.getKey(), cacheObj.getValue(), RedisHandler.NXXX, RedisHandler.EXPX, cacheObj.getExpired());
        }
        return null;
    }

    @Override
    public void deleteCacheData(CacheObj cacheObj) throws CoreException {

    }

    @Override
    public Object getCacheData(CacheObj cacheObj) throws CoreException {
        if (cacheObj != null && !cacheObj.isEmpty()) {
            if(cacheObj.getValue() != null){
                return redisHandler.getObject(cacheObj.getPrefix(), cacheObj.getKey(), cacheObj.getValue().getClass());
            }
        }
        return null;
    }

    public RedisHandler getRedisHandler() {
        return redisHandler;
    }

    public void setRedisHandler(RedisHandler redisHandler) {
        this.redisHandler = redisHandler;
    }
}
