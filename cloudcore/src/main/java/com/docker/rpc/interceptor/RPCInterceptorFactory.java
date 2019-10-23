package com.docker.rpc.interceptor;

import script.groovy.runtime.MethodInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RPCInterceptorFactory {
    public static RPCInterceptorFactory instance;
    private Map<String, List<MethodInterceptor>> methodInterceptorMap;
    public static synchronized RPCInterceptorFactory getInstance(){
        if (instance == null) {
            synchronized (RPCInterceptorFactory.class) {
                if (instance == null) {
                    instance = new RPCInterceptorFactory();
                }
            }
        }
        return instance;
    }

    public void addMethodInterceptor(String key, MethodInterceptor methodInterceptor) {
        if (key != null && methodInterceptor != null) {
            if (methodInterceptorMap == null) {
                methodInterceptorMap = new ConcurrentHashMap<>();
            }

            if (!methodInterceptorMap.containsKey(key)) {
                List methodInterceptors = new ArrayList<MethodInterceptor>();
                methodInterceptors.add(methodInterceptor);
                methodInterceptorMap.put(key, methodInterceptors);
            } else {
                List<MethodInterceptor> methodInterceptors = methodInterceptorMap.get(key);
                if (methodInterceptors != null) {
                    methodInterceptors.add(methodInterceptor);
                }
            }
        }
    }

    public Map<String, List<MethodInterceptor>> getMethodInterceptorMap() {
        return methodInterceptorMap;
    }
}
