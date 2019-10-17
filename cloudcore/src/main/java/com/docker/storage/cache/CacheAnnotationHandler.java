package com.docker.storage.cache;

import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.annotations.CacheClass;
import com.docker.annotations.CacheEvict;
import com.docker.annotations.CachePut;
import com.docker.data.CacheObj;
import com.docker.rpc.remote.stub.RPCInterceptorFactory;
import com.docker.script.BaseRuntime;
import org.apache.commons.lang.StringUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheAnnotationHandler extends ClassAnnotationHandler {
    public static final String TAG = CacheAnnotationHandler.class.getSimpleName();
    private CachePutMethodInterceptor cachePutMethodInterceptor = new CachePutMethodInterceptor();
    private CacheEvictMethodInterceptor cacheEvictMethodInterceptor = new CacheEvictMethodInterceptor();
    protected Map<String, CacheObj> cacheMethodMap = new ConcurrentHashMap<>();

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return CacheClass.class;
    }

    @Override
    public void handlerShutdown() {

    }

    public CacheAnnotationHandler() {
        this.cachePutMethodInterceptor.setCacheAnnotationHandler(this);
        this.cacheEvictMethodInterceptor.setCacheAnnotationHandler(this);
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
        String serviceName = "";
        try {
            Field field = clazz.getField("SERVICE");
            serviceName = (String) field.get(clazz);
        } catch (Exception e) {
            LoggerEx.error(TAG, "Get field failed");
            return;
        }
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if (methods != null) {
            for (Method method : methods) {
                handleCachePutAnnotation(serviceName, clazz, method);
                handleCacheEvictAnnotation(serviceName, clazz, method);
            }
        }
    }

    private void handleCacheEvictAnnotation(String serviceName, Class<?> clazz, Method method) {
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        if (cacheEvict != null){
            BaseRuntime baseRuntime = (BaseRuntime) getGroovyRuntime();
            String methodKey = String.valueOf(ReflectionUtil.getCrc(clazz, method.getName(), serviceName));
            CacheObj cacheObj = new CacheObj();
            cacheObj.setCacheMethod(cacheEvict.cacheMethod());
            if (StringUtils.isNotBlank(cacheEvict.key())) {
                cacheObj.setSpelKey(cacheEvict.key());
            }
            if (StringUtils.isNotBlank(cacheEvict.prefix())) {
                cacheObj.setPrefix(cacheEvict.prefix());
            }
            cacheObj.setParamNames(ReflectionUtil.getParamNames(method));
            cacheObj.setMethod(method);
            cacheMethodMap.put(methodKey, cacheObj);
            RPCInterceptorFactory.getInstance().addMethodInterceptor(baseRuntime.getServiceName() + "_v" + baseRuntime.getServiceVersion(), methodKey, cacheEvictMethodInterceptor);
            LoggerEx.info("SCAN", "Mapping cacheEvict method key " + methodKey + " for class " + clazz.getName() + " method " + method.getName());
        }

    }


    private void handleCachePutAnnotation(String serviceName, Class<?> clazz, Method method) {
        CachePut cachePut = method.getAnnotation(CachePut.class);
        if (cachePut != null) {
            BaseRuntime baseRuntime = (BaseRuntime) getGroovyRuntime();
            String methodKey = String.valueOf(ReflectionUtil.getCrc(clazz, method.getName(), serviceName));
            CacheObj cacheObj = new CacheObj();
            Long expired = cachePut.expired();
            cacheObj.setExpired(expired);
            cacheObj.setCacheMethod(cachePut.cacheMethod());
            if (StringUtils.isNotBlank(cachePut.key())) {
                cacheObj.setSpelKey(cachePut.key());
            }
            if (StringUtils.isNotBlank(cachePut.prefix())) {
                cacheObj.setPrefix(cachePut.prefix());
            }
            cacheObj.setParamNames(ReflectionUtil.getParamNames(method));
            cacheObj.setMethod(method);
            cacheMethodMap.put(methodKey, cacheObj);
            RPCInterceptorFactory.getInstance().addMethodInterceptor(baseRuntime.getServiceName() + "_v" + baseRuntime.getServiceVersion(), methodKey, cachePutMethodInterceptor);
            LoggerEx.info("SCAN", "Mapping cachePut method key " + methodKey + " for class " + clazz.getName() + " method " + method.getName());
        }
    }

    public Map<String, CacheObj> getCacheMethodMap() {
        return cacheMethodMap;
    }

    public void setCacheMethodMap(Map<String, CacheObj> cacheMethodMap) {
        this.cacheMethodMap = cacheMethodMap;
    }
}
