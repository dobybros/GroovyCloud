package com.docker.storage.cache;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.AsyncCallbackHandler;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.script.BaseRuntime;
import com.docker.storage.cache.handlers.CacheStorageAdapter;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CacheMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CacheMethodInterceptor.class.getSimpleName();
    private CacheStorageFactory cacheStorageFactory = CacheStorageFactory.getInstance();
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
                CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod(), baseRuntime.getCacheHost(cacheObj.getCacheMethod()));
                if (cacheStorageAdapter == null || cacheObj.isEmpty()) {
                    return rpcMethodInvocation.proceed();
                }
                Object key = ReflectionUtil.parseSpel(cacheObj.getParamNames(), rpcMethodInvocation.arguments, cacheObj.getSpelKey());
                if (key == null) {
                    return rpcMethodInvocation.proceed();
                } else {
                    Object result = cacheStorageAdapter.getCacheData(cacheObj.getPrefix() + "_" + key, rpcMethodInvocation.getMethodMapping().getReturnClass());
                    if (result != null) {
                        if (rpcMethodInvocation.getAsync()) {
                            CompletableFuture completableFuture = new CompletableFuture();
                            completableFuture.complete(result);
                            return completableFuture;
                        } else {
                            return result;
                        }
                    } else {
                        if (!rpcMethodInvocation.getAsync()) {
                            result = rpcMethodInvocation.handleSync();
                            if (result != null) {
                                try {
                                    cacheStorageAdapter.addCacheData(cacheObj.getPrefix() + "_" + key, result, cacheObj.getExpired());
                                } catch (CoreException e) {
                                    LoggerEx.error(TAG, "Add cache data failed,key is " + cacheObj.getPrefix() + "_" + key + ",value is " + result + ",reason is " + e.getMessage());
                                }
                            }
                        } else {
                            rpcMethodInvocation.getMethodRequest().putExtra(CACHEKEY, key);
                            result = rpcMethodInvocation.handleAsync();
                        }
                        return result;
                    }
                }
            }
        }
        return rpcMethodInvocation.proceed();
    }


    public static class CacheAsyncCallbackHandler extends AsyncCallbackHandler {
        @Override
        public void handle() {

        }
    }

    public void setCacheAnnotationHandler(CacheAnnotationHandler cacheAnnotationHandler){
        this.cacheAnnotationHandler = cacheAnnotationHandler;
        this.cacheMethodMap = cacheAnnotationHandler.getCacheMethodMap();

    }

    public Map<String, CacheObj> getCacheMethodMap() {
        return cacheMethodMap;
    }
}
