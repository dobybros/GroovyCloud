package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.script.i18n.I18nHandler;
import com.docker.script.i18n.MessageProperties;
import com.docker.script.servlet.GroovyServletManagerEx;
import com.docker.script.servlet.WebServiceAnnotationHandler;
import com.docker.storage.cache.CacheAnnotationHandler;
import com.docker.storage.cache.CacheStorageFactory;
import com.docker.storage.cache.CacheStorageMethod;
import com.docker.storage.cache.handlers.EhCacheCacheStorageHandler;
import com.docker.storage.cache.handlers.RedisCacheStorageHandler;
import com.docker.storage.ehcache.EhCacheHandler;
import com.docker.storage.kafka.KafkaConfCenter;
import com.docker.storage.kafka.KafkaProducerHandler;
import com.docker.storage.redis.RedisHandler;
import com.docker.utils.SpringContextUtil;
import connectors.mongodb.MongoClientHelper;
import connectors.mongodb.annotations.handlers.MongoCollectionAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDatabaseAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDocumentAnnotationHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.filter.JsonFilterFactory;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.*;
import script.groovy.servlets.GroovyServletDispatcher;
import script.groovy.servlets.RequestPermissionHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseRuntime extends GroovyRuntime {
    public static final String TAG = BaseRuntime.class.getSimpleName();
    private ConcurrentHashMap<String, Object> memoryCache = new ConcurrentHashMap<>();

	private MongoDBHandler mongoDBHandler;
	//	private RedisHandler redisHandler;
	private String redisHost;
	private EhCacheHandler ehCacheHandler;
	private KafkaProducerHandler kafkaProducerHandler;
	private KafkaConfCenter kafkaConfCenter;
	private I18nHandler i18nHandler;

    private String service;

    private String serviceName;
    private Integer serviceVersion;

	private Properties config;

	public void prepare(String service, Properties properties, String rootPath) {
		LoggerEx.info(TAG, "prepare service: " + service + " properties: " + properties + " rootPath: " + rootPath);
		this.service = service.toLowerCase();
		this.config = properties;
		String enableGroovyMVC = null;
		addClassAnnotationHandler(new GroovyBeanFactory());
		if (properties != null) {
			Object rpcServerHandler = SpringContextUtil.getBean("dockerRpcServer");
			if (rpcServerHandler != null && rpcServerHandler instanceof ClassAnnotationHandler)
				addClassAnnotationHandler((ClassAnnotationHandler) rpcServerHandler);
			Object rpcServerSslHandler = SpringContextUtil.getBean("dockerRpcServerSsl");
			if (rpcServerSslHandler != null && rpcServerSslHandler instanceof ClassAnnotationHandler)
				addClassAnnotationHandler((ClassAnnotationHandler) rpcServerSslHandler);
			Object upStreamAnnotationHandler = SpringContextUtil.getBean("upStreamAnnotationHandler");
			if (upStreamAnnotationHandler != null && upStreamAnnotationHandler instanceof ClassAnnotationHandler)
				addClassAnnotationHandler((ClassAnnotationHandler) upStreamAnnotationHandler);

			enableGroovyMVC = properties.getProperty("web.groovymvc.enable");
			String mongodbHost = properties.getProperty("db.mongodb.uri");
			if (mongodbHost != null) {
				addClassAnnotationHandler(new MongoDatabaseAnnotationHolder());
				addClassAnnotationHandler(new MongoCollectionAnnotationHolder());
				addClassAnnotationHandler(new MongoDocumentAnnotationHolder());
				mongoDBHandler = new MongoDBHandler();
				MongoClientHelper helper = new MongoClientHelper();
				helper.setHosts(mongodbHost);
				mongoDBHandler.setMongoClientHelper(helper);
				addClassAnnotationHandler(mongoDBHandler);
			}
			this.redisHost = properties.getProperty("db.redis.uri");
//			this.redisHandler = this.getRedisHandler();
			EhCacheCacheStorageHandler ehCacheCacheStorageHandler = (EhCacheCacheStorageHandler) CacheStorageFactory.getInstance().getCacheStorageAdapter(CacheStorageMethod.METHOD_EHCACHE, null);
			ehCacheHandler = ehCacheCacheStorageHandler.getEhCacheHandler();
			String produce = properties.getProperty("db.kafka.produce");
			kafkaConfCenter = new KafkaConfCenter();
			kafkaConfCenter.filterKafkaConf(properties, KafkaConfCenter.FIELD_PRODUCE, KafkaConfCenter.FIELD_CONSUMER);
			if (produce != null) {
				kafkaProducerHandler = new KafkaProducerHandler(kafkaConfCenter);
				kafkaProducerHandler.connect();
			}
			String i18nFolder = properties.getProperty("i18n.folder");
			String name = properties.getProperty("i18n.name");
			if (i18nFolder != null && name != null) {
				i18nHandler = new I18nHandler();
				File messageFile = new File(rootPath + i18nFolder);
				if (messageFile != null) {
					File[] files = messageFile.listFiles();
					if (files != null) {
						for (File file : files) {
							String fileName = file.getName();
							fileName = fileName.replace(name + "_", "");
							fileName = fileName.replace(".properties", "");
							MessageProperties messageProperties = new MessageProperties();
							messageProperties.setAbsolutePath(file.getAbsolutePath());
							try {
								messageProperties.init();
							} catch (IOException e) {
								e.printStackTrace();
							}
							i18nHandler.getMsgPropertyMap().put(fileName, messageProperties);
						}
					}
				}
			}
		}

        if (enableGroovyMVC != null && enableGroovyMVC.trim().equals("true")) {
            GroovyServletManagerEx servletManagerEx = new GroovyServletManagerEx(this.serviceName, this.serviceVersion);
            addClassAnnotationHandler(servletManagerEx);
            GroovyServletDispatcher.addGroovyServletManagerEx(this.service, servletManagerEx);
            addClassAnnotationHandler(new WebServiceAnnotationHandler());
        } else {
            GroovyServletDispatcher.removeGroovyServletManagerEx(this.service);
        }



		addClassAnnotationHandler(new GroovyTimerTaskHandler());
		addClassAnnotationHandler(new GroovyRedeployMainHandler());
		addClassAnnotationHandler(new ServerLifeCircleHandler());
		addClassAnnotationHandler(new JsonFilterFactory());
		addClassAnnotationHandler(new RequestPermissionHandler());
		addClassAnnotationHandler(new CacheAnnotationHandler());
		addClassAnnotationHandler(new ServiceMemoryHandler());

	}

	@Override
    public void close() {
	    if(service != null) {
			GroovyServletDispatcher.removeGroovyServletManagerEx(service.toLowerCase());
		}
		try {
			if(mongoDBHandler != null) {
				MongoClientHelper helper = mongoDBHandler.getMongoClientHelper();
				if(helper != null) {
					helper.disconnect();
				}
			}
		} catch(Throwable t) {
		    LoggerEx.error(TAG, "Close mongo error, errMsg: " + ExceptionUtils.getFullStackTrace(t));
		}
		try {
			if (redisHost != null) {
				CacheStorageFactory.getInstance().removeCacheStorageAdapter(CacheStorageMethod.METHOD_REDIS, redisHost);
			}
		} catch(Throwable t) {
            LoggerEx.error(TAG, "Close redis error, errMsg: " + ExceptionUtils.getFullStackTrace(t));
		}
		super.close();
		clear();
	}

    @Override
    public String processAnnotationString(String markParam) {
        if(StringUtils.isNotBlank(markParam)){
            if (markParam.startsWith("#{") && markParam.endsWith("}")) {
                markParam = markParam.replaceAll(" ", "");
                String[] markParams = markParam.split("#\\{");
                if (markParams.length == 2) {
                    markParam = markParams[1];
                    markParams = markParam.split("}");
                    if (markParams.length == 1) {
                        markParam = markParams[0];
                        markParam = getConfig().getProperty(markParam);
                    }
                }
            }
        }
        return markParam;
    }

    public Object executeBeanMethod(Object caller, String name, Object... args) throws CoreException, InvocationTargetException, IllegalAccessException {
        GroovyBeanFactory beanFactory = (GroovyBeanFactory) getClassAnnotationHandler(GroovyBeanFactory.class);
        if (beanFactory != null) {
            script.groovy.object.GroovyObjectEx objectEx = beanFactory.getBean(caller.getClass());
            if (objectEx == null)
                return null;
            Object obj = objectEx.getObject();
            if (obj != null) {
                Class<?>[] argClasses = null;
                if (args != null && args.length > 0) {
                    argClasses = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        argClasses[i] = args[i].getClass();
                    }
                }
                Method method = null;
                try {
                    method = obj.getClass().getMethod(name, argClasses);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    LoggerEx.error(TAG, "NoSuchMethod while executeBeanMethod " + name + " args " + Arrays.toString(args));
                }
                if (method != null) {
                    return method.invoke(obj, args);
                }
            }
        }
        return null;
    }

    public <T> T getBean(Class<T> beanClass) {
        GroovyObjectEx<T> groovyObjectEx = getBeanFactory().getBean(beanClass);
        if (groovyObjectEx == null) return null;

        try {
            return groovyObjectEx.getObject();
        } catch (CoreException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "getBean failed for class " + beanClass + " error " + ExceptionUtils.getFullStackTrace(e));
            return null;
        }
    }

    public MongoDBHandler getMongoDBHandler() {
        return mongoDBHandler;
    }

	public RedisHandler getRedisHandler() {
		if (redisHost != null) {
			RedisCacheStorageHandler cacheStorageAdapter = (RedisCacheStorageHandler) CacheStorageFactory.getInstance().getCacheStorageAdapter(CacheStorageMethod.METHOD_REDIS, redisHost);
			return cacheStorageAdapter.getRedisHandler();
		}
		return null;
	}

    public KafkaProducerHandler getKafkaProducerHandler() {
        return kafkaProducerHandler;
    }

    public KafkaConfCenter getKafkaConfCenter() {
        return kafkaConfCenter;
    }

    public I18nHandler getI18nHandler() {
        return i18nHandler;
    }

    public Object get(String key) {
        return memoryCache.get(key);
    }

    public Object put(String key, Object value) {
        return memoryCache.put(key, value);
    }

    public ConcurrentHashMap<String, Object> getMemoryCache() {
        return memoryCache;
    }

    public Object remove(String key) {
        return memoryCache.remove(key);
    }

    public void clear() {
        memoryCache.clear();
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Properties getConfig() {
        return config;
    }

    public String getCacheHost(String cacheMethod) {
        if (StringUtils.isBlank(cacheMethod)) {
            cacheMethod = CacheStorageMethod.METHOD_REDIS;
        }
        if (CacheStorageMethod.METHOD_REDIS.equals(cacheMethod)) {
            Object cacheRedisUri = getConfig().get("cache.redis.uri");
            if (cacheRedisUri == null) {
                return null;
            }
            return (String) cacheRedisUri;
        }
        return null;
    }

    public EhCacheHandler getEhCacheHandler() {
        return ehCacheHandler;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getServiceVersion() {
        return serviceVersion;
    }

	public void setServiceVersion(Integer serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

}
