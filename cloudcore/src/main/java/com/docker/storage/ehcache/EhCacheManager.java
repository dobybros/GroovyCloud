package com.docker.storage.ehcache;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.PropertiesContainer;
import com.docker.errors.CoreErrorCodes;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EhCacheManager {
    public static final String TAG = EhCacheManager.class.getSimpleName();

    protected CacheManager cacheManager;
    protected ResourcePools resourcePools;
    protected Map<String, Cache> ehCacheObjMap = new ConcurrentHashMap<>();
    protected PropertiesContainer propertiesContainer = PropertiesContainer.getInstance();

    public static void main(String[] args) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.init();
    }

    public void init() {
        try {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
            cacheManager.init();
            buildResourcePools();
        } catch (Throwable e) {
            LoggerEx.error(TAG, "Ehcache initialization failed, reason is " + ExceptionUtils.getFullStackTrace(e));
        } finally {
//            if (persistentCacheManager != null) {
//                persistentCacheManager.close();
//            }
        }
    }

    public void buildResourcePools() {
        //判断存储形式，以及存储大小
        //该属性代表是否支持堆内存储
//        Boolean ehcacheHeap = true;
        long ehcacheHeapStorageCapacity = 100l;
        if (propertiesContainer.getProperty("ehcache.heap.storage.capacity") != null) {
            ehcacheHeapStorageCapacity = Long.valueOf((String) propertiesContainer.getProperty("ehcache.heap.storage.capacity"));
        }
        ResourcePoolsBuilder resourcePoolsBuilder =
                ResourcePoolsBuilder.newResourcePoolsBuilder();
        resourcePoolsBuilder = resourcePoolsBuilder.heap(ehcacheHeapStorageCapacity, MemoryUnit.MB);

//        ResourcePoolsBuilder resourcePoolsBuilder =
//                ResourcePoolsBuilder.newResourcePoolsBuilder();
//        resourcePoolsBuilder = resourcePoolsBuilder.heap(100, MemoryUnit.MB);
        resourcePools = resourcePoolsBuilder.build();
    }

    public Cache getEhCacheObj(@Nullable Long expired, String tableName) throws CoreException{
        Cache cache = ehCacheObjMap.get(tableName);
        if (cache == null) {
            if (expired != null) {
                try {
                    CacheConfigurationBuilder cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class, resourcePools);
                    cacheConfigurationBuilder = cacheConfigurationBuilder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofMillis(expired)));
                    Long sizeOfMaxObjectGraph = 10000000l;
                    if (propertiesContainer.getProperty("max.object.graph.size") != null) {
                        sizeOfMaxObjectGraph = Long.valueOf((String) propertiesContainer.getProperty("max.object.graph.size"));
                    }
                    cacheConfigurationBuilder = cacheConfigurationBuilder.withSizeOfMaxObjectGraph(sizeOfMaxObjectGraph);
                    CacheConfiguration cacheConfiguration = cacheConfigurationBuilder.build();
                    cache = cacheManager.createCache(tableName, CacheConfigurationBuilder.newCacheConfigurationBuilder(cacheConfiguration));
                } catch (Throwable throwable) {
                    LoggerEx.error(TAG, "Create cache failed, reason is " + ExceptionUtils.getFullStackTrace(throwable));
                    throw new CoreException(ChatErrorCodes.ERROR_CREATE_CACHE, "Create cache failed");
                }
                if (cache != null)
                    ehCacheObjMap.put(tableName, cache);
            }
        }
        return cache;
    }

    public void setEhCacheObjMap(Map ehCacheObjMap) {
        this.ehCacheObjMap = ehCacheObjMap;
    }
}
