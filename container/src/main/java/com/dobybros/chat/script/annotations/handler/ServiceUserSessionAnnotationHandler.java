package com.dobybros.chat.script.annotations.handler;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.script.annotations.gateway.ServiceUserSessionHandler;
import com.dobybros.chat.script.annotations.gateway.ServiceUserSessionListener;
import com.docker.script.MyBaseRuntime;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceUserSessionAnnotationHandler extends ClassAnnotationHandler {

    public static final String TAG = ServiceUserSessionAnnotationHandler.class.getSimpleName();

    private Map<String, Class<?>> annotatedClassMap;

    private ConcurrentHashMap<String, ServiceUserSessionListener> listenerMap = new ConcurrentHashMap<>();

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return ServiceUserSessionHandler.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        this.setAnnotatedClassMap(annotatedClassMap);
    }

    @Override
    public void handlerShutdown() {
        super.handlerShutdown();
        for (String serverUser : listenerMap.keySet()) {
            ServiceUserSessionListener listener = listenerMap.get(serverUser);
            try {
                listener.closeSession();
            } catch (CoreException e) {
                LoggerEx.error(TAG, "Listener close session error when handlerShutdown, eMsg : " + e.getMessage());
            }
        }
    }

    public ServiceUserSessionListener createAnnotatedListener(String userId, String service) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            String key = getUserKey(userId, service);
            ServiceUserSessionListener listener = listenerMap.get(key);
            if (listener != null)
                return listener;
            for (Class<?> annotatedClass : annotatedClassMap.values()) {
                try {
                    Object obj = annotatedClass.newInstance();
                    if (obj instanceof ServiceUserSessionListener) {
                        listener = (ServiceUserSessionListener)obj;
                        listener.setUserId(userId);
                        listener.setService(service);
                        if (getGroovyRuntime() instanceof MyBaseRuntime) {
                            MyBaseRuntime myBaseRuntime = (MyBaseRuntime)getGroovyRuntime();
                            myBaseRuntime.injectBean(listener);
                        }
                    }
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "Create listener error, eMsg : " + t.getMessage());
                }
                if (listener != null)
                    break;
            }
            listenerMap.putIfAbsent(key, listener);
            return listenerMap.get(key);
        }
        return null;
    }

    public ServiceUserSessionListener getAnnotatedListener(String userId, String service) {
        if (listenerMap != null) {
            String key = getUserKey(userId, service);
            return listenerMap.get(key);
        }
        return null;
    }

    public void removeListeners(String userId, String service) {
        if (listenerMap != null)
            listenerMap.remove(getUserKey(userId, service));
    }

    private String getUserKey(String userId, String service) {
        return userId + "@" + service;
    }

    public ConcurrentHashMap<String, ServiceUserSessionListener> getListenerMap() {
        return listenerMap;
    }

    public void setListenerMap(ConcurrentHashMap<String, ServiceUserSessionListener> listenerMap) {
        this.listenerMap = listenerMap;
    }

    public Map<String, Class<?>> getAnnotatedClassMap() {
        return annotatedClassMap;
    }

    public void setAnnotatedClassMap(Map<String, Class<?>> annotatedClassMap) {
        this.annotatedClassMap = annotatedClassMap;
    }
}
