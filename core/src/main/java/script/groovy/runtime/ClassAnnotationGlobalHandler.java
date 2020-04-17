package script.groovy.runtime;


import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class ClassAnnotationGlobalHandler {
    public abstract Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime);

    public abstract void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, GroovyRuntime groovyRuntime);

    public abstract void handleAnnotatedClassesInjectBean(GroovyRuntime groovyRuntime);

    public boolean isBean() {
        return true;
    }

    public Object getKey() {
        return this.getClass();
    }

    public void handlerShutdown() {
    }
}
