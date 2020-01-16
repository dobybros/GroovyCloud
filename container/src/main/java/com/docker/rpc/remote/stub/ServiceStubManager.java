package com.docker.rpc.remote.stub;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.data.Lan;
import com.docker.errors.CoreErrorCodes;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.remote.MethodMapping;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.ServiceVersionServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.DockerStatusDAO;
import com.docker.storage.mongodb.daos.ServiceVersionDAO;
import com.docker.utils.GroovyCloudBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;
import script.groovy.servlets.Tracker;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStubManager {
    private static final String TAG = ServiceStubManager.class.getSimpleName();
    private ConcurrentHashMap<String, Boolean> classScanedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, MethodMapping> methodMap = new ConcurrentHashMap<>();
    private String host;
    private Class<?> serviceStubProxyClass;
    //sure is ssl
    private Boolean usePublicDomain = false;
    private String fromService;
    private Integer lanType;
    public ServiceStubManager(){

    }
    public ServiceStubManager(String fromService) {
        if (fromService != null) {
            this.fromService = fromService;
        }
    }
    public ServiceStubManager(String host, String fromService) {
        if (fromService != null) {
            this.fromService = fromService;
        }
        this.host = host;
    }
    public void init(){
        LoggerEx.info(TAG, "ServiceStubManager will init");
        if(this.lanType != null && this.lanType.equals(Lan.TYPE_http)){
            if (this.host == null) {
                throw new NullPointerException("Discovery host is null, ServiceStubManager initialize failed!");
            }
            if (!this.host.startsWith("http")) {
                this.host = "http://" + this.host;
            }
            RemoteServersManager.getInstance().addCrossHost(this.host);
        }
        handle();
    }
    public void clearCache() {
        methodMap.clear();
    }

    public MethodMapping getMethodMapping(Long crc) {
        return methodMap.get(crc);
    }

    public void scanClass(Class<?> clazz, String service) {
        if (clazz == null || service == null)
            return;
//        if(service == null) {
//            String[] paths = clazz.getName().split(".");
//            if(paths.length >= 2) {
//                service = paths[paths.length - 2];
//            }
//        }
        if (!classScanedMap.containsKey(clazz.getName() + "_" + service)) {
            try {
                Field field = clazz.getField("SERVICE");
                field.get(clazz);
            } catch (Throwable t) {
                try {
                    Field field = clazz.getField("service");
                    field.get(clazz);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.error(TAG, "The service has no field: SERVICE, please check!!!" + "class: " + clazz.getSimpleName());
                    return;
                }
            }
            classScanedMap.put(clazz.getName() + "_" + service, true);
        } else {
            return;
        }
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if (methods != null) {
            for (Method method : methods) {
                MethodMapping mm = new MethodMapping(method);
                long value = ReflectionUtil.getCrc(method, service);
                if (methodMap.containsKey(value)) {
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
                            LoggerEx.fatal(TAG, "Parameter " + parameterType + " in method " + method + " couldn't be initialized. ");
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
                mm.setGenericReturnClass(method.getGenericReturnType());
                if (method.getGenericReturnType() instanceof ParameterizedType) {
                    Type[] tArgs = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
                    mm.setGenericReturnActualTypeArguments(tArgs);
                }

                if (method.getGenericReturnType().getTypeName().contains(CompletableFuture.class.getTypeName())) {
                    mm.setAsync(true);
                } else {
                    mm.setAsync(false);
                }
                methodMap.put(value, mm);
//                RemoteProxy.cacheMethodCrc(method, value);
                LoggerEx.info("SCAN", "Mapping crc " + value + " for class " + clazz.getName() + " method " + method.getName() + " for service " + service);
            }
        }
    }

    private MethodRequest getMethodRequest(String service, String className, String method, Object[] args) {
        Long crc = ReflectionUtil.getCrc(className, method, service);
        MethodRequest request = new MethodRequest();
        request.setEncode(MethodRequest.ENCODE_JAVABINARY);
        request.setArgs(args);
        //TODO should consider how to optimize get CRC too often.

        request.setCrc(crc);
        Tracker tracker = Tracker.trackerThreadLocal.get();
        request.setTrackId(tracker == null ? null : tracker.getTrackId());
        request.setServiceStubManager(this);
        return request;
    }

    public CompletableFuture<?> callAsync(String service, String className, String method, Object... args) throws CoreException {
        MethodRequest request = getMethodRequest(service, className, method, args);
        AsyncRpcFuture asyncRpcFuture = new AsyncRpcFuture(ReflectionUtil.getCrc(className, method, service), null);
        RemoteServerHandler remoteServerHandler = getRemoteServerHandler(service);
        remoteServerHandler.setCallbackFutureId(asyncRpcFuture.getCallbackFutureId());
        RpcCacheManager.getInstance().pushToAsyncRpcMap(asyncRpcFuture.getCallbackFutureId(), asyncRpcFuture);
        LoggerEx.info(TAG, "pushToAsyncRpcMap success, callbackFutureId: " + asyncRpcFuture.getCallbackFutureId() + ",CurrentThread: " + Thread.currentThread() + ",asyncFuture:" + RpcCacheManager.getInstance().getAsyncRpcFuture(asyncRpcFuture.getCallbackFutureId()));
        return remoteServerHandler.callAsync(request);
    }

    public Object call(String service, String className, String method, Object... args) throws CoreException {
        MethodRequest request = getMethodRequest(service, className, method, args);
        MethodResponse response = getRemoteServerHandler(service).call(request);
        return Proxy.getReturnObject(request, response);
    }

    public <T> T getService(String service, Class<T> adapterClass) {

        return getService(service, adapterClass, 1);
    }

    public <T> T getService(String service, Class<T> adapterClass, Integer version) {
        if (service == null)
            throw new NullPointerException("Service can not be nulll");
        T adapterService = null;
        //TODO should cache adapterService. class as Key, value is adapterService,every class -> adaService
        scanClass(adapterClass, service);
        if (serviceStubProxyClass != null) {
            try {
                Method getProxyMethod = serviceStubProxyClass.getMethod("getProxy", Class.class, ServiceStubManager.class, RemoteServerHandler.class);
                if (getProxyMethod != null) {
                    //远程service
                    adapterService = (T) getProxyMethod.invoke(null, adapterClass, this, getRemoteServerHandler(service));
                } else {
                    LoggerEx.error(TAG, "getProxy method doesn't be found for " + adapterClass + " in service " + service);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "Generate proxy object for " + adapterClass + " in service " + service + " failed, " + ExceptionUtils.getFullStackTrace(t));
            }

        } else {
            try {
                RemoteProxy proxy = new RemoteProxy(this, getRemoteServerHandler(service));
                adapterService = (T) proxy.getProxy(adapterClass);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "Initiate moduleClass " + adapterClass + " failed, " + ExceptionUtils.getFullStackTrace(e));
            }
        }
        return adapterService;
    }
    private RemoteServerHandler getRemoteServerHandler(String service){
        RemoteServerHandler remoteServerHandler = new RemoteServerHandler(service, this);
        return remoteServerHandler;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Class<?> getServiceStubProxyClass() {
        return serviceStubProxyClass;
    }

    public void setServiceStubProxyClass(Class<?> serviceStubProxyClass) {
        this.serviceStubProxyClass = serviceStubProxyClass;
    }

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public Boolean getUsePublicDomain() {
        return usePublicDomain;
    }

    public void setUsePublicDomain(Boolean usePublicDomain) {
        this.usePublicDomain = usePublicDomain;
    }

    public Integer getLanType() {
        return lanType;
    }

    public void setLanType(Integer lanType) {
        this.lanType = lanType;
    }
    private void handle(){
        LoggerEx.info(TAG, "RemoteServersManager.instance: " + (RemoteServersManager.getRemoteServersManager() == null));
        if(RemoteServersManager.getRemoteServersManager() == null){
            ServiceVersionServiceImpl serviceVersionService = (ServiceVersionServiceImpl) GroovyCloudBean.getBean(GroovyCloudBean.SERVICEVERSIONSERVICE);
            DockerStatusServiceImpl dockerStatusService = (DockerStatusServiceImpl)GroovyCloudBean.getBean(GroovyCloudBean.DOCKERSTATUSSERVICE);
            if(serviceVersionService == null || dockerStatusService == null){
                ClassPathResource configResource = new ClassPathResource("groovycloud.properties");
                Properties properties = new Properties();
                try {
                    properties.load(configResource.getInputStream());
                    String mongoHost = properties.getProperty("database.host");
                    LoggerEx.info(TAG, "Groovycloud.properties, mongoHost: " + mongoHost);
                    if(mongoHost == null){
                        LoggerEx.error(TAG, "Cant find config:database.host");
                        throw new CoreException(CoreErrorCodes.ERROR_GROOVYCLOUDCONFIG_ILLEGAL, "Cant find config:database.host");
                    }
                    MongoHelper mongoHelper = new MongoHelper();
                    mongoHelper.setHost(mongoHost);
                    mongoHelper.setConnectionsPerHost(100);
                    mongoHelper.setDbName("dockerdb");
                    mongoHelper.init();
                    if(serviceVersionService == null){
                        serviceVersionService = new ServiceVersionServiceImpl();
                        ServiceVersionDAO serviceVersionDAO = new ServiceVersionDAO();
                        serviceVersionDAO.setMongoHelper(mongoHelper);
                        serviceVersionDAO.init();
                        serviceVersionService.setServiceVersionDAO(serviceVersionDAO);
                    }
                    if(dockerStatusService == null){
                        dockerStatusService = new DockerStatusServiceImpl();
                        DockerStatusDAO dockerStatusDAO = new DockerStatusDAO();
                        dockerStatusDAO.setMongoHelper(mongoHelper);
                        dockerStatusDAO.init();
                        dockerStatusService.setDockerStatusDAO(dockerStatusDAO);
                    }
                }catch (Throwable t){
                    LoggerEx.error(TAG, "Get groovycloud.properties err, errMsg : " + ExceptionUtils.getFullStackTrace(t));
                }finally {
                    try {
                        configResource.getInputStream().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(serviceVersionService != null && dockerStatusService != null){
                RemoteServersManager.getInstance(serviceVersionService, dockerStatusService).init();
                LoggerEx.info(TAG, "RemoteServersManager init success");
            }else {
                LoggerEx.error(TAG, "serviceVersionService or dockerStatusService is null, cant init RemoteServersManager");
            }
        }
    }
}
