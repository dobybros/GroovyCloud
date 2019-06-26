package com.dobybros.gateway.channels.tcp;

import chat.logs.LoggerEx;
import com.dobybros.chat.annotation.MessageReceived;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.msgs.MessageReceivedListener;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UpStreamAnnotationHandler extends ClassAnnotationHandler {
	private static final String TAG = UpStreamAnnotationHandler.class.getSimpleName();
	private Map<Byte, GroovyObjectEx<MessageReceivedListener>> messageReceivedMap;

	public Map<Byte, GroovyObjectEx<MessageReceivedListener>> getMessageReceivedMap() {
		return messageReceivedMap;
	}

	public void setMessageReceivedMap(Map<Byte, GroovyObjectEx<MessageReceivedListener>> messageReceivedMap) {
		this.messageReceivedMap = messageReceivedMap;
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(
			GroovyRuntime groovyRuntime) {
		return MessageReceived.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder(
					"\r\n---------------------------------------\r\n");

			Map<Byte, GroovyObjectEx<MessageReceivedListener>> newMessageReceivedMap = new ConcurrentHashMap<>();
			Set<String> keys = annotatedClassMap.keySet();
			GroovyRuntime groovyRuntime = getGroovyRuntime();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);
				if (groovyClass != null) {
					MessageReceived messageReceivedAnnotation = groovyClass.getAnnotation(MessageReceived.class);
					if (messageReceivedAnnotation != null) {
						Class<? extends Data> dataClass = messageReceivedAnnotation.dataClass();
						Byte type = messageReceivedAnnotation.type();
						if (dataClass != null && type != null) {
							GroovyObjectEx<MessageReceivedListener> messageReceivedObj = ((GroovyBeanFactory)getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
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
