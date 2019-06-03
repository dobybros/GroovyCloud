package com.dobybros.chat.script.annotations.login;

import chat.logs.LoggerEx;
import com.dobybros.chat.data.userinfo.UserInfo;
import com.docker.script.MyBaseRuntime;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoginGroovyRuntime extends MyBaseRuntime {
	private static final String TAG = LoginGroovyRuntime.class.getSimpleName();

	private List<GroovyObjectEx<UserLoginListener>> userLoginListeners;

	private ReadWriteLock userLoginLock = new ReentrantReadWriteLock();

	@Override
	public void prepare(String service, Properties properties, String localScriptPath) {
		super.prepare(service, properties, localScriptPath);
		final LoginGroovyRuntime instance = this;
		addClassAnnotationHandler(new ClassAnnotationHandler() {
			@Override
			public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime runtime) {
				return UserLogin.class;
			}

			@Override
			public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
											   MyGroovyClassLoader cl) {
				if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
					StringBuilder uriLogs = new StringBuilder(
							"\r\n---------------------------------------\r\n");

					List<GroovyObjectEx<UserLoginListener>> newUserLogiMap = new ArrayList<>();
					Set<String> keys = annotatedClassMap.keySet();
					GroovyRuntime groovyRuntime = instance;
					for (String key : keys) {
						Class<?> groovyClass = annotatedClassMap.get(key);
						if (groovyClass != null) {
							UserLogin messageReceivedAnnotation = groovyClass.getAnnotation(UserLogin.class);
							if (messageReceivedAnnotation != null) {
								GroovyObjectEx<UserLoginListener> messageReceivedObj = groovyRuntime
										.create(groovyClass);
								if (messageReceivedObj != null) {
									uriLogs.append("UserLoginListener #" + groovyClass + "\r\n");
									newUserLogiMap.add(messageReceivedObj);
								}
							}
						}
					}
					instance.userLoginLock.writeLock().lock();
					try {
						instance.userLoginListeners = newUserLogiMap;
					} finally {
						instance.userLoginLock.writeLock().unlock();
					}
					uriLogs.append("---------------------------------------");
					LoggerEx.info(TAG, uriLogs.toString());
				}
			}
		});
	}

	public Integer loginIfNotCreated(String userId, String service, Integer terminal, String scope) {
		userLoginLock.readLock().lock();
		try {
			if(userLoginListeners != null) {
				for(GroovyObjectEx<UserLoginListener> listener : userLoginListeners) {
					try {
						return listener.getObject().loginIfNotCreated(userId, service, terminal, scope);
					} catch (Throwable t) {
						t.printStackTrace();
						LoggerEx.error(TAG, "Handle loginIfNotCreated userId " + userId + " service " + service + " terminal " + terminal + " scope " + scope + " failed, " + t.getMessage());
					}
				}
			}
		} finally {
			userLoginLock.readLock().unlock();
		}
		return null;
	}
	public Integer userRoaming(String account, String service, Integer terminal, String lanId, UserInfo userInfo) {
		userLoginLock.readLock().lock();
		try {
			if(userLoginListeners != null) {
				for(GroovyObjectEx<UserLoginListener> listener : userLoginListeners) {
					try {
						return listener.getObject().userRoaming(account, service, terminal, lanId, userInfo);
					} catch (Throwable t) {
						t.printStackTrace();
						LoggerEx.error(TAG, "Handle userRoaming userId " + account + " service " + service + " terminal " + terminal + " lanId " + lanId + " userInfo " + userInfo + " failed, " + t.getMessage());
					}
				}
			}
		} finally {
			userLoginLock.readLock().unlock();
		}
		return null;
	}
}
