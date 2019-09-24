package com.docker.interceptor;

import chat.errors.CoreException;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.method.RPCMethodInvocation;
import com.docker.storage.cache.CacheAnnotationHandler;
import com.docker.storage.cache.CacheStorageAdapter;
import com.docker.storage.cache.CacheStorageFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.concurrent.ConcurrentHashMap;

public class CacheMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CacheMethodInterceptor.class.getSimpleName();
    private ConcurrentHashMap<String, CacheObj> cacheMethodMap;
    private CacheStorageFactory cacheStorageFactory;
    private CacheAnnotationHandler cacheAnnotationHandler;
    private ExpressionParser parser = new SpelExpressionParser();
    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws CoreException {
        RPCMethodInvocation rpcMethodInvocation = (RPCMethodInvocation) methodInvocation;
        String methodKey = ReflectionUtil.getMethodKey(rpcMethodInvocation.clazz, rpcMethodInvocation.method);
        if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
            CacheObj cacheObj = cacheMethodMap.get(methodKey);
            if (cacheObj != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
                if (cacheStorageAdapter == null && cacheObj.isEmpty()) {
                    return rpcMethodInvocation.proceed();
                }
                Object key = parseSpel(cacheObj.getParamNames(), rpcMethodInvocation.arguments, cacheObj.getSpelKey());
                if (key == null) {
                    return rpcMethodInvocation.proceed();
                } else {
                    cacheObj.setKey((String) key);
                    Object result = cacheStorageAdapter.getCacheData(cacheObj);
                    if (result != null) {
                        return result;
                    } else {
                        if(!rpcMethodInvocation.getAsync()){
                            result = rpcMethodInvocation.handleSync();
                        }
                        if (result != null) {
                            cacheObj.setValue(result);
                            cacheStorageAdapter.addCacheData(cacheObj);
                        }
                        return result;
                    }
                }
            }
        }
        return rpcMethodInvocation.proceed();
    }


    private Object parseSpel(String[] paramNames, Object[] arguments, String spel) {
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int len = 0; len < paramNames.length; len++) {
                context.setVariable(paramNames[len], arguments[len]);
            }
        }
        try {
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context);
        } catch (Exception e) {
//            return defaultResult;
        }
        return null;
    }

    public CacheAnnotationHandler getCacheAnnotationHandler() {
        return cacheAnnotationHandler;
    }

    public void setCacheAnnotationHandler(CacheAnnotationHandler cacheAnnotationHandler) {
        this.cacheAnnotationHandler = cacheAnnotationHandler;
        this.cacheMethodMap = cacheAnnotationHandler.cacheMethodMap;
        this.cacheStorageFactory = cacheAnnotationHandler.cacheStorageFactory;
    }
}
