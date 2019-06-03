package com.docker.rpc.remote.skeleton;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.AnalyticsLogger;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.alibaba.fastjson.JSON;
import com.docker.data.ServiceAnnotation;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.RemoteService;
import com.docker.script.ClassAnnotationHandlerEx;
import com.docker.server.OnlineServer;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;
import script.groovy.servlets.Tracker;
import script.memodb.ObjectId;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceSkeletonAnnotationHandler extends ClassAnnotationHandlerEx {
	private static final String TAG = ServiceSkeletonAnnotationHandler.class.getSimpleName();
    private ConcurrentHashMap<Long, SkelectonMethodMapping> methodMap = new ConcurrentHashMap<>();

    private Integer serviceVersion;
	private String service;
    private List<Class<? extends Annotation>> extraAnnotations;
    private List<ServiceAnnotation> annotationList = new ArrayList<>();

    @Override
    public void handlerShutdown() {
        methodMap.clear();
    }
    public ServiceSkeletonAnnotationHandler() {
        extraAnnotations = new ArrayList<>();
    }

    public void addExtraAnnotation(Class<? extends Annotation> annotationClass) {
        if(!extraAnnotations.contains(annotationClass)) {
            extraAnnotations.add(annotationClass);
        }
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return com.docker.rpc.remote.annotations.RemoteService.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            ConcurrentHashMap<Long, SkelectonMethodMapping> newMethodMap = new ConcurrentHashMap<>();
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);

                // Class<GroovyServlet> groovyClass =
                // groovyServlet.getGroovyClass();
                if (groovyClass != null) {
                    // Handle RequestIntercepting
                    com.docker.rpc.remote.annotations.RemoteService requestIntercepting = groovyClass.getAnnotation(com.docker.rpc.remote.annotations.RemoteService.class);
                    if (requestIntercepting != null) {
                        GroovyObjectEx<RemoteService> serverAdapter = getGroovyRuntime()
                                .create(groovyClass);
                        scanClass(groovyClass, serverAdapter, newMethodMap);
                    }
                }
            }
            this.methodMap = newMethodMap;
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(Integer serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public class SkelectonMethodMapping extends MethodMapping {
        private GroovyObjectEx<RemoteService> remoteService;

        public SkelectonMethodMapping(Method method) {
            super(method);
        }

        public MethodResponse invoke(MethodRequest request) throws CoreException {
            Object[] rawArgs = request.getArgs();
            Long crc = request.getCrc();
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
                System.arraycopy(rawArgs, 0, args, 0, rawArgs.length);
            }
//            if(parameterClasses != null) {
//                for(int i = 0; i < parameterClasses.length; i++) {
//                    Class<?> clazz = parameterClasses[i];
//                    if(i < rawArgs.length) {
//                        if(rawArgs[i] != null) {
//                            if(String.class.equals(clazz) && rawArgs[i] instanceof String) {
//                                args[i] = rawArgs[i];
//                            } else if(!ClassUtils.isPrimitiveOrWrapper(clazz) && rawArgs[i] instanceof JSON) {
//                                args[i] = JSON.parseObject((String) rawArgs[i], clazz);
//                            } else if(ClassUtils.isPrimitiveOrWrapper(clazz)) {
//                                args[i] = TypeUtils.cast(rawArgs[i], clazz, ParserConfig.getGlobalInstance());
//                            }
//                        }
//                    }
//                }
//            }

            Object returnObj = null;
            CoreException exception = null;
            String parentTrackId = request.getTrackId();
            String currentTrackId = null;
            if(parentTrackId != null) {
                currentTrackId = ObjectId.get().toString();
                Tracker tracker = new Tracker(currentTrackId, parentTrackId);
                Tracker.trackerThreadLocal.set(tracker);
            }
            StringBuilder builder = new StringBuilder();
            boolean error = false;
            long time = System.currentTimeMillis();
            try {
                builder.append("$$methodrequest:: " + method.getDeclaringClass().getSimpleName() + "#" + method.getName() + " $$service:: " + service + " $$serviceversion:: " + serviceVersion + " $$parenttrackid:: " + parentTrackId + " $$currenttrackid:: " + currentTrackId + " $$args:: " + request.getArgsTmpStr() );

                returnObj = remoteService.invokeRootMethod(method.getName(), args);
            } catch (Throwable t) {
                error = true;
                builder.append(" $$error" +
                        ":: " + t.getClass() + " $$errormsg:: " + t.getMessage());
                if(t instanceof InvokerInvocationException) {
                    Throwable theT = ((InvokerInvocationException) t).getCause();
                    if(theT != null) {
                        t = theT;
                    }
                }
                if(t instanceof CoreException) {
                    exception = (CoreException) t;
                }
                else {
                    exception = new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR, t.getMessage());
                }
            } finally {
                String ip = OnlineServer.getInstance().getIp();
                Tracker.trackerThreadLocal.remove();
                long invokeTokes = System.currentTimeMillis() - time;
                builder.append(" $$takes:: " + invokeTokes);
                builder.append(" $$sdockerip:: " + ip);
            }
            MethodResponse response = new MethodResponse(returnObj, exception);
//            response.setService(service);
            response.setRequest(request);
            response.setEncode(MethodResponse.ENCODE_JAVABINARY);
            response.setCrc(crc);
            if(returnObj != null)
                response.setReturnTmpStr(JSON.toJSONString(returnObj));
            builder.append(" $$returnobj:: " + response.getReturnTmpStr());
            if(error)
                AnalyticsLogger.error(TAG, builder.toString());
            else
                AnalyticsLogger.info(TAG, builder.toString());
            return response;
        }

        public GroovyObjectEx<RemoteService> getRemoteService() {
            return remoteService;
        }

        public void setRemoteService(GroovyObjectEx<RemoteService> remoteService) {
            this.remoteService = remoteService;
        }
    }

    public SkelectonMethodMapping getMethodMapping(Long crc) {
        return methodMap.get(crc);
    }

    public void scanClass(Class<?> clazz, GroovyObjectEx<RemoteService> serverAdapter, ConcurrentHashMap<Long, SkelectonMethodMapping> methodMap) {
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

        Method[] methods = ReflectionUtil.getMethods(clazz);
        if(methods != null) {
            for(Method method : methods) {
                if(method.isSynthetic() || method.getModifiers() == Modifier.PRIVATE)
                    continue;
                SkelectonMethodMapping mm = new SkelectonMethodMapping(method);
                mm.setRemoteService(serverAdapter);
                long value = ReflectionUtil.getCrc(method, service);
                if(methodMap.contains(value)) {
                    LoggerEx.warn(TAG, "Don't support override methods, please rename your method " + method + " for crc " + value + " and existing method " + methodMap.get(value).getMethod());
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes != null) {
                    boolean failed = false;
                    for(int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = ReflectionUtil.getInitiatableClass(parameterTypes[i]);
                        Class<?> parameterType = parameterTypes[i];
                        if(!ReflectionUtil.canBeInitiated(parameterType)) {
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
                returnType = ReflectionUtil.getInitiatableClass(returnType);
                mm.setReturnClass(returnType);
                methodMap.put(value, mm);

                //TODO DTS
                Annotation[] annotations = method.getDeclaredAnnotations();
                for(Annotation annotation : annotations) {
                    boolean isExists = extraAnnotations.contains(annotation.annotationType());
                    if(isExists) {
                        Method annotationMethodContainer = null;
                        try {
                            Method theMethod = annotation.annotationType().getDeclaredMethod("values");
                            if(theMethod != null)
                                annotationMethodContainer = theMethod;
                        } catch (NoSuchMethodException e) {
                        }
                        if(annotationMethodContainer != null) {
                            Annotation[] innerAnnotations = new Annotation[0];
                            try {
                                innerAnnotations = (Annotation[]) annotationMethodContainer.invoke(annotation);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                LoggerEx.warn(TAG, "(IllegalAccessException)Try to get annotations for key values in class " + annotationMethodContainer.getDeclaringClass()+ " method " + annotationMethodContainer.getName() + " failed, " + e.getMessage());
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                                LoggerEx.warn(TAG, "(InvocationTargetException)Try to get annotations for key values in class " + annotationMethodContainer.getDeclaringClass()+ " method " + annotationMethodContainer.getName() + " failed, " + e.getMessage());
                            }
                            if(innerAnnotations != null) {
                                for(Annotation innerAnnotation : innerAnnotations) {
                                    ServiceAnnotation serviceAnnotation = getServiceAnnotationFromAnnotation(innerAnnotation, method);
                                    annotationList.add(serviceAnnotation);
                                }
                            }
                        } else {
                            ServiceAnnotation serviceAnnotation = getServiceAnnotationFromAnnotation(annotation, method);
                            annotationList.add(serviceAnnotation);
                        }
                    }else{
                        continue;
                    }
                }

                LoggerEx.info("SCAN", "Mapping crc " + value + " for class " + clazz.getName() + " method " + method.getName() + " for service " + service);
            }
        }
    }

    private ServiceAnnotation getServiceAnnotationFromAnnotation(Annotation annotation, Method method) {
        ServiceAnnotation serviceAnnotation = new ServiceAnnotation();
        Map<String, Object> annotationParams = new HashMap<>();

        Method[] innerAnnotationMethods = annotation.annotationType().getDeclaredMethods();
        for(Method innerAnnotationMethod : innerAnnotationMethods) {
            String annotationKey = innerAnnotationMethod.getName();
            Object annotationValue = null;
            try {
                annotationValue = innerAnnotationMethod.invoke(annotation);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "(IllegalAccessException)Try to get annotation value for key " + annotationKey + " in class " + method.getDeclaringClass()+ " method " + method.getName() + " failed, " + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "(InvocationTargetException)Try to get annotation value for key " + annotationKey + " in class " + method.getDeclaringClass()+ " method " + method.getName() + " failed, " + e.getMessage());
            }
            if(annotationValue != null)
                annotationParams.put(annotationKey, annotationValue);
        }
        serviceAnnotation.setAnnotationParams(annotationParams);
        serviceAnnotation.setClassName(method.getDeclaringClass().getSimpleName());
        serviceAnnotation.setMethodName(method.getName());
        serviceAnnotation.setType(annotation.annotationType().getSimpleName());
        return serviceAnnotation;
    }

    @Override
    public void configService(com.docker.data.Service theService){
        theService.appendServiceAnnotation(annotationList);
    }
}
