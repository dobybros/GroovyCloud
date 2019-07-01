package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Map;

import script.groovy.runtime.classloader.MyGroovyClassLoader;

public interface ClassAnnotationListener {
	public abstract Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime);
	
	public abstract void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader);
}
