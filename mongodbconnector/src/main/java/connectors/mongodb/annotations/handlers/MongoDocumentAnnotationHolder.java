package connectors.mongodb.annotations.handlers;

import connectors.mongodb.annotations.DBDocument;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MongoDocumentAnnotationHolder extends ClassAnnotationHandler {
	private static final String TAG = MongoDocumentAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, DBDocument> documentClassMap = new LinkedHashMap<>();
	
//	private static MongoDocumentAnnotationHolder instance;
	public MongoDocumentAnnotationHolder() {
//		instance = this;
	}

	@Override
	public void handlerShutdown() {
		documentClassMap.clear();
	}

	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return DBDocument.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Map<Class<?>, DBDocument> newDocumentClassMap = new LinkedHashMap<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				DBDocument mongoDocument = groovyClass.getAnnotation(DBDocument.class);
				if(mongoDocument != null) {
					newDocumentClassMap.put((Class<?>) groovyClass, mongoDocument);
				}
			}
			documentClassMap = newDocumentClassMap;

			MongoDBHandler mongoDBHandler = (MongoDBHandler) getGroovyRuntime().getClassAnnotationHandler(MongoDBHandler.class);
			mongoDBHandler.handleAnnotatedClasses(null, classLoader);
		}
	}

	public Map<Class<?>, DBDocument> getDocumentClassMap() {
		return documentClassMap;
	}

}
