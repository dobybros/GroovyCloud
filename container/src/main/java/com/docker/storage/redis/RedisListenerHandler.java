package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.script.BaseRuntime;
import com.docker.storage.redis.annotation.RedisListener;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationGlobalHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lick on 2020/3/5.
 * Description：
 */
public class RedisListenerHandler extends ClassAnnotationGlobalHandler {
    private final String TAG = RedisListenerHandler.class.getSimpleName();
    private List<GroovyObjectEx<com.docker.storage.redis.MyRedisListener>> groovyObjectExes = new CopyOnWriteArrayList<>();

    @Override
    public void handlerShutdown() {
    }

    @Override
    public void handleAnnotatedClassesInjectBean(GroovyRuntime groovyRuntime) {
        for (GroovyObjectEx<com.docker.storage.redis.MyRedisListener> groovyObjectEx : groovyObjectExes) {
            try {
                groovyObjectEx = ((GroovyBeanFactory) groovyRuntime.getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyObjectEx.getGroovyClass());
            }catch (CoreException e){
                LoggerEx.error(TAG, e.getMessage());
            }
        }
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return RedisListener.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, GroovyRuntime groovyRuntime) {
        if (annotatedClassMap != null) {
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);
                if (groovyClass != null) {
                    RedisListener redisListenerAnnotation = groovyClass.getAnnotation(RedisListener.class);
                    if (redisListenerAnnotation != null) {
                        GroovyObjectEx<com.docker.storage.redis.MyRedisListener> redisListenerObj = ((GroovyBeanFactory) groovyRuntime.getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        if (redisListenerObj != null) {
                            groovyObjectExes.add(redisListenerObj);
                            try {
                                redisListenerObj.getObject().redisHandler = ((BaseRuntime) groovyRuntime).getRedisHandler();
                            } catch (CoreException e) {
                                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
                            }
                        }
                    }
                }
            }
        }
    }


    public void setRedisHandler() {
        for (GroovyObjectEx<com.docker.storage.redis.MyRedisListener> groovyObjectEx : groovyObjectExes) {
            try {
                groovyObjectEx.getObject().redisHandler = ((BaseRuntime)groovyObjectEx.getGroovyRuntime()).getRedisHandler();
            } catch (CoreException e) {
                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
            }
        }
    }
}
