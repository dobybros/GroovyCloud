package com.docker.interceptor;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.storage.cache.CacheAnnotationHandler;
import com.docker.storage.cache.CacheStorageAdapter;
import com.docker.storage.cache.CacheStorageFactory;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.NullObject;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.MethodInterceptor;

import java.util.concurrent.ConcurrentHashMap;

public class CacheMethodInterceptor implements MethodInterceptor {
    public static final String TAG = CacheMethodInterceptor.class.getSimpleName();
    private ConcurrentHashMap<String, CacheObj> cacheMethodMap;
    private CacheStorageFactory cacheStorageFactory;
    private CacheAnnotationHandler cacheAnnotationHandler;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws CoreException {
        String methodKey = ReflectionUtil.getMethodKey(methodInvocation.target.getClass(), methodInvocation.method);
        if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
            CacheObj cacheObj = cacheMethodMap.get(methodKey);
            if (cacheObj != null) {
                CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
                if (cacheStorageAdapter == null) {
                    return NullObject.getNullObject();
                }
                CacheObj.CacheKeyObj cacheKeyObj = cacheObj.getCacheKeyObj();
                //获取指定key的值
                Object key = getCacheKey(cacheKeyObj, methodInvocation.arguments);
                if (key == null) {
                    return methodInvocation.proceed();
                } else {
                    cacheKeyObj.setKey((String) key);
                    Object result = cacheStorageAdapter.getCacheData(cacheObj);
                    if (result != null) {
                        return result;
                    } else {
                        result = methodInvocation.invokeMethod();
                        if (result != null) {
                            cacheObj.setValue(result);
                            cacheStorageAdapter.addCacheData(cacheObj);
                        }
                        return result;
                    }
                }
            }
        }
        return methodInvocation.proceed();
    }

    public Object getCacheKey(CacheObj.CacheKeyObj cacheKeyObj, Object... params) {
        if (cacheKeyObj != null) {
            if (StringUtils.isNotBlank(cacheKeyObj.getKey())) {
                return cacheKeyObj.getKey();
            }
            if (cacheKeyObj.getMethod() != null) {
                if (cacheKeyObj.getIndex() != 0) {
                    Object obj = params[cacheKeyObj.getIndex()];
                    try {
                        return ReflectionUtil.getFieldValueByGetter(obj, cacheKeyObj.getMethod());
                    } catch (Exception e) {
                        LoggerEx.error(TAG, "Get cache key failed, reason is " + e.getMessage());
                    }
                }
            } else {
                if (cacheKeyObj.getIndex() != 0) {
                    return params[cacheKeyObj.getIndex()];
                }
            }
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


    //    public ConcurrentHashMap<String, CacheObj> getCacheMethodMap() {
//        return cacheMethodMap;
//    }
//
//    public void setCacheMethodMap(ConcurrentHashMap<String, CacheObj> cacheMethodMap) {
//        this.cacheMethodMap = cacheMethodMap;
//    }
//
//    public CacheStorageFactory getCacheStorageFactory() {
//        return cacheStorageFactory;
//    }
//
//    public void setCacheStorageFactory(CacheStorageFactory cacheStorageFactory) {
//        this.cacheStorageFactory = cacheStorageFactory;
//    }
}
