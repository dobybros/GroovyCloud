package com.proxy.im;

import chat.logs.LoggerEx;
import com.proxy.annotation.ProxySessionListener;
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

/**
 * @author lick
 * @date 2019/11/12
 */
public class ProxyAnnotationHandler extends ClassAnnotationHandler {
    private final String TAG = ProxyAnnotationHandler.class.getSimpleName();
    private List<GroovyObjectEx<com.proxy.im.ProxySessionListener>> tcpListeners = new ArrayList<>();

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return ProxySessionListener.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            List<GroovyObjectEx<com.proxy.im.ProxySessionListener>> newTcpList = new ArrayList<>();
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);
                if (groovyClass != null) {
                    ProxySessionListener messageReceivedAnnotation = groovyClass.getAnnotation(ProxySessionListener.class);
                    if (messageReceivedAnnotation != null) {
                        GroovyObjectEx<com.proxy.im.ProxySessionListener> messageReceivedObj = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        if (messageReceivedObj != null) {
                            uriLogs.append("TcpListener " + "#" + groovyClass + "\r\n");
                            newTcpList.add(messageReceivedObj);
                        }
                    }
                }
            }
            this.tcpListeners = newTcpList;
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }

    public List<GroovyObjectEx<com.proxy.im.ProxySessionListener>> getTcpListeners() {
        return tcpListeners;
    }
}
