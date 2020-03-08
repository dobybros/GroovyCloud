package com.docker.storage.cache.handlers;

import chat.errors.CoreException;
import com.docker.storage.redis.RedisHandler;

import java.lang.reflect.Type;

public class RedisCacheStorageHandler extends CacheStorageAdapter {

    public static RedisCacheStorageHandler instance;
    private RedisHandler redisHandler;

    public RedisCacheStorageHandler(String host) {
        this.redisHandler = new RedisHandler(host);
        redisHandler.connect();
    }

    public void disconnect() {
        if (redisHandler != null)
            redisHandler.disconnect();
    }

    @Override
    public Object addCacheData(String prefix, String key, Object value, Long expired) throws CoreException {
        if (prefix != null && key != null && value != null)
            return redisHandler.setObject(prefix, key, value, RedisHandler.NXXX, RedisHandler.EXPX, expired);
        return null;
    }

    @Override
    public void deleteCacheData(String prefix, String key) throws CoreException {
        if (prefix != null && key != null)
            redisHandler.expire(prefix, key, 0L);
    }

    @Override
    public <T> T getCacheData(String prefix, String key, Class<T> clazz) throws CoreException {
        if (prefix != null && key != null && clazz != null) {
            return redisHandler.getObject(prefix, key, clazz);
        }
        return null;
    }

    @Override
    public <T> T getCacheData(String prefix, String key, Type type) throws CoreException {
        if (prefix != null && key != null && type != null) {
            return redisHandler.getObject(prefix, key, type);
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
