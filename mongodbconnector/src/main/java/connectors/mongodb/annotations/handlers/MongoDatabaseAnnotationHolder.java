package connectors.mongodb.annotations.handlers;

import connectors.mongodb.annotations.Database;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MongoDatabaseAnnotationHolder extends ClassAnnotationHandler {
	private static final String TAG = MongoDatabaseAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, Database> dbClassMap = new LinkedHashMap<>();
	
//	private static MongoDatabaseAnnotationHolder instance;
	public MongoDatabaseAnnotationHolder() {
//		instance = this;
	}

	@Override
	public void handlerShutdown() {
		dbClassMap.clear();
	}
	
	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return Database.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Map<Class<?>, Database> newDbClassMap = new LinkedHashMap<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				Database mongoDatabase = groovyClass.getAnnotation(Database.class);
				newDbClassMap.put(groovyClass, mongoDatabase);
			}
			dbClassMap = newDbClassMap;
		}
	}

	public Map<Class<?>, Database> getDbClassMap() {
		return dbClassMap;
	}

}
