package com.docker.storage.ehcache;


import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ehcache.Cache;

import java.util.Set;

public class EhCacheHandler {
    public static final String TAG = EhCacheHandler.class.getSimpleName();
    private EhCacheManager ehCacheManager;

    public <T> T put(String prefix, String key, T value, Long expired) throws CoreException {
        if (prefix == null || key == null || value == null) {
            throw new CoreException(ChatErrorCodes.ERROR_CACHE_ILLEGAL_PARAMETER, "Illegal param");
        }
        Cache cache = ehCacheManager.getEhCacheObj(expired, prefix);
        if (cache != null) {
//            value = JSONObject.toJSONString(value);
            try {
                cache.put(key, value);
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "Cache put failed,reason is " + ExceptionUtils.getFullStackTrace(throwable));
                throw new CoreException(ChatErrorCodes.ERROR_CACHE_PUT, "Cache put failed");
            }
        }
        return value;
    }

    public <T> T get(String prefix, String key) throws CoreException {
        if (prefix == null || key == null) {
            throw new CoreException(ChatErrorCodes.ERROR_CACHE_ILLEGAL_PARAMETER, "Illegal param");
        }
        Cache cache = ehCacheManager.getEhCacheObj(null, prefix);
        if (cache != null) {
            try {
                Object value = cache.get(key);
                if (value != null) {
                    return (T) value;
//                return JSONObject.parseObject((String) value, clazz);
                }
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "Cache get failed,reason is " + ExceptionUtils.getFullStackTrace(throwable));
                throw new CoreException(ChatErrorCodes.ERROR_CACHE_GET, "Cache get failed");
            }
        }
        return null;
    }

//    public <T> T get(String tableName, String key, Type type) throws CoreException {
//        Cache cache = ehCacheManager.getEhCacheObj(tableName);
//        if (cache != null) {
//            Object value = cache.get(key);
//            if (value != null) {
//                return (T)value;
////                return JSONObject.parseObject((String) value, type);
//            }
//        }
//        return null;
//    }


    public void remove(String prefix, String key) throws CoreException {
        if (prefix == null || key == null) {
            throw new CoreException(ChatErrorCodes.ERROR_CACHE_ILLEGAL_PARAMETER, "Illegal param");
        }
        Cache cache = ehCacheManager.getEhCacheObj(null, prefix);
        if (cache != null) {
            cache.remove(key);
        }
    }

//    public void putAll(String tableName, Map<String, Object> values, Long expired) throws CoreException {
//        Cache cache = ehCacheManager.getEhCacheObj(expired, tableName);
//        if (cache != null) {
//            cache.putAll(values);
//        }
//    }

    public void removeAll(String prefix, Set<String> keys) throws CoreException {
        if (prefix == null || keys == null || keys.isEmpty()) {
            throw new CoreException(ChatErrorCodes.ERROR_CACHE_ILLEGAL_PARAMETER, "Illegal param");
        }
        Cache cache = ehCacheManager.getEhCacheObj(null, prefix);
        if (cache != null) {
            cache.removeAll(keys);
        }
    }

    public <T> T putIfAbsent(String prefix, String key, T value, Long expired) throws CoreException {
        if (prefix == null || key == null || value == null) {
            throw new CoreException(ChatErrorCodes.ERROR_CACHE_ILLEGAL_PARAMETER, "Illegal param");
        }
        Cache cache = ehCacheManager.getEhCacheObj(expired, prefix);
        if (cache != null) {
            return (T) cache.putIfAbsent(key, value);
        }
        return null;
    }

    public EhCacheManager getEhCacheManager() {
        return ehCacheManager;
    }

    public void setEhCacheManager(EhCacheManager ehCacheManager) {
        this.ehCacheManager = ehCacheManager;
    }
}
