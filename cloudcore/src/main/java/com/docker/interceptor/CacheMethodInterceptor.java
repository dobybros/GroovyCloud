package com.docker.interceptor;

import chat.errors.CoreException;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.rpc.remote.stub.ServerCacheManager;
import com.docker.storage.cache.CacheStorageAdapter;
import com.docker.storage.cache.CacheStorageFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CacheMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CacheMethodInterceptor.class.getSimpleName();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws CoreException {
        RPCMethodInvocation rpcMethodInvocation = (RPCMethodInvocation) methodInvocation;
        String methodKey = rpcMethodInvocation.methodKey;
        ServerCacheManager serverCacheManager = ServerCacheManager.getInstance();
        Map<String, CacheObj> cacheMethodMap = serverCacheManager.getCacheMethodMap();
        CacheStorageFactory cacheStorageFactory = serverCacheManager.getCacheStorageFactory();
        if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
            CacheObj cacheObj = cacheMethodMap.get(methodKey);
            if (cacheObj != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
                if (cacheStorageAdapter == null && cacheObj.isEmpty()) {
                    return rpcMethodInvocation.proceed();
                }
                Object key = ReflectionUtil.parseSpel(cacheObj.getParamNames(), rpcMethodInvocation.arguments, cacheObj.getSpelKey());
                if (key == null) {
                    return rpcMethodInvocation.proceed();
                } else {
                    cacheObj.setKey((String) key);
                    Object result = cacheStorageAdapter.getCacheData(cacheObj);
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
                                cacheObj.setValue(result);
                                cacheStorageAdapter.addCacheData(cacheObj);
                            }
                        }else{
                            result = rpcMethodInvocation.handleAsync();
                        }

                        return result;
                    }
                }
            }
        }
        return rpcMethodInvocation.proceed();
    }

}
