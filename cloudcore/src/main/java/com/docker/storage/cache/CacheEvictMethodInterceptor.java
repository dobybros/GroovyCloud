package com.docker.storage.cache;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.script.BaseRuntime;
import com.docker.storage.cache.handlers.CacheStorageAdapter;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.Map;

public class CacheEvictMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CachePutMethodInterceptor.class.getSimpleName();
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
                if(key == null){
                    return rpcMethodInvocation.proceed();
                } else {
                    try{
                        cacheStorageAdapter.deleteCacheData(cacheObj.getPrefix() + "_" + key);
                    }catch (Throwable throwable){
                        LoggerEx.error(TAG, "Delete cache failed by key : " + cacheObj.getPrefix() + "_" + key);
                    }
                    return rpcMethodInvocation.proceed();
                }
            }
        }
        return rpcMethodInvocation.proceed();
    }

    public void setCacheAnnotationHandler(CacheAnnotationHandler cacheAnnotationHandler) {
        this.cacheAnnotationHandler = cacheAnnotationHandler;
        this.cacheMethodMap = cacheAnnotationHandler.getCacheMethodMap();

    }

}
