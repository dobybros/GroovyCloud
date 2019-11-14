package com.docker.script;

import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSONObject;
import com.docker.script.annotations.ServiceMemory;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by wenqi on 2018/12/4
 */
public class ServiceMemoryHandler extends ClassAnnotationHandler {
    private final String TAG = ServiceMemory.class.getSimpleName();
    private List<GroovyObjectEx<?>> serviceMemoryList = new ArrayList();
    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return ServiceMemory.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if(annotatedClassMap != null){
            Collection<Class<?>> values = annotatedClassMap.values();
            for (Class<?> groovyClass : values){
                GroovyObjectEx<?> groovyObj = ((GroovyBeanFactory)getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                serviceMemoryList.add(groovyObj);
            }
        }
    }
    public List<Object> getMemory(){
        if(!serviceMemoryList.isEmpty()){
            List<Object> list = new ArrayList<>();
            for (GroovyObjectEx<?> groovyObjectEx : serviceMemoryList){
                try {
                    Object jsonObject = groovyObjectEx.invokeRootMethod("memory");
                    list.add(jsonObject);
                } catch (Throwable e) {
                    LoggerEx.error(TAG, "Get service memory error, err: " + ExceptionUtils.getFullStackTrace(e));
                    e.printStackTrace();
                }
            }
            return list;
        }
        return null;
    }
    @Override
    public void handlerShutdown() {
    }
}
