package com.dobybros.chat.handlers.imextention;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.rpc.remote.stub.RemoteServers;
import com.docker.storage.cache.CacheStorageFactory;
import com.docker.storage.cache.CacheStorageMethod;
import com.docker.storage.cache.handlers.RedisCacheStorageHandler;
import com.docker.storage.redis.RedisHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/12/26.
 * Description：
 */
public class IMExtensionCache {
    private final String TAG = IMExtensionCache.class.getSimpleName();
    private final String SYMBOL = "@";
    public Map<String, String> newUserIdMap = new ConcurrentHashMap<>();
    private final String SESSION_PREFIX_USER = "IMEXTENSIONUSER";
    private final String SESSION_PREFIX_NEWUSER = "IMEXTENSIONNEWUSER";
    private RedisHandler redisHandler = null;
    private String host;

    public void init() {
        RedisCacheStorageHandler cacheStorageAdapter = (RedisCacheStorageHandler) CacheStorageFactory.getInstance().getCacheStorageAdapter(CacheStorageMethod.METHOD_REDIS, host);
        redisHandler = cacheStorageAdapter.getRedisHandler();
        TimerEx.schedule(new TimerTaskEx("IMExtensionCache init") {
            @Override
            public void execute() {
                if (!newUserIdMap.isEmpty()) {
                    try {
                        for (String key : newUserIdMap.keySet()) {
                            String newUserId = getStr(key);
                            if (newUserId != null) {
                                newUserIdMap.put(key, newUserId);
                            } else {
                                newUserIdMap.remove(key);
                            }
                        }
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "Refresh newUserId err, errMsg: " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        }, 10000L, 10000L);
    }

    public String saveNewUserIdReturnOld(String userId, String service, Integer terminal, String newUserId) throws CoreException {
        String key = getUserKey(userId, service, terminal);
        newUserIdMap.put(key, newUserId);
        return saveStrReturnOld(key, newUserId);
    }

    public Long saveNewUserIdNX(String userId, String service, Integer terminal, String newUserId) throws CoreException {
        String key = getUserKey(userId, service, terminal);
        Long result = saveStrNX(key, newUserId);
        if (result == 1) {
            newUserIdMap.put(key, newUserId);
        } else if (result == 0) {
            newUserIdMap.put(key, getStr(key));
        }
        return result;
    }

    public String getNewUserId(String userId, String service, Integer terminal) throws CoreException {
        String key = getUserKey(userId, service, terminal);
        String newUserId = newUserIdMap.get(key);
        if (newUserId == null) {
            newUserId = getStr(key);
        }
        return newUserId;
    }


    private String saveStrReturnOld(String key, String value) throws CoreException {
        return redisHandler.getSet(SESSION_PREFIX_USER + key, value);
    }

    public void delNewUserId(String newUserId, String service, Integer terminal) throws CoreException {
        String userId = getUserId(newUserId);
        if(userId != null){
            String key = getUserKey(userId, service, terminal);
            newUserIdMap.remove(key);
            delStr(key);
        }
    }

    private Long saveStrNX(String key, String value) throws CoreException {
        return redisHandler.setNX(SESSION_PREFIX_USER + key, value);
    }

    private String getStr(String key) throws CoreException {
        return redisHandler.get(SESSION_PREFIX_USER + key);
    }

    private Long delStr(String key) throws CoreException {
        return redisHandler.del(SESSION_PREFIX_USER + key);
    }

    public Long setUserServer(String newUserId, String service, RemoteServers.Server server) throws CoreException {
        String userId = getUserId(newUserId);
        if(userId != null){
            return redisHandler.hsetObject(SESSION_PREFIX_NEWUSER + getNewUserKey(userId, service), newUserId, server);
        }
        return null;
    }

    public Long delUserServer(String newUserId, String service) throws CoreException {
        if(newUserId != null && service != null){
            String userId = getUserId(newUserId);
            if (userId != null) {
                return redisHandler.hdel(SESSION_PREFIX_NEWUSER + getNewUserKey(userId, service), newUserId);
            }
        }
        return null;
    }

    public RemoteServers.Server getServer(String userId, String service, String newUserId) throws CoreException {
        return redisHandler.hgetObject(SESSION_PREFIX_NEWUSER + getNewUserKey(userId, service), newUserId, RemoteServers.Server.class);
    }

    public Map<String, RemoteServers.Server> getNewUsers(String userId, String service) throws CoreException {
        return redisHandler.hgetAllObject(SESSION_PREFIX_NEWUSER + getNewUserKey(userId, service), RemoteServers.Server.class);
    }

    private String getUserKey(String userId, String service, Integer terminal) {
        return userId + SYMBOL + service + SYMBOL + terminal.toString();
    }

    private String getNewUserKey(String userId, String service) {
        return userId + SYMBOL + service;
    }

    public String generateNewUserId(String userId, Short encodeVersion) {
        return SESSION_PREFIX_NEWUSER + SYMBOL + userId + SYMBOL + ObjectId.get().toString() + SYMBOL + encodeVersion.toString();
    }

    private Boolean isNewUserId(String userId) {
        return userId.startsWith(SESSION_PREFIX_NEWUSER + SYMBOL);
    }

    public String getUserId(String newUserId) {
        if (isNewUserId(newUserId)) {
            String[] strings = newUserId.split(SYMBOL);
            return strings[1];
        }
        return null;
    }

    public String getEncodeVersion(String newUserId) {
        if (isNewUserId(newUserId)) {
            String[] strings = newUserId.split(SYMBOL);
            return strings[3];
        }
        return null;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
