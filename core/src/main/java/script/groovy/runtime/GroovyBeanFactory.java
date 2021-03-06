package script.groovy.runtime;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import org.apache.commons.lang.StringUtils;
import script.groovy.annotation.Bean;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	public<T> GroovyObjectEx<T> getClassBean(Class<?> c) {
		return getObject(c);
	}
	public<T> GroovyObjectEx<T> getClassBean(String classStr) {
		return getObject(classStr);
	}

	public <T> GroovyObjectEx<T> getBean(Class<?> c, boolean forceCreate) {
		if(forceCreate)
			return getObject(c);
		else
			return getBean(c);
	}

	private <T> GroovyObjectEx<T> getObject(String beanName, String groovyPath, ConcurrentHashMap<String, GroovyObjectEx> beanMap) {
		if(beanMap == null) {
			beanMap = this.beanMap;
		}
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
	private <T> GroovyObjectEx<T> getObject(String beanName, Class<?> c, ConcurrentHashMap<String, GroovyObjectEx> beanMap) {
		String groovyPath = GroovyRuntime.path(c);
		return getObject(beanName, groovyPath, beanMap);
	}

	private <T> GroovyObjectEx<T> getObject(Class<?> c, ConcurrentHashMap<String, GroovyObjectEx> beanMap) {
		return getObject(null, c, beanMap);
	}
	
	private <T> GroovyObjectEx<T> getObject(Class<?> c) {
		return getObject(c, null);
	}
	private <T> GroovyObjectEx<T> getObject(String cstr) {
		return getObject(null, cstr, null);
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return Bean.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
									   MyGroovyClassLoader classLoader) {
		ConcurrentHashMap<String, GroovyObjectEx> newBeanMap = beanMap;//new ConcurrentHashMap<>();
//		if(beanMap != null) {
//			for(Map.Entry<String, GroovyObjectEx> entry : beanMap.entrySet()) {
//				try {
//					if(entry.getValue().getGroovyClass().getClassLoader().getParent().equals(classLoader)) {
//						newBeanMap.put(entry.getKey(), entry.getValue());
//					}
//				} catch (Throwable e) {
//				}
//			}
//		}
//		ConcurrentHashMap<String, Class<?>> newProxyClassMap = new ConcurrentHashMap<>();
		if (annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for (Class<?> groovyClass : values) {
				long time = System.currentTimeMillis();
				Bean bean = groovyClass.getAnnotation(Bean.class);
				String name = getGroovyRuntime().processAnnotationString(bean.name());
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
				LoggerEx.info(TAG, "class " + groovyClass.getName() +",Spend time: " + (System.currentTimeMillis() - time));

			}
		}
//		ConcurrentHashMap<String, Class<?>> oldProxyClassMap = proxyClassMap;
//		proxyClassMap = newProxyClassMap;
//		if (oldProxyClassMap != null)
//			oldProxyClassMap.clear();

//		ConcurrentHashMap<String, GroovyObjectEx> oldBeanMap = beanMap;
		beanMap = newBeanMap;
//		if (oldBeanMap != null)
//			oldBeanMap.clear();
		long time = System.currentTimeMillis();
		GroovyObjectEx.fillGroovyObjects(beanMap.values(), getGroovyRuntime());
		LoggerEx.info(TAG, "fillGroovyObjects " +",Spend time: " + (System.currentTimeMillis() - time));

	}
}
