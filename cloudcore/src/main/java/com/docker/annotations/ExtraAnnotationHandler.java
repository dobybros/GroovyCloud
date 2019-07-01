package com.docker.annotations;

import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by wenqi on 2018/12/4
 */
public class ExtraAnnotationHandler extends ClassAnnotationHandler {
    private Class<? extends Annotation> annotationClass;
    public ExtraAnnotationHandler(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return this.annotationClass;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        System.out.println("");
    }
}
