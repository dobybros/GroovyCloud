package com.docker.script;

import script.groovy.runtime.ClassAnnotationHandler;

/**
 * Created by wenqi on 2018/12/4
 */
public abstract class ClassAnnotationHandlerEx extends ClassAnnotationHandler {
    public void configService(com.docker.data.Service theService){};
}
