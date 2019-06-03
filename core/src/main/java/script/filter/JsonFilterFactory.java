package script.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import script.filter.annotations.JsonFilterClass;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import chat.logs.LoggerEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class JsonFilterFactory extends ClassAnnotationHandler{
	private static final String TAG = JsonFilterFactory.class.getSimpleName();
	
//	protected static JsonFilterFactory instance;
	private Map<String, GroovyObjectEx<JsonFilter>> filterMap;

	@Override
	public void handlerShutdown() {
		if(filterMap != null) {
			filterMap.clear();
		}
	}

	public JsonFilterFactory() {
//		instance = this;
	}
	
//	public static JsonFilterFactory getInstance() {
//		return instance;
//	}

	public Object fromByKey(Class<?> filterClass, Object doc, Object...arguments) {
		return fromByKey(filterClass.getName(), doc, arguments);
	}

	public Object fromByKey(String key, Object doc, Object...arguments) {
		if(doc == null || key == null)
			return null;
		GroovyObjectEx<JsonFilter> goe = getDocumentFilter(key);
		if(goe != null) {
			try {
				return goe.getObject().from(doc, arguments);
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "(fromByKey) From document " + doc + " by key " + key + " failed, " + t.getMessage());
			}
		} else {
			LoggerEx.error(TAG, "(fromByKey) No specified document filter has been found for key " + key);
		}
		return null;
	}

	public Object filterByKey(String key, Object target, Object...arguments) {
		if(target == null || key == null)
			return null;
		GroovyObjectEx<JsonFilter> goe = getDocumentFilter(key);
		if(goe != null) {
			try {
				return goe.getObject().filter(target, arguments);
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "(filterByKey) Filter target " + target + " by key " + key + " failed, " + t.getMessage());
			}
		} else {
			LoggerEx.error(TAG, "(filterByKey) No specified document filter has been found for key " + key);
		}
		return null;
	}
	
	public Object filter(Object target, Object...arguments) {
		if(target == null)
			return null;
		return filterByKey(target.getClass().getName(), target, arguments);
	}
	
	private Map<String, GroovyObjectEx<JsonFilter>> getFilterMap() {
		return filterMap;
	}

	private GroovyObjectEx<JsonFilter> getDocumentFilter(String key) {
		if(filterMap != null)
			return filterMap.get(key);
		return null;
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(
			GroovyRuntime groovyRuntime) {
		return JsonFilterClass.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder(
					"\r\n---------------------------------------\r\n");

			Map<String, GroovyObjectEx<JsonFilter>> newFilterMap = new ConcurrentHashMap<>();
			Set<String> keys = annotatedClassMap.keySet();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);

				// Class<GroovyServlet> groovyClass =
				// groovyServlet.getGroovyClass();
				if (groovyClass != null) {
					// Handle JsonFilterClass
					Class<?> targetClass = null;
					Type[] types = groovyClass.getGenericInterfaces();
					 for (Type type : types) {
						 if(type instanceof ParameterizedType) {
							 ParameterizedType pType = (ParameterizedType) type;
							 if(pType.getRawType().equals(JsonFilter.class)) {
								 Type[] params = pType.getActualTypeArguments();
								 if(params != null && params.length == 1) {
									 if(params[0] instanceof Class) {
										 targetClass = (Class<?>) params[0];
									 }
								 }
							 }
						 }
			        }
					 
					JsonFilterClass requestIntercepting = groovyClass.getAnnotation(JsonFilterClass.class);
					if (requestIntercepting != null) {
						String filterKey = requestIntercepting.key();
						if(targetClass == null && StringUtils.isBlank(filterKey)) {
							LoggerEx.error(TAG, "JsonFilterClass " + filterKey + "#" + groovyClass + " is ignored, if targetClass is ParameterizedType, then key has to be specified. \r\n");
							continue;
						}
						GroovyObjectEx<JsonFilter> serverAdapter = getGroovyRuntime()
								.create(groovyClass);
						if (serverAdapter != null) {
							if (StringUtils.isBlank(filterKey)) {
								filterKey = targetClass.getName();
							}
							newFilterMap.put(filterKey, serverAdapter);
							uriLogs.append("JsonFilterClass " + filterKey + "#" + groovyClass + "\r\n");
						}
					}
				}
				this.filterMap = newFilterMap;
			}
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}
}
