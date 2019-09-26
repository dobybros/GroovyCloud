package script.groovy.runtime;


import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class ClassAnnotationHandler {
	private GroovyRuntime groovyRuntime;

	public abstract Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime);
	
	public abstract void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader);

	public GroovyRuntime getGroovyRuntime() {
		return groovyRuntime;
	}

	public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
		this.groovyRuntime = groovyRuntime;
	}

	public Object getKey() {
		return this.getClass();
	}

	public void handlerShutdown() {}
}
