package com.proxy.im;

import chat.logs.LoggerEx;
import com.proxy.annotation.ProxyMessageReceived;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProxyUpStreamAnnotationHandler extends ClassAnnotationHandler {
    private static final String TAG = ProxyUpStreamAnnotationHandler.class.getSimpleName();
    private List<GroovyObjectEx<ProxyMessageReceivedListener>> proxyMessageReceivedListeners = new ArrayList<>();

    public List<GroovyObjectEx<ProxyMessageReceivedListener>> getProxyMessageReceivedListeners() {
        return proxyMessageReceivedListeners;
    }

    public void setProxyMessageReceivedListeners(List<GroovyObjectEx<ProxyMessageReceivedListener>> proxyMessageReceivedListeners) {
        this.proxyMessageReceivedListeners = proxyMessageReceivedListeners;
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(
            GroovyRuntime groovyRuntime) {
        return ProxyMessageReceived.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                       MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            Set<String> keys = annotatedClassMap.keySet();
            List<GroovyObjectEx<ProxyMessageReceivedListener>> newProxyMessageReceivedListeners = new ArrayList<>();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);
                if (groovyClass != null) {
                    ProxyMessageReceived messageReceivedAnnotation = groovyClass.getAnnotation(ProxyMessageReceived.class);
                    if (messageReceivedAnnotation != null) {
                        GroovyObjectEx<ProxyMessageReceivedListener> messageReceivedObj = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        if (messageReceivedObj != null) {
                            uriLogs.append("MessageReceivedListener " + groovyClass + "\r\n");
                            newProxyMessageReceivedListeners.add(messageReceivedObj);
                        }
                    }
                }
            }
            this.proxyMessageReceivedListeners = newProxyMessageReceivedListeners;
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }


}
