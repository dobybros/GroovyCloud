package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ConcurrentHashSet;
import com.docker.script.BaseRuntime;
import com.docker.storage.redis.annotation.RedisSubscribe;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationGlobalHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2020/2/13.
 * Description：
 */
public class RedisSubscribeHandler extends ClassAnnotationGlobalHandler {
    private final String TAG = RedisSubscribeHandler.class.getSimpleName();
    private Map<String, Set<GroovyObjectEx>> redisSubscribeMap = new ConcurrentHashMap<>();

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return RedisSubscribe.class;
    }
    public void shutdown(){
        redisSubscribeMap.clear();
        MyRedisPubSubAdapter.getInstance().shutdown();
    }
    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, GroovyRuntime groovyRuntime) {
        if (annotatedClassMap != null) {
            Collection<Class<?>> values = annotatedClassMap.values();
            for (Class<?> groovyClass : values) {
                RedisSubscribe redisSubscribe = groovyClass.getAnnotation(RedisSubscribe.class);
                String[] keys = redisSubscribe.keys();
                if(keys.length > 0){
                    for (int i = 0; i < keys.length; i++) {
                        Set<GroovyObjectEx> groovyObjectExes = redisSubscribeMap.get(keys[i]);
                        if (groovyObjectExes == null) {
                            groovyObjectExes = new ConcurrentHashSet<>();
                            Set<GroovyObjectEx> groovyObjectExesOld = redisSubscribeMap.putIfAbsent(keys[i], groovyObjectExes);
                            if (groovyObjectExesOld != null) {
                                groovyObjectExes = groovyObjectExesOld;
                            }
                        }
                        GroovyObjectEx<?> groovyObj = ((GroovyBeanFactory) groovyRuntime.getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        if (!groovyObjectExes.contains(groovyObj)) {
                            for (GroovyObjectEx groovyObjectEx : groovyObjectExes){
                                if(groovyObjectEx.getGroovyPath().equals(groovyObj.getGroovyPath())){
                                    groovyObjectExes.remove(groovyObjectEx);
                                }
                            }
                            groovyObjectExes.add(groovyObj);
                            ((BaseRuntime)groovyRuntime).getRedisHandler().subscribe();
                        }
                    }
                }
            }
        }
    }

    public void redisCallback(String key) {
        if (key.contains("_")) {
            String[] reallyKeys = key.split("_");
            String reallyKey = reallyKeys[0];
            Set<GroovyObjectEx> groovyObjectExes = redisSubscribeMap.get(reallyKey);
            if(groovyObjectExes != null){
                for (GroovyObjectEx groovyObjectEx : groovyObjectExes){
                    try {
                        groovyObjectEx.invokeRootMethod("redisCallback", reallyKeys[1]);
                    } catch (CoreException e) {
                        LoggerEx.error(TAG, "Invoke method redisCallback err, groovyPath: " + groovyObjectEx.getGroovyPath() + ",errMsg: " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
        }

    }
}
