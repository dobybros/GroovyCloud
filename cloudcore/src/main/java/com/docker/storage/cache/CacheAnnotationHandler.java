package com.docker.storage.cache;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.annotations.Cache;
import com.docker.annotations.CacheClass;
import com.docker.annotations.CacheKey;
import com.docker.annotations.ClearCache;
import com.docker.data.CacheObj;
import com.docker.interceptor.CacheMethodInterceptor;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.NullObject;
import script.groovy.object.GroovyObjectEx;
import script.groovy.object.MethodInvocation;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.MethodInterceptor;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheAnnotationHandler extends ClassAnnotationHandler {
    public static final String TAG = CacheAnnotationHandler.class.getSimpleName();
    public ConcurrentHashMap<String, CacheObj> cacheMethodMap = new ConcurrentHashMap<>();
    private CacheMethodInterceptor cacheMethodInterceptor = new CacheMethodInterceptor();
//    private ClearCacheMethodInterceptor clearCacheMethodInterceptor = new ClearCacheMethodInterceptor();
    public CacheStorageFactory cacheStorageFactory;

    public CacheAnnotationHandler(CacheStorageFactory cacheStorageFactory) {
        this.cacheStorageFactory = cacheStorageFactory;
        this.cacheMethodInterceptor.setCacheAnnotationHandler(this);
    }

//    public class ClearCacheMethodInterceptor implements MethodInterceptor {
//        @Override
//        public Object invoke(GroovyObjectEx groovyObjectEx, String methodName, Object[] params) throws CoreException {
//            String methodKey = ReflectionUtil.getMethodKey(groovyObjectEx.getGroovyClass(), methodName);
//            if (cacheMethodMap != null && !cacheMethodMap.isEmpty()) {
//                CacheObj cacheObj = cacheMethodMap.get(methodKey);
//                if (cacheObj != null) {
//                    CacheStorageAdapter cacheStorageAdapter = cacheStorageFactory.getCacheStorageAdapter(cacheObj.getCacheMethod());
//                    if (cacheStorageAdapter == null) {
//                        return NullObject.getNullObject();
//                    }
//                    CacheObj.CacheKeyObj cacheKeyObj = cacheObj.getCacheKeyObj();
//                    //获取指定key的值
//                    Object key = getCacheKey(cacheKeyObj, params);
//                    if (key == null) {
//                        return NullObject.getNullObject();
//                    } else {
//                        cacheKeyObj.setKey((String) key);
//                        //根据key获取缓存的值
//                        Object result = cacheStorageAdapter.getCacheData(cacheObj);
//                        if (result != null) {
//                            //TODO 清除缓存
//                            return result;
//                        }
//                    }
//                }
//            }
//            return NullObject.getNullObject();
//        }
//    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return CacheClass.class;
    }

    @Override
    public void handlerShutdown() {
        cacheMethodMap.clear();
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);

                if (groovyClass != null) {
                    CacheClass cacheClass = groovyClass.getAnnotation(CacheClass.class);
                    if (cacheClass != null) {
                        GroovyObjectEx cacheGroovyObj = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        scanClass(groovyClass, cacheGroovyObj);
                    }
                }
            }
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }


    private void scanClass(Class<?> clazz, GroovyObjectEx cacheGroovyObj) {
        if (clazz == null)
            return;
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if (methods != null) {
            for (Method method : methods) {
                handleCacheAnnotation(clazz, method, cacheGroovyObj);
//                handleClearCacheAnnotation(clazz, method, cacheGroovyObj);
            }
        }
    }




    private void handleCacheAnnotation(Class<?> clazz, Method method, GroovyObjectEx cacheGroovyObj) {
        Cache cache = method.getAnnotation(Cache.class);
        if (cache != null) {
            String methodKey = ReflectionUtil.getMethodKey(clazz, method.getName());
            CacheObj cacheObj = new CacheObj();
            CacheObj.CacheKeyObj cacheKeyObj = cacheObj.new CacheKeyObj();
            Long expired = cache.expired();
            cacheObj.setExpired(expired);
            cacheObj.setCacheMethod(cache.cacheMethod());
            if (StringUtils.isNotBlank(cache.key())) {
                cacheKeyObj.setKey(cache.key());
            }
            if (StringUtils.isNotBlank(cache.prefix())) {
                cacheObj.setPrefix(cache.prefix());
            }
            if (method.getParameterAnnotations() != null && method.getParameterAnnotations().length > 0) {
                generationCacheKeyObj(method, cacheKeyObj);
            }
            cacheObj.setCacheKeyObj(cacheKeyObj);
            cacheMethodMap.put(methodKey, cacheObj);
            cacheGroovyObj.addMethodInterceptors(methodKey, cacheMethodInterceptor);
            LoggerEx.info("SCAN", "Mapping cache method key " + methodKey + " for class " + clazz.getName() + " method " + method.getName());
        }
    }

    private void generationCacheKeyObj(Method method, CacheObj.CacheKeyObj cacheKeyObj) {
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                CacheKey cacheKey = parameter.getAnnotation(CacheKey.class);
                Class<?> clazz = parameter.getType();
                if (cacheKey != null) {
                    cacheKeyObj.setIndex(i);
                    if (StringUtils.isNotBlank(cacheKey.field())) {
                        try {
                            Method method1 = ReflectionUtil.getMethodByField(clazz, cacheKey.field());
                            if (method != null) {
//                            cacheKeyObj.setField(cacheKey.field());
                                cacheKeyObj.setMethod(method1);

                            }
                        } catch (Throwable throwable) {
                            LoggerEx.error(TAG, "Generation method failed, class is " + clazz.getSimpleName() + "field is " + cacheKey.field());
                        }
                    }
                }
            }
        }
    }


//    private void handleClearCacheAnnotation(Class<?> clazz, Method method, GroovyObjectEx cacheGroovyObj) {
//        ClearCache clearCache = method.getAnnotation(ClearCache.class);
//        if (clearCache != null) {
//            String methodKey = ReflectionUtil.getMethodKey(clazz, method.getName());
//            CacheObj cacheObj = new CacheObj();
//            CacheObj.CacheKeyObj cacheKeyObj = cacheObj.new CacheKeyObj();
//            if (StringUtils.isNotBlank(clearCache.key())) {
//                cacheKeyObj.setKey(clearCache.key());
//            }
//            if (method.getParameterAnnotations() != null && method.getParameterAnnotations().length > 0) {
//                generationCacheKeyObj(method, cacheKeyObj);
//            }
//            cacheMethodMap.put(methodKey, cacheObj);
//            cacheGroovyObj.addMethodInterceptors(methodKey, clearCacheMethodInterceptor);
//            LoggerEx.info("SCAN", "Mapping clear cache method key " + methodKey + " for class " + clazz.getName() + " method " + method.getName());
//        }
//    }


    public void setCacheStorageFactory(CacheStorageFactory cacheStorageFactory) {
        this.cacheStorageFactory = cacheStorageFactory;
    }


}
