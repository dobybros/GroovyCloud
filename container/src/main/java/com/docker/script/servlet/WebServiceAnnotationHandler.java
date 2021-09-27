package com.docker.script.servlet;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.rpc.remote.MethodMapping;
import com.docker.script.servlet.annotations.WebService;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebServiceAnnotationHandler extends ClassAnnotationHandler {
	private static final String TAG = WebServiceAnnotationHandler.class.getSimpleName();
    private ConcurrentHashMap<String, WebMethodMapping> methodMap = new ConcurrentHashMap<>();
    @Override
    public void handlerShutdown() {
        methodMap.clear();
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return WebService.class;
    }
    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            ConcurrentHashMap<String, WebMethodMapping> newMethodMap = new ConcurrentHashMap<>();
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);

                // Class<GroovyServlet> groovyClass =
                // groovyServlet.getGroovyClass();
                if (groovyClass != null) {
                    // Handle RequestIntercepting
                    WebService requestIntercepting = groovyClass.getAnnotation(WebService.class);
                    if (requestIntercepting != null) {
                        GroovyObjectEx<?> serverAdapter = ((GroovyBeanFactory)getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                        scanClass(groovyClass, serverAdapter, newMethodMap);
                    }
                }
            }
            this.methodMap = newMethodMap;
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }

    public Class<?>[] getMethodParameterTypes(String className, String methodName) {
        WebMethodMapping methodMapping = methodMap.get(className + "#" + methodName);
        if(methodMapping != null)
            return methodMapping.getMethod().getParameterTypes();
        return null;
    }

    public Object getWebServiceBean(String className, String methodName) throws CoreException {
        WebMethodMapping methodMapping = methodMap.get(className + "#" + methodName);
        return methodMapping.getWebService().getObject();
    }
    public Object execute(String className, String methodName, Object... args) throws CoreException {
        WebMethodMapping methodMapping = methodMap.get(className + "#" + methodName);
        try {
            return methodMapping.invoke(args);
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof CoreException)
                throw (CoreException) e;
            else
                throw new CoreException(ChatErrorCodes.ERROR_UNKNOWN, "Unknown error while invoke method " + methodName + " in class " + className + " with args " + Arrays.toString(args) + " failed, " + e.getMessage() + " errorClass " + e.getClass());
        }
    }

    public class WebMethodMapping extends MethodMapping {
        private GroovyObjectEx<?> webService;

        public WebMethodMapping(Method method) {
            super(method);
        }

        public Object invoke(Object[] rawArgs) throws CoreException {
            if(method == null)
                throw new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_METHOD_NULL, "Invoke method is null");
            int argLength = rawArgs != null ? rawArgs.length : 0;
            Object[] args = null;
            if(parameterTypes.length == argLength) {
                args = rawArgs;
            } else if(parameterTypes.length < argLength) {
                args = new Object[parameterTypes.length];
                System.arraycopy(rawArgs, 0, args, 0, parameterTypes.length);
            } else {
                args = new Object[parameterTypes.length];
                if (rawArgs != null)
                    System.arraycopy(rawArgs, 0, args, 0, rawArgs.length);
            }
            Object returnObj = null;
            CoreException exception = null;
            try {
                returnObj = webService.invokeRootMethod(method.getName(), args);
//                returnObj = method.invoke(obj, args);
            } catch (Throwable t) {
                if(t instanceof InvokerInvocationException) {
                    Throwable theT = ((InvokerInvocationException) t).getCause();
                    if(theT != null)
                        t = theT;
                }
                if(t instanceof CoreException)
                    exception = (CoreException) t;
                else
                    exception = new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR, t.getMessage());
                throw exception;
            }
            return returnObj;
        }

        public GroovyObjectEx<?> getWebService() {
            return webService;
        }

        public void setWebService(GroovyObjectEx<?> webService) {
            this.webService = webService;
        }
    }

//    public WebMethodMapping getMethodMapping(String key) {
//        return methodMap.get(key);
//    }

    public void scanClass(Class<?> clazz, GroovyObjectEx<?> serverAdapter, ConcurrentHashMap<String, WebMethodMapping> methodMap) {
        if(clazz == null)
            return;
//        Object obj = cachedInstanceMap.get(clazz);
//        if(obj == null) {
//            try {
//                obj = clazz.newInstance();
//                cachedInstanceMap.putIfAbsent(clazz, obj);
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        if(!ReflectionUtil.canBeInitiated(clazz)) {
//            LoggerEx.fatal(TAG, "Class " + clazz + " couldn't be initialized without parameters, it will cause the rpc call failed!");
//            return;
//        }

        Method[] methods = chat.utils.ReflectionUtil.getMethods(clazz);
        if(methods != null) {
            for(Method method : methods) {
                if(method.isSynthetic() || method.getModifiers() == Modifier.PRIVATE)
                    continue;
                WebMethodMapping mm = new WebMethodMapping(method);
                mm.setWebService(serverAdapter);
                String value = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
                if(methodMap.contains(value)) {
                    LoggerEx.warn(TAG, "Don't support override methods, please rename your method " + method + " for value " + value + " and existing method " + methodMap.get(value).getMethod());
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes != null) {
                    boolean failed = false;
                    for(int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = chat.utils.ReflectionUtil.getInitiatableClass(parameterTypes[i]);
                        Class<?> parameterType = parameterTypes[i];
                        if(!chat.utils.ReflectionUtil.canBeInitiated(parameterType)) {
                            failed = true;
                            LoggerEx.warn(TAG, "Parameter " + parameterType + " in method " + method + " couldn't be initialized. ");
                            break;
                        }
                    }
                    if(failed)
                        continue;
                }
                mm.setParameterTypes(parameterTypes);

                Class<?> returnType = method.getReturnType();
                returnType = chat.utils.ReflectionUtil.getInitiatableClass(returnType);
                mm.setReturnClass(returnType);
                methodMap.put(value, mm);

                LoggerEx.info("SCAN", "Mapping method " + value + " for class " + clazz.getName() + " method " + method.getName());
            }
        }
    }
}
