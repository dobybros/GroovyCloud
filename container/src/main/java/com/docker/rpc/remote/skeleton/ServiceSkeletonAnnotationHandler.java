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
import com.docker.rpc.remote.RpcServerInterceptor;
import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.script.ClassAnnotationHandlerEx;
import com.docker.server.OnlineServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;
import script.groovy.servlets.Tracker;
import script.memodb.ObjectId;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        if (!extraAnnotations.contains(annotationClass)) {
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
//                        GroovyObjectEx<RemoteService> serverAdapter = getGroovyRuntime()
//                                .create(groovyClass);
                        GroovyObjectEx<RemoteService> serverAdapter = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
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
        private List<RpcServerInterceptor> rpcServerInterceptors;
        public SkelectonMethodMapping(Method method) {
            super(method);
        }

        private Object[] prepareMethodArgs(MethodRequest request) throws CoreException {
            Object[] rawArgs = request.getArgs();
            if (method == null)
                throw new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_METHOD_NULL, "Invoke method is null");
            int argLength = rawArgs != null ? rawArgs.length : 0;
            Object[] args = null;
            if (parameterTypes.length == argLength) {
                args = rawArgs;
            } else if (parameterTypes.length < argLength) {
                args = new Object[parameterTypes.length];
                System.arraycopy(rawArgs, 0, args, 0, parameterTypes.length);
            } else {
                args = new Object[parameterTypes.length];
                if(rawArgs != null)
                    System.arraycopy(rawArgs, 0, args, 0, rawArgs.length);
            }
            return args;
        }

        public MethodResponse invoke(MethodRequest request) throws CoreException {
            Object[] args = prepareMethodArgs(request);
            Long crc = request.getCrc();
            Object returnObj = null;
            CoreException exception = null;
            String parentTrackId = request.getTrackId();
            String currentTrackId = null;
            if (parentTrackId != null) {
                currentTrackId = ObjectId.get().toString();
                Tracker tracker = new Tracker(currentTrackId, parentTrackId);
                Tracker.trackerThreadLocal.set(tracker);
            }
            StringBuilder builder = new StringBuilder();
            boolean error = false;
            long time = System.currentTimeMillis();
            try {
                builder.append("$$methodrequest:: " + method.getDeclaringClass().getSimpleName() + "#" + method.getName() + " $$service:: " + service + " $$serviceversion:: " + serviceVersion + " $$parenttrackid:: " + parentTrackId + " $$currenttrackid:: " + currentTrackId + " $$args:: " + request.getArgsTmpStr());
                returnObj = remoteService.invokeRootMethod(method.getName(), args);
            } catch (Throwable t) {
                error = true;
                builder.append(" $$error" +
                        ":: " + t.getClass() + " $$errormsg:: " + ExceptionUtils.getFullStackTrace(t));
                if (t instanceof InvokerInvocationException) {
                    Throwable theT = ((InvokerInvocationException) t).getCause();
                    if (theT != null) {
                        t = theT;
                    }
                }
                if (t instanceof CoreException) {
                    exception = (CoreException) t;
                } else {
                    exception = new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR, t.getMessage());
                }
                LoggerEx.error(TAG, "invoke MethodRequest " + request.toString() + " error, " + ExceptionUtils.getFullStackTrace(t));
            } finally {
                String ip = OnlineServer.getInstance().getIp();
                Tracker.trackerThreadLocal.remove();
                long invokeTokes = System.currentTimeMillis() - time;
                builder.append(" $$takes:: " + invokeTokes);
                builder.append(" $$sdockerip:: " + ip);
            }
            MethodResponse response = new MethodResponse(returnObj, exception);
            response.setRequest(request);
            response.setEncode(MethodResponse.ENCODE_JAVABINARY);
            response.setCrc(crc);
            if (returnObj != null)
                response.setReturnTmpStr(JSON.toJSONString(returnObj));
