package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang.StringUtils;

import script.groovy.annotation.Bean;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class GroovyBeanFactory extends ClassAnnotationHandler {
	private static final String TAG = GroovyBeanFactory.class.getSimpleName();

	private ConcurrentHashMap<String, GroovyObjectEx> beanMap = new ConcurrentHashMap<>();
//	private ConcurrentHashMap<String, Class<?>> proxyClassMap = new ConcurrentHashMap<>();

	@Override
	public void handlerShutdown() {
		beanMap.clear();
//		proxyClassMap.clear();
	}

	public GroovyBeanFactory() {
//		instance = this;
	}
	
//	public Class<?> getProxyClass(String className) {
//		return proxyClassMap.get(className);
//	}
	
	public <T> GroovyObjectEx<T> getBean(String beanName) {
		if(beanMap != null) {
			return beanMap.get(beanName);
		}
		return null;
	}
	
	public <T> GroovyObjectEx<T> getBean(Class<?> c) {
		if(c == null)
			return null;
		String groovyPath = GroovyRuntime.path(c);
		return getBean(groovyPath);
	}
	
	public <T> GroovyObjectEx<T> getBean(Class<?> c, boolean forceCreate) {
		if(forceCreate)
			return getObject(c);
		else
			return getBean(c);
	}

	private <T> GroovyObjectEx<T> getObject(String beanName, Class<?> c, ConcurrentHashMap<String, GroovyObjectEx> beanMap) {
		if(beanMap == null) {
			beanMap = this.beanMap;
		}
		String groovyPath = GroovyRuntime.path(c);
		GroovyObjectEx<T> goe = beanMap.get(groovyPath);
		if(goe == null) {
			
			goe = getGroovyRuntime().create(groovyPath);
			if(beanName == null) {
				beanName = groovyPath;
			}
			if(goe != null) {
				GroovyObjectEx<T> oldgoe = beanMap.putIfAbsent(beanName, goe);
				if(oldgoe != null) 
					goe = oldgoe;
			}
		}
		return goe;
	}
	private <T> GroovyObjectEx<T> getObject(Class<?> c, ConcurrentHashMap<String, GroovyObjectEx> beanMap) {
		return getObject(null, c, beanMap);
	}
	
	private <T> GroovyObjectEx<T> getObject(Class<?> c) {
		return getObject(c, null);
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return Bean.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
									   MyGroovyClassLoader classLoader) {
		ConcurrentHashMap<String, GroovyObjectEx> newBeanMap = new ConcurrentHashMap<>();
//		ConcurrentHashMap<String, Class<?>> newProxyClassMap = new ConcurrentHashMap<>();
		if (annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for (Class<?> groovyClass : values) {
				Bean bean = groovyClass.getAnnotation(Bean.class);
				String name = bean.name();
				if (StringUtils.isBlank(name)) {
					name = null;
				}
//				Class<?> groovyObjectExProxyClass = newProxyClassMap.get(groovyClass.getName());
//				if (groovyObjectExProxyClass == null) {
//					String[] strs = new String[]{
//							"package script.groovy.runtime;",
//							"import script.groovy.object.GroovyObjectEx",
//							"class GroovyObjectEx" + groovyClass.getSimpleName() + "Proxy extends " + groovyClass.getName() + " implements GroovyInterceptable{",
//							"private GroovyObjectEx<?> groovyObject;",
//							"public GroovyObjectEx" + groovyClass.getSimpleName() + "Proxy(GroovyObjectEx<?> groovyObject) {",
//							"this.groovyObject = groovyObject;",
//							"}",
//							"def invokeMethod(String name, args) {",
////                            "chat.logs.LoggerEx.info(\" PROXY \", \"Invoked \" + name + \" args \" + Arrays.toString(args));",
//							"Class<?> groovyClass = this.groovyObject.getGroovyClass();",
//							"def calledMethod = groovyClass.metaClass.getMetaMethod(name, args);",
//							"def returnObj = calledMethod?.invoke(this.groovyObject.getObject(), args);",
//							"return returnObj;",
//							"}",
//                            "Class<?> getGroovyClass() {",
//                            "Class<?> groovyClass = this.groovyObject == null ? null : this.groovyObject.getGroovyClass();",
//                            "return groovyClass;",
//                            "}",
//							"}"
//					};
//					String proxyClassStr = StringUtils.join(strs, "\r\n");
//					groovyObjectExProxyClass = getGroovyRuntime().getClassLoader().parseClass(proxyClassStr,
//							"/script/groovy/runtime/proxy/GroovyObjectEx" + groovyClass.getSimpleName() + "Proxy.groovy");
//
//					newProxyClassMap.put(groovyClass.getName(), groovyObjectExProxyClass);
//				}

				GroovyObjectEx groovyObjectEx = getObject(name, groovyClass, newBeanMap);
				try {
					groovyObjectEx.getObject(false);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
//		ConcurrentHashMap<String, Class<?>> oldProxyClassMap = proxyClassMap;
//		proxyClassMap = newProxyClassMap;
//		if (oldProxyClassMap != null)
//			oldProxyClassMap.clear();

		ConcurrentHashMap<String, GroovyObjectEx> oldBeanMap = beanMap;
		beanMap = newBeanMap;
		if (oldBeanMap != null)
			oldBeanMap.clear();

		GroovyObjectEx.fillGroovyObjects(beanMap.values(), getGroovyRuntime());
	}

}
