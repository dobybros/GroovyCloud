package com.proxy.im;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.proxy.annotation.ProxyMessageReceived;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyUpStreamAnnotationHandler extends ClassAnnotationHandler {
	private static final String TAG = ProxyUpStreamAnnotationHandler.class.getSimpleName();
	private Map<Byte, GroovyObjectEx<ProxyMessageReceivedListener>> messageReceivedMap;

	public Map<Byte, GroovyObjectEx<ProxyMessageReceivedListener>> getMessageReceivedMap() {
		return messageReceivedMap;
	}

	public void setMessageReceivedMap(Map<Byte, GroovyObjectEx<ProxyMessageReceivedListener>> messageReceivedMap) {
		this.messageReceivedMap = messageReceivedMap;
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

			Map<Byte, GroovyObjectEx<ProxyMessageReceivedListener>> newMessageReceivedMap = new ConcurrentHashMap<>();
			Set<String> keys = annotatedClassMap.keySet();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);
				if (groovyClass != null) {
					ProxyMessageReceived messageReceivedAnnotation = groovyClass.getAnnotation(ProxyMessageReceived.class);
					if (messageReceivedAnnotation != null) {
						Class<? extends Data> dataClass = messageReceivedAnnotation.dataClass();
						Byte type = (byte)messageReceivedAnnotation.type();
						if (dataClass != null && type != null) {
							GroovyObjectEx<ProxyMessageReceivedListener> messageReceivedObj = ((GroovyBeanFactory)getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
							if (messageReceivedObj != null) {
								uriLogs.append("MessageReceivedListener " + dataClass + "#" + groovyClass + "\r\n");
								newMessageReceivedMap.put(type, messageReceivedObj);
							}
						}
					}
				}
			}
			this.messageReceivedMap = newMessageReceivedMap;
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}


}