//            builder.append(" $$returnobj:: " + response.getReturnTmpStr());
            if (error)
                AnalyticsLogger.error(TAG, builder.toString());
            else
                AnalyticsLogger.info(TAG, builder.toString());
            return response;
        }

        public Object invokeAsync(MethodRequest request, String callbackFutureId) throws CoreException {
            Object[] args = prepareMethodArgs(request);
            Object returnObj = remoteService.invokeRootMethod(method.getName(), args);
            return returnObj;
        }

        public GroovyObjectEx<RemoteService> getRemoteService() {
            return remoteService;
        }

        public void setRemoteService(GroovyObjectEx<RemoteService> remoteService) {
            this.remoteService = remoteService;
        }

        public List<RpcServerInterceptor> getRpcServerInterceptors() {
            return rpcServerInterceptors;
        }

        public void setRpcServerInterceptors(List<RpcServerInterceptor> rpcServerInterceptors) {
            this.rpcServerInterceptors = rpcServerInterceptors;
        }
    }

    public SkelectonMethodMapping getMethodMapping(Long crc) {
        return methodMap.get(crc);
    }

    public void scanClass(Class<?> clazz, GroovyObjectEx<RemoteService> serverAdapter, ConcurrentHashMap<Long, SkelectonMethodMapping> methodMap) {
        if (clazz == null)
            return;
        com.docker.rpc.remote.annotations.RemoteService remoteService = clazz.getAnnotation(com.docker.rpc.remote.annotations.RemoteService.class);
        int concurrentLimit = remoteService.concurrentLimit();
        int queueSize = remoteService.waitingSize();
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if (methods != null) {
            for (Method method : methods) {
                if (method.isSynthetic() || method.getModifiers() == Modifier.PRIVATE)
                    continue;
                SkelectonMethodMapping mm = new SkelectonMethodMapping(method);
                mm.setRemoteService(serverAdapter);
                long value = ReflectionUtil.getCrc(method, service);
                if (methodMap.contains(value)) {
                    LoggerEx.warn(TAG, "Don't support override methods, please rename your method " + method + " for crc " + value + " and existing method " + methodMap.get(value).getMethod());
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                Type[] genericParamterTypes = method.getGenericParameterTypes();
                if (parameterTypes != null) {
                    boolean failed = false;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = ReflectionUtil.getInitiatableClass(parameterTypes[i]);
                        Class<?> parameterType = parameterTypes[i];
                        if (!ReflectionUtil.canBeInitiated(parameterType)) {
                            failed = true;
                            LoggerEx.warn(TAG, "Parameter " + parameterType + " in method " + method + " couldn't be initialized. ");
                            break;
                        }
                    }
                    if (failed)
                        continue;
                }
                mm.setParameterTypes(parameterTypes);
                mm.setGenericParameterTypes(genericParamterTypes);
                Class<?> returnType = method.getReturnType();
                returnType = ReflectionUtil.getInitiatableClass(returnType);
                mm.setReturnClass(returnType);
                if (method.getGenericReturnType().getTypeName().contains(CompletableFuture.class.getTypeName())) {
                    mm.setAsync(true);
                    if(concurrentLimit != -1){
                        RpcServerInterceptor concurrentLimitRpcServerInterceptor = new ConcurrentLimitRpcServerInterceptor(concurrentLimit, queueSize,  clazz.getName() +"-" + method.getName());
                        List<RpcServerInterceptor> rpcServerInterceptors = new ArrayList<>();
                        rpcServerInterceptors.add(concurrentLimitRpcServerInterceptor);
                        mm.setRpcServerInterceptors(rpcServerInterceptors);
                    }
                } else {
                    mm.setAsync(false);
                }
                methodMap.put(value, mm);
                RpcCacheManager.getInstance().putCrcMethodMap(value, service + "_" + clazz.getSimpleName() + "_" + method.getName());
                //TODO DTS
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    boolean isExists = extraAnnotations.contains(annotation.annotationType());
                    if (isExists) {
                        Method annotationMethodContainer = null;
                        try {
                            Method theMethod = annotation.annotationType().getDeclaredMethod("values");
                            if (theMethod != null)
                                annotationMethodContainer = theMethod;
                        } catch (NoSuchMethodException e) {
                        }
                        if (annotationMethodContainer != null) {
                            Annotation[] innerAnnotations = new Annotation[0];
                            try {
                                innerAnnotations = (Annotation[]) annotationMethodContainer.invoke(annotation);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                LoggerEx.warn(TAG, "(IllegalAccessException)Try to get annotations for key values in class " + annotationMethodContainer.getDeclaringClass() + " method " + annotationMethodContainer.getName() + " failed, " + ExceptionUtils.getFullStackTrace(e));
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                                LoggerEx.warn(TAG, "(InvocationTargetException)Try to get annotations for key values in class " + annotationMethodContainer.getDeclaringClass() + " method " + annotationMethodContainer.getName() + " failed, " + ExceptionUtils.getFullStackTrace(e));
                            }
                            if (innerAnnotations != null) {
                                for (Annotation innerAnnotation : innerAnnotations) {
                                    ServiceAnnotation serviceAnnotation = getServiceAnnotationFromAnnotation(innerAnnotation, method);
                                    annotationList.add(serviceAnnotation);
                                }
                            }
                        } else {
                            ServiceAnnotation serviceAnnotation = getServiceAnnotationFromAnnotation(annotation, method);
                            annotationList.add(serviceAnnotation);
                        }
                    } else {
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
        for (Method innerAnnotationMethod : innerAnnotationMethods) {
            String annotationKey = innerAnnotationMethod.getName();
            Object annotationValue = null;
            try {
                annotationValue = innerAnnotationMethod.invoke(annotation);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "(IllegalAccessException)Try to get annotation value for key " + annotationKey + " in class " + method.getDeclaringClass() + " method " + method.getName() + " failed, " + ExceptionUtils.getFullStackTrace(e));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "(InvocationTargetException)Try to get annotation value for key " + annotationKey + " in class " + method.getDeclaringClass() + " method " + method.getName() + " failed, " + ExceptionUtils.getFullStackTrace(e));
            }
            if (annotationValue != null)
                if (annotationValue.getClass().isArray()) {
                    List list = new ArrayList();
                    try {
                        String[] strs = (String[]) annotationValue;
                        for (int i = 0; i < strs.length; i++) {
                            String markParam = getGroovyRuntime().processAnnotationString(strs[i]);
                            if(markParam != null){
                                list.add(markParam);
                            }
                        }
                        annotationValue = list;
                        annotationParams.put(annotationKey, annotationValue);
                    }catch (Throwable t){
                        t.printStackTrace();
                        LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(t));
                    }
                }else if(annotationValue instanceof String){
                    annotationParams.put(annotationKey, getGroovyRuntime().processAnnotationString((String) annotationValue));
                }else {
                    annotationParams.put(annotationKey, annotationValue);
                }
        }
        serviceAnnotation.setAnnotationParams(annotationParams);
        serviceAnnotation.setClassName(method.getDeclaringClass().getSimpleName());
        serviceAnnotation.setMethodName(method.getName());
        serviceAnnotation.setType(annotation.annotationType().getSimpleName());
        if (method.getGenericReturnType().getTypeName().contains(CompletableFuture.class.getTypeName())){
            serviceAnnotation.setAsync(true);
        }else {
            serviceAnnotation.setAsync(false);
        }
        return serviceAnnotation;
    }
    @Override
    public void configService(com.docker.data.Service theService) {
        theService.appendServiceAnnotation(annotationList);
    }
}
