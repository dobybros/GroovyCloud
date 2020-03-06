package com.docker.storage.redis;

import chat.errors.CoreException;
import com.docker.script.BaseRuntime;
import com.docker.storage.redis.annotation.RedisListener;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * Created by lick on 2020/3/5.
 * Description：
 */
public class RedisListenerHandler extends ClassAnnotationHandler {
    @Override
    public void handlerShutdown() {

    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return RedisListener.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                       MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null) {
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);
                if (groovyClass != null) {
                    RedisListener redisListenerAnnotation = groovyClass.getAnnotation(RedisListener.class);
                    if (redisListenerAnnotation != null) {
                        GroovyObjectEx<com.docker.storage.redis.MyRedisListener> redisListenerObj = ((GroovyBeanFactory) classLoader.getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        if (redisListenerObj != null) {
                            try {
                                redisListenerObj.getObject().redisHandler = ((BaseRuntime)classLoader.getGroovyRuntime()).getRedisHandler();
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

    }
}
