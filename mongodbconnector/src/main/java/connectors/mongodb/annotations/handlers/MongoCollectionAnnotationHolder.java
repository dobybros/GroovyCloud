package connectors.mongodb.annotations.handlers;

import connectors.mongodb.annotations.DBCollection;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MongoCollectionAnnotationHolder extends ClassAnnotationHandler {
	private static final String TAG = MongoCollectionAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, DBCollection> collectionClassMap = new LinkedHashMap<>();

	@Override
	public void handlerShutdown() {
		collectionClassMap.clear();
	}

//	private static MongoCollectionAnnotationHolder instance;
	public MongoCollectionAnnotationHolder() {
//		instance = this;
	}
	
//	public static MongoCollectionAnnotationHolder getInstance() {
//		return instance;
//	}
	
	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return DBCollection.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Map<Class<?>, DBCollection> newCollectionClassMap = new LinkedHashMap<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				DBCollection mongoCollection = groovyClass.getAnnotation(DBCollection.class);
				if(mongoCollection != null) {
					newCollectionClassMap.put(groovyClass, mongoCollection);
				}
			}
			collectionClassMap = newCollectionClassMap;
		}
	}

	public Map<Class<?>, DBCollection> getCollectionClassMap() {
		return collectionClassMap;
	}

}
