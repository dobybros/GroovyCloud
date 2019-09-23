package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.remote.MethodMapping;
import com.docker.script.BaseRuntime;
import com.docker.storage.cache.CacheStorageAdapter;
import com.docker.storage.cache.CacheStorageFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import script.groovy.runtime.GroovyRuntime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RPCInvocationHandlerImpl extends InvacationHandler implements RPCInvocationHandler {
    private ExpressionParser parser = new SpelExpressionParser();

    protected RPCInvocationHandlerImpl(RemoteServerHandler remoteServerHandler) {
        super(remoteServerHandler);
    }

    @Override
    public Object handleSync(MethodMapping methodMapping, MethodRequest request) throws CoreException {
        BaseRuntime baseRuntime = (BaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(methodMapping.getMethod().getDeclaringClass().getClassLoader());
        ConcurrentHashMap<String, CacheObj> cacheMethodMap = baseRuntime.getCacheMethodMap();
        CacheStorageFactory cacheStorageFactory = baseRuntime.getCacheStorageFactory();
        String methodKey = ReflectionUtil.getMethodKey(methodMapping.getMethod().getDeclaringClass(), methodMapping.getMethod().getName());
        if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
            CacheObj cacheObj = cacheMethodMap.get(methodKey);
            if (cacheObj != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
                if (cacheStorageAdapter == null && cacheObj.isEmpty()) {
                    return super.handleSync(request);
                }
                Object key = parseSpel(cacheObj.getParamNames(), request.getArgs(), cacheObj.getSpelKey());
                if (key == null) {
                    return super.handleSync(request);
                } else {
                    cacheObj.setKey((String) key);
                    Object result = cacheStorageAdapter.getCacheData(cacheObj);
                    if (result != null) {
                        return result;
                    } else {
                        result = super.handleSync(request);
                        if (result != null) {
                            cacheObj.setValue(result);
                            cacheStorageAdapter.addCacheData(cacheObj);
                        }
                        return result;
                    }
                }
            }
        }
        return super.handleSync(request);
    }

    @Override
    public CompletableFuture<?> handleAsync(MethodMapping methodMapping, MethodRequest request) {
        return super.handleAsync(request);
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
}
