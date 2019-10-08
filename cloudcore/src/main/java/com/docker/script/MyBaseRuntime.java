package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.docker.annotations.*;
import com.docker.data.Lan;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.annotations.ServiceNotFound;
import com.docker.script.annotations.ServiceNotFoundListener;
import com.docker.script.i18n.I18nHandler;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.LansService;
import groovy.lang.GroovyObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.FieldInjectionListener;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.ClassHolder;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyBaseRuntime extends BaseRuntime {
	private static final String TAG = MyBaseRuntime.class.getSimpleName();
	private String remoteServiceHost;
	private ServiceStubManager serviceStubManager;
	private ConcurrentHashMap<String, ServiceStubManager> stubManagerForLanIdMap = new ConcurrentHashMap<>();

	@Autowired
	LansService lansService;
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private List<GroovyObjectEx<ServiceNotFoundListener>> serviceNotFoundListeners;

    private ReadWriteLock serviceNotFoundLock = new ReentrantReadWriteLock();

    public void resetServiceStubManager(Class<?> proxyClass) {
        if (serviceStubManager != null) {
            serviceStubManager.setServiceStubProxyClass(proxyClass);
        }
    }

    public void resetServiceStubManagerForLans(Class<?> proxyClass) {
        Collection<ServiceStubManager> managers = stubManagerForLanIdMap.values();
        for (ServiceStubManager manager : managers) {
            manager.setServiceStubProxyClass(proxyClass);
        }
    }

    public ServiceStubManager getServiceStubManager(String lanId) {
        if (StringUtils.isBlank(lanId))
            return null;
        ServiceStubManager manager = stubManagerForLanIdMap.get(lanId);
        if (manager == null) {
            if (lanId.equals(OnlineServer.getInstance().getLanId())) {
                // 本地访问
                return serviceStubManager;
            }
            synchronized (stubManagerForLanIdMap) {
                manager = stubManagerForLanIdMap.get(lanId);
                if (manager == null) {


                    if (lansService == null)
                        return null;
                    Lan lan = null;
                    try {
                        lan = lansService.getLan(lanId);
                    } catch (CoreException e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Read lan " + lanId + " information failed, " + ExceptionUtils.getFullStackTrace(e));
                    }
                    if (lan == null)
                        throw new NullPointerException("Lan is null for lanId " + lanId);
                    if (lan.getDomain() == null || lan.getPort() == null || lan.getProtocol() == null)
                        throw new NullPointerException("Lan " + lan + " is illegal for lanId " + lanId + " domain " + lan.getDomain() + " port " + lan.getPort() + " protocol " + lan.getProtocol());
                    String host = lan.getProtocol() + "://" + lan.getDomain() + ":" + lan.getPort();
                    manager = new ServiceStubManager(host, serviceStubManager.getFromService());
                    beanFactory.autowireBean(manager);
                    manager.setUsePublicDomain(true);
                    manager.setBeanFactory(beanFactory);
                    manager.setServiceStubProxyClass(serviceStubManager.getServiceStubProxyClass());
                    stubManagerForLanIdMap.putIfAbsent(lanId, manager);
                }
            }
        }
        return manager;
    }

    @Override
    public void prepare(String service, Properties properties, String localScriptPath) {
        super.prepare(service, properties, localScriptPath);
        fieldInjection(this);
        ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = new ServiceSkeletonAnnotationHandler();
        beanFactory.autowireBean(serviceSkeletonAnnotationHandler);
        serviceSkeletonAnnotationHandler.setService(getServiceName());
        serviceSkeletonAnnotationHandler.setServiceVersion(getServiceVersion());
        serviceSkeletonAnnotationHandler.addExtraAnnotation(PeriodicTask.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(OneTimeTask.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(KafkaListener.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(Summaries.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(Summary.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(Transactions.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(Transaction.class);
        serviceSkeletonAnnotationHandler.addExtraAnnotation(TransactionResultNotify.class);
        addClassAnnotationHandler(serviceSkeletonAnnotationHandler);

        final MyBaseRuntime instance = this;
        addClassAnnotationHandler(new ClassAnnotationHandler() {
            @Override
            public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime runtime) {
                return ServiceNotFound.class;
            }

            @Override
            public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                               MyGroovyClassLoader cl) {
                if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
                    StringBuilder uriLogs = new StringBuilder(
                            "\r\n---------------------------------------\r\n");

                    List<GroovyObjectEx<ServiceNotFoundListener>> newServiceNotFoundMap = new ArrayList<>();
                    Set<String> keys = annotatedClassMap.keySet();
                    GroovyRuntime groovyRuntime = instance;
                    for (String key : keys) {
                        Class<?> groovyClass = annotatedClassMap.get(key);
                        if (groovyClass != null) {
                            ServiceNotFound messageReceivedAnnotation = groovyClass.getAnnotation(ServiceNotFound.class);
                            if (messageReceivedAnnotation != null) {
                                GroovyObjectEx<ServiceNotFoundListener> messageReceivedObj = groovyRuntime
                                        .create(groovyClass);
                                if (messageReceivedObj != null) {
                                    uriLogs.append("ServiceNotFoundListener #" + groovyClass + "\r\n");
                                    newServiceNotFoundMap.add(messageReceivedObj);
                                }
                            }
                        }
                    }
                    instance.serviceNotFoundLock.writeLock().lock();
                    try {
                        instance.serviceNotFoundListeners = newServiceNotFoundMap;
                    } finally {
                        instance.serviceNotFoundLock.writeLock().unlock();
                    }
                    uriLogs.append("---------------------------------------");
                    LoggerEx.info(TAG, uriLogs.toString());
                }
            }
        });

        remoteServiceHost = properties.getProperty("remote.service.host");
        if (remoteServiceHost != null) {
            serviceStubManager = new ServiceStubManager(remoteServiceHost, service);
            beanFactory.autowireBean(serviceStubManager);
            serviceStubManager.setBeanFactory(beanFactory);
        }
        String libs = properties.getProperty("libs");
        if (libs != null) {
            String[] libArray = libs.split(",");
            for (String lib : libArray) {
                addLibPath(lib);
            }
        }
    }

    ClassHolder serviceStubProxyClass = null;

    public void prepareServiceStubProxy() {
        MyGroovyClassLoader classLoader = getClassLoader();
        if (classLoader != null && serviceStubProxyClass == null) {
            serviceStubProxyClass = classLoader.getClass("script.groovy.runtime.ServiceStubProxy");
            resetServiceStubManagerForLans(serviceStubProxyClass.getParsedClass());
            resetServiceStubManager(serviceStubProxyClass.getParsedClass());
        }
    }

    public void beforeDeploy() {
        if (remoteServiceHost != null) {
            String code =
                    "package script.groovy.runtime\n" +
                            "@script.groovy.annotation.RedeployMain\n" +
                            "class ServiceStubProxy extends com.docker.rpc.remote.stub.Proxy implements GroovyInterceptable{\n" +
                            "    private Class<?> remoteServiceStub;\n" +
                            "    private com.docker.rpc.remote.stub.RpcCacheManager rpcCacheManager;\n" +
                            "    ServiceStubProxy() {\n" +

                            "        super(null, null, null);\n" +
                            "    }\n" +
                            "    ServiceStubProxy(Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager, org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory, com.docker.rpc.remote.stub.RemoteServerHandler remoteServerHandler, com.docker.rpc.remote.stub.RpcCacheManager rpcCacheManager) {\n" +
                            "        super(beanFactory, serviceStubManager, remoteServerHandler)\n" +
                            "        this.remoteServiceStub = remoteServiceStub;\n" +
                            "        this.rpcCacheManager = rpcCacheManager;\n" +
                            "    }\n" +
                            "    def methodMissing(String methodName,methodArgs) {\n" +
                            "        Long crc = chat.utils.ReflectionUtil.getCrc(remoteServiceStub, methodName, remoteServerHandler.getToService());\n" +
                            "        this.rpcCacheManager.putCrcMethodMap(crc, remoteServerHandler.getToService() + '_' + remoteServiceStub.getSimpleName() + '_' + methodName);\n" +
                            "        return invoke(crc, methodArgs);\n" +
                            "    }\n" +
                            "    public static def getProxy(Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager, org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory, com.docker.rpc.remote.stub.RemoteServerHandler remoteServerHandler, com.docker.rpc.remote.stub.RpcCacheManager rpcCacheManager) {\n" +
                            "        ServiceStubProxy proxy = new ServiceStubProxy(remoteServiceStub, serviceStubManager, beanFactory, remoteServerHandler, rpcCacheManager)\n" +
                            "        def theProxy = proxy.asType(proxy.remoteServiceStub)\n" +
                            "        return theProxy\n" +
                            "    }\n" +
                            "    public void main() {\n" +
                            "        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(this.getClass().getClassLoader());\n" +
                            "        baseRuntime.prepareServiceStubProxy();" +
                            "    }\n" +
                            "    public void shutdown(){}\n" +
                            "}";
            try {
                FileUtils.writeStringToFile(new File(path + "/script/groovy/runtime/ServiceStubProxy.groovy"), code, "utf8");
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "write ServiceStubProxy.groovy file on " + (path + "/script/groovy/runtime/ServiceStubProxy.groovy") + " in service " + getService() + " failed, " + ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (serviceNotFoundListeners != null) {
            serviceNotFoundListeners.clear();
        }
    }

    public ServiceStubManager getServiceStubManager() {
        return serviceStubManager;
    }

    public void setServiceStubManager(ServiceStubManager serviceStubManager) {
        this.serviceStubManager = serviceStubManager;
    }

    private void fieldInjection(MyBaseRuntime baseRuntime) {
        baseRuntime.addFieldInjectionListener(new FieldInjectionListener<ServiceBean>() {
            public Class<ServiceBean> annotationClass() {
                return ServiceBean.class;
            }

            @Override
            public void inject(ServiceBean annotation, Field field, Object obj) {
                String serviceName = processAnnotationString(annotation.name());
                String lanId = processAnnotationString(annotation.lanId());
                if (!StringUtils.isBlank(serviceName)) {
                    baseRuntime.prepareServiceStubProxy();
                    Object serviceStub = null;
                    if (StringUtils.isBlank(lanId)) {
                        serviceStub = baseRuntime.getServiceStubManager().getService(serviceName, field.getType());
                    }else {
                        serviceStub = baseRuntime.getServiceStubManager(lanId).getService(serviceName, field.getType());
                    }
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    try {
                        field.set(obj, serviceStub);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Set field " + field.getName() + " for service " + serviceName + " class " + field.getType() + " in class " + obj.getClass());
                    }
                }
            }
        });


        baseRuntime.addFieldInjectionListener(new FieldInjectionListener<ConfigProperty>() {
            public Class<ConfigProperty> annotationClass() {
                return ConfigProperty.class;
            }

            @Override
            public void inject(ConfigProperty annotation, Field field, Object obj) {
                String key = processAnnotationString(annotation.name());
                if (!StringUtils.isBlank(key)) {
                    Properties properties = baseRuntime.getConfig();
                    if (properties == null)
                        return;
                    String value = properties.getProperty(key);
                    if (value == null)
                        return;
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    try {
                        field.set(obj, TypeUtils.cast(value, field.getType(), ParserConfig.getGlobalInstance()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Set field " + field.getName() + " for config key " + key + " class " + field.getType() + " in class " + obj.getClass());
                    }
                }
            }
        });

        baseRuntime.addFieldInjectionListener(new FieldInjectionListener<I18nBean>() {
            public Class<I18nBean> annotationClass() {
                return I18nBean.class;
            }

            @Override
            public void inject(I18nBean annotation, Field field, Object obj) {
                I18nHandler i18nHandler = baseRuntime.getI18nHandler();
                if (!field.isAccessible())
                    field.setAccessible(true);
                try {
                    field.set(obj, TypeUtils.cast(i18nHandler, field.getType(), ParserConfig.getGlobalInstance()));
                } catch (Throwable e) {
                    e.printStackTrace();
                    LoggerEx.error(TAG, "Set field " + field.getName() + " for i18nhandler key " + i18nHandler + " class " + field.getType() + " in class " + obj.getClass());
                }
            }
        });
    }

    public BaseRuntime getRuntimeWhenNotFound(String service) {
        serviceNotFoundLock.readLock().lock();
        try {
            if (serviceNotFoundListeners != null) {
                for (GroovyObjectEx<ServiceNotFoundListener> listener : serviceNotFoundListeners) {
                    try {
                        return listener.getObject().getRuntimeWhenNotFound(service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle getRuntime service " + service + " failed, " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        } finally {
            serviceNotFoundLock.readLock().unlock();
        }
        return null;
    }

    public String getRemoteServiceHost() {
        return remoteServiceHost;
    }

    public void injectBean(Object obj) {
        if (obj instanceof GroovyObject) {
            try {
                GroovyObjectEx.fillGroovyObject((GroovyObject) obj, this);
            } catch (IllegalAccessException e) {
                LoggerEx.error(TAG, "fillGroovyObject " + obj + " failed, " + ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        }
    }
}
