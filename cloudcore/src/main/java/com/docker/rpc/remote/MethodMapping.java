package com.docker.rpc.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodMapping {
    protected Method method;
    protected Class<?>[] parameterTypes;
    protected Type[] genericParameterTypes;
    protected Class<?> returnClass;
    protected Type genericReturnClass;

    public MethodMapping(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class<?> returnClass) {
        this.returnClass = returnClass;
    }

    public Type getGenericReturnClass() {
        return genericReturnClass;
    }

    public void setGenericReturnClass(Type genericReturnClass) {
        this.genericReturnClass = genericReturnClass;
    }

    public Type[] getGenericParameterTypes() {
        return genericParameterTypes;
    }

    public void setGenericParameterTypes(Type[] genericParameterTypes) {
        this.genericParameterTypes = genericParameterTypes;
    }
}