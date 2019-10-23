package com.docker.storage.cache.handlers;

import chat.errors.CoreException;
import com.docker.storage.redis.RedisHandler;

import java.lang.reflect.Type;

public class RedisCacheStorageHandler extends CacheStorageAdapter {

    public static RedisCacheStorageHandler instance;
    private RedisHandler redisHandler;

    public RedisCacheStorageHandler(String host){
        this.redisHandler = new RedisHandler(host);
        redisHandler.connect();
    }

    @Override
    public Object addCacheData(String key, Object value, Long expired) throws CoreException {
        if (key != null && value != null)
            return redisHandler.setObject(key, value, RedisHandler.NXXX, RedisHandler.EXPX, expired);
        return null;
    }

    @Override
    public void deleteCacheData(String key) throws CoreException {
        if (key != null)
            redisHandler.expire(key, 0L);
    }

    @Override
    public <T> T getCacheData(String key, Class<T> clazz) throws CoreException {
        if (key != null && clazz != null) {
            return redisHandler.getObject(key, clazz);
        }
        return null;
    }

    @Override
    public <T> T getCacheData(String key, Type type) throws CoreException {
        if(key != null && type != null){
            return redisHandler.getObject(key, type);
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
