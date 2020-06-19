package script.groovy.runtime;

import script.groovy.annotation.RegisterClassAnnotation;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * Created by lick on 2020/6/19.
 * Description：
 */
public class RegisterClassAnnotationHandler extends ClassAnnotationHandler {
    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return RegisterClassAnnotation.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null) {
            Collection<Class<?>> values = annotatedClassMap.values();
            for (Class<?> groovyClass : values) {
                RegisterClassAnnotation registerClassAnnotation = groovyClass.getAnnotation(RegisterClassAnnotation.class);
                if (registerClassAnnotation != null) {
                    GroovyObjectEx<?> groovyObj = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                    try {
                        Object o = groovyObj.getObject();
                        if(o instanceof ClassAnnotationHandler){
                            ClassAnnotationHandler classAnnotationHandler = ((ClassAnnotationHandler)o);
                            classAnnotationHandler.handleAnnotatedClasses(classLoader.getGroovyRuntime().getAllClasses(), classLoader);
                        }
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean isBean() {
        return super.isBean();
    }

    @Override
    public GroovyRuntime getGroovyRuntime() {
        return super.getGroovyRuntime();
    }

    @Override
    public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
        super.setGroovyRuntime(groovyRuntime);
    }

    @Override
    public Object getKey() {
        return super.getKey();
    }
}
