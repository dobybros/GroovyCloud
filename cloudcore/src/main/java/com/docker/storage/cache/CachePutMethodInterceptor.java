package com.docker.storage.cache;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.async.AsyncCallbackHandler;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.script.BaseRuntime;
import com.docker.storage.cache.handlers.CacheStorageAdapter;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CachePutMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CachePutMethodInterceptor.class.getSimpleName();
    public static final String CACHE_METHODMAP = "cacheMethodMap";
    public static final String CRC = "crc";
    public static final String CACHE_KEY = "key";
    public static final String CACHE_HOST = "host";
    public static final String CACHE_EXPIRD = "expird";
    public static final String CACHE_PREFIX = "prefix";
    public static final String CACHEKEY = "cacheKey";
    private Map<String, CacheObj> cacheMethodMap;
    private CacheAnnotationHandler cacheAnnotationHandler;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws CoreException {
        RPCMethodInvocation rpcMethodInvocation = (RPCMethodInvocation) methodInvocation;

        String methodKey = rpcMethodInvocation.methodKey;
        //获取当前的baseRuntime
        BaseRuntime baseRuntime = (BaseRuntime) cacheAnnotationHandler.getGroovyRuntime();
        if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
            CacheObj cacheObj = cacheMethodMap.get(methodKey);
            if (cacheObj != null) {
                String cacheHost = baseRuntime.getCacheHost(cacheObj.getCacheMethod());
                CacheStorageAdapter cacheStorageAdapter = CacheStorageFactory.getInstance().getCacheStorageAdapter(cacheObj.getCacheMethod(), cacheHost);
                if (cacheStorageAdapter == null || cacheObj.isEmpty()) {
                    return rpcMethodInvocation.proceed();
                }
                Object key = ReflectionUtil.parseSpel(cacheObj.getParamNames(), rpcMethodInvocation.arguments, cacheObj.getSpelKey());
                if (key == null) {
                    return rpcMethodInvocation.proceed();
                } else {
                    MethodMapping methodMapping = rpcMethodInvocation.getMethodMapping();
                    Type returnType = null;
                    if (methodMapping.getGenericReturnClass().getTypeName().contains(CompletableFuture.class.getTypeName())) {
                        if (methodMapping.getGenericReturnActualTypeArguments() != null && methodMapping.getGenericReturnActualTypeArguments().length > 0) {
                            returnType = methodMapping.getGenericReturnActualTypeArguments()[0];
                        }
                    } else {
                        returnType = methodMapping.getGenericReturnClass();
                    }
                    if (returnType != null && !returnType.getTypeName().equals(void.class.getSimpleName())) {
                        Object result = cacheStorageAdapter.getCacheData(cacheObj.getPrefix(), (String) key, returnType);
                        if (result != null) {
                            if (rpcMethodInvocation.getAsync()) {
                                AsyncRpcFuture asyncRpcFuture = RpcCacheManager.getInstance().getAsyncRpcFuture(((RPCMethodInvocation) methodInvocation).getRemoteServerHandler().getCallbackFutureId());
                                if (asyncRpcFuture != null && asyncRpcFuture.getFuture() != null) {
                                    List<String> list = new ArrayList<>();
                                    list.add(CacheAsyncCallbackHandler.class.getSimpleName());
                                    asyncRpcFuture.handleAsyncHandler(result, list);
                                    CompletableFuture completableFuture = asyncRpcFuture.getFuture();
                                    completableFuture.complete(result);
                                    return completableFuture;
                                }
                            } else {
                                return result;
                            }
                        } else {
                            if (!rpcMethodInvocation.getAsync()) {
                                result = rpcMethodInvocation.handleSync();
                                if (result != null) {
                                    try {
                                        cacheStorageAdapter.addCacheData(cacheObj.getPrefix(), (String) key, result, cacheObj.getExpired());
                                    } catch (CoreException e) {
                                        LoggerEx.error(TAG, "Add cache data failed,key is " + cacheObj.getPrefix() + "_" + key + ",value is " + result + ",reason is " + e.getMessage());
                                    }
                                }
                            } else {
                                Map<String, Object> map = new HashMap<>();
                                map.put(CACHE_METHODMAP, cacheMethodMap);
                                map.put(CRC, String.valueOf(rpcMethodInvocation.getMethodRequest().getCrc()));
                                map.put(CACHE_HOST, cacheHost);
                                map.put(CACHE_KEY, key);
                                map.put(CACHE_PREFIX, cacheObj.getPrefix());
                                map.put(CACHE_EXPIRD, cacheObj.getExpired());
                                CacheAsyncCallbackHandler cacheAsyncCallbackHandler = new CacheAsyncCallbackHandler(map);

                                RpcCacheManager.getInstance().getAsyncRpcFuture(((RPCMethodInvocation) methodInvocation).getRemoteServerHandler().getCallbackFutureId()).addHandler(cacheAsyncCallbackHandler);
                                result = rpcMethodInvocation.handleAsync();
                            }
                            return result;
                        }
                    }
                }
            }
        }
        return rpcMethodInvocation.proceed();
    }


    private class CacheAsyncCallbackHandler extends AsyncCallbackHandler {

        public CacheAsyncCallbackHandler(Map map) {
            super(map);
        }

        @Override
        public void handle() {
            Map<String, CacheObj> cacheObjMap = (Map<String, CacheObj>) map.get(CACHE_METHODMAP);
            CacheObj cacheObj = cacheObjMap.get(map.get(CRC));
            CacheStorageAdapter cacheStorageAdapter = CacheStorageFactory.getInstance().getCacheStorageAdapter(cacheObj.getCacheMethod(), (String) map.get(CACHE_HOST));
            String key = (String) map.get(CACHE_KEY);
            String prefix = (String) map.get(CACHE_PREFIX);
            if (key != null && result != null && cacheStorageAdapter != null) {
                try {
                    cacheStorageAdapter.addCacheData(prefix, key, result, (Long) map.get(CACHE_EXPIRD));
                } catch (CoreException coreException) {
                    LoggerEx.error(TAG, "Add cache data failed on async call class is service_class_method:reason is " + coreException.getMessage());
                }
            }
        }
    }

    public void setCacheAnnotationHandler(CacheAnnotationHandler cacheAnnotationHandler) {
        this.cacheAnnotationHandler = cacheAnnotationHandler;
        this.cacheMethodMap = cacheAnnotationHandler.getCacheMethodMap();

    }

    public Map<String, CacheObj> getCacheMethodMap() {
        return cacheMethodMap;
    }
}
