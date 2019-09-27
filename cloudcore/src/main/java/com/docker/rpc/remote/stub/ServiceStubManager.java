package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.remote.MethodMapping;
import scala.annotation.meta.field;
import script.groovy.servlets.Tracker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStubManager {
    private static final String TAG = ServiceStubManager.class.getSimpleName();
    private ConcurrentHashMap<Class<?>, Boolean> classScanedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, MethodMapping> methodMap = new ConcurrentHashMap<>();
    private String clientTrustJksPath;
    private String serverJksPath;
    private String jksPwd;
    private String host;
    private boolean inited = false;
    private Class<?> serviceStubProxyClass;
    private Boolean usePublicDomain = false;
    private RPCClientAdapterMap clientAdapterMap;
    private RemoteServersDiscovery remoteServersDiscovery;

    /**
     *
     */
    private String fromService;

    public ServiceStubManager() {
    }

    public ServiceStubManager(String fromService) {
        this.fromService = fromService;
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
        if(!classScanedMap.containsKey(clazz)) {
            try {
                Field field = clazz.getField("SERVICE");
                field.get(clazz);
            } catch (Throwable t) {
                t.printStackTrace();
                try {
                    Field field = clazz.getField("service");
                    field.get(clazz);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.error(TAG, "The service has no field: SERVICE, please check!!!" + "class: " + clazz.getSimpleName());
                    return;
                }
            }
            classScanedMap.put(clazz, true);
        } else {
            return;
        }
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if (methods != null) {
            for (Method method : methods) {
                MethodMapping mm = new MethodMapping(method);
                long value = ReflectionUtil.getCrc(method, service);
                if (methodMap.containsKey(value)) {
                    LoggerEx.fatal(TAG, "Don't support override methods, please rename your method " + method + " for crc " + value + " and existing method " + methodMap.get(value).getMethod());
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
                if(method.getGenericReturnType() instanceof ParameterizedType){
                    Type[] tArgs = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
                    mm.setGenericReturnActualTypeArguments(tArgs);
                }

                if(method.getGenericReturnType().getTypeName().contains(CompletableFuture.class.getTypeName())){
                    mm.setAsync(true);
                }else {
                    mm.setAsync(false);
                }
                methodMap.put(value, mm);
//                RemoteProxy.cacheMethodCrc(method, value);
                LoggerEx.info("SCAN", "Mapping crc " + value + " for class " + clazz.getName() + " method " + method.getName() + " for service " + service);
            }
        }
    }

    public synchronized void init() {
        if (inited)
            return;
        if (host == null) {
            throw new NullPointerException("Discovery host is null, ServiceStubManager initialize failed!");
        }
        if (!host.startsWith("http")) {
            host = "http://" + host;
        }
        RefreshServers.getInstance().addRemoteHost(host);
        this.clientAdapterMap = new RPCClientAdapterMap();
        if (clientTrustJksPath != null && serverJksPath != null && jksPwd != null) {
            this.clientAdapterMap.setEnableSsl(true);
            this.clientAdapterMap.setRpcSslClientTrustJksPath(clientTrustJksPath);
            this.clientAdapterMap.setRpcSslJksPwd(jksPwd);
            this.clientAdapterMap.setRpcSslServerJksPath(serverJksPath);
        }
        this.remoteServersDiscovery = new RemoteServersDiscovery(host);
        inited = true;
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
        CompletableFuture<?> future = new RemoteServerHandler(service, this).callAsync(getMethodRequest(service, className, method, args));
        return future;
    }

    public Object call(String service, String className, String method, Object... args) throws CoreException {
        MethodRequest request = getMethodRequest(service, className, method, args);
        MethodResponse response = new RemoteServerHandler(service, this).call(request);
        return Proxy.getReturnObject(request, response);
    }

    public <T> T getService(String service, Class<T> adapterClass) {

        return getService(service, adapterClass, 1);
    }

    public <T> T getService(String service, Class<T> adapterClass, Integer version) {
        if (host == null)
            throw new NullPointerException("Discovery host is null, ServiceStubManager initialize failed!");
//        if(!inited)
//            throw new NullPointerException("ServiceSubManager hasn't been initialized yet, please call init method first.");
        if (service == null)
            throw new NullPointerException("Service can not be nulll");
        T adapterService = null;
        //TODO should cache adapterService. class as Key, value is adapterService,every class -> adaService
        scanClass(adapterClass, service);
        if (serviceStubProxyClass != null) {
            try {
                Method getProxyMethod = serviceStubProxyClass.getMethod("getProxy", RemoteServerHandler.class, Class.class, ServiceStubManager.class);
                if (getProxyMethod != null) {
                    //远程service
                    adapterService = (T) getProxyMethod.invoke(null, new RemoteServerHandler(service, this), adapterClass, this);
                } else {
                    LoggerEx.error(TAG, "getProxy method doesn't be found for " + adapterClass + " in service " + service);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "Generate proxy object for " + adapterClass + " in service " + service + " failed, " + t.getMessage());
            }

        } else {
            try {
                RemoteProxy proxy = new RemoteProxy(new RemoteServerHandler(service, this), this);
                adapterService = (T) proxy.getProxy(adapterClass);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.warn(TAG, "Initiate moduleClass " + adapterClass + " failed, " + e.getMessage());
            }
        }
        return adapterService;
    }

    public String getClientTrustJksPath() {
        return clientTrustJksPath;
    }

    public void setClientTrustJksPath(String clientTrustJksPath) {
        this.clientTrustJksPath = clientTrustJksPath;
    }

    public String getServerJksPath() {
        return serverJksPath;
    }

    public void setServerJksPath(String serverJksPath) {
        this.serverJksPath = serverJksPath;
    }

    public String getJksPwd() {
        return jksPwd;
    }

    public void setJksPwd(String jksPwd) {
        this.jksPwd = jksPwd;
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

    public RPCClientAdapterMap getClientAdapterMap() {
        return clientAdapterMap;
    }

    public void setClientAdapterMap(RPCClientAdapterMap clientAdapterMap) {
        this.clientAdapterMap = clientAdapterMap;
    }

    public RemoteServersDiscovery getRemoteServersDiscovery() {
        return remoteServersDiscovery;
    }

    public void setRemoteServersDiscovery(RemoteServersDiscovery remoteServersDiscovery) {
        this.remoteServersDiscovery = remoteServersDiscovery;
    }
}
