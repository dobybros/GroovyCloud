package sdockerboot.boot;//package sdockerboot.sdockerboot;

import chat.utils.IPHolder;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.http.MyHttpParameters;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.script.ScriptManager;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.DockerStatusDAO;
import com.docker.storage.mongodb.daos.LansDAO;
import com.docker.storage.mongodb.daos.SDockerDAO;
import com.docker.storage.mongodb.daos.ServersDAO;
import com.docker.utils.SpringContextUtil;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by lick on 2019/5/9.
 * Description：
 */
//@Component
@Configuration
//@PropertySource({"classpath:config/server_params.properties", "classpath:config/database.properties"})
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private String mongoHost;
    private String mongoConnectionsPerHost;
    private String dbName;
    private String mongoUsername;
    private String mongoPassword;

    private String gridHost;
    private String girdConnectionsPerHost;
    private String gridDbName;
    private String gridUsername;
    private String gridPassword;

    private String ipPrefix;
    private String ethPrefix;
    private String oauthPath;
    private String serverType;
    private String internalKey;
    private String rpcPort;
    private String sslRpcPort;
    private String publicDomain;
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;
    private String localPath;
    private String remotePath;
    private String runtimeBootClass;
    private String serverPort;

    MyBeanDefinitionRegistryPostProcessor(){
        InputStream inStream = MyBeanDefinitionRegistryPostProcessor.class.getClassLoader().getResourceAsStream("sdocker.properties");
        InputStream appInStream = MyBeanDefinitionRegistryPostProcessor.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        Properties apppProp = new Properties();
        try {
            prop.load(inStream);
            mongoHost = prop.getProperty("database.host");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            dbName = prop.getProperty("dockerstatus.dbname");
            mongoUsername = prop.getProperty("mongo.username");
            mongoPassword = prop.getProperty("mongo.password");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            gridHost = prop.getProperty("gridfs.host");
            girdConnectionsPerHost = prop.getProperty("gridfs.connectionsPerHost");
            gridDbName = prop.getProperty("gridfs.files.dbname");
            gridUsername = prop.getProperty("gridfs.username");
            gridPassword = prop.getProperty("gridfs.password");
            ipPrefix = prop.getProperty("server.ip.prefix");
            ethPrefix = prop.getProperty("server.eth.prefix");
            oauthPath = prop.getProperty("oauth.path");
            serverType = prop.getProperty("server.type");
            internalKey = prop.getProperty("internal.key");
            rpcPort = prop.getProperty("rpc.port");
            sslRpcPort = prop.getProperty("rpc.sslport");
            publicDomain = prop.getProperty("public.domain");
            rpcSslClientTrustJksPath = prop.getProperty("rpc.ssl.clientTrust.jks.path");
            rpcSslServerJksPath = prop.getProperty("rpc.ssl.server.jks.path");
            rpcSslJksPwd = prop.getProperty("rpc.ssl.jks.pwd");
            localPath = prop.getProperty("script.local.path");
            remotePath = prop.getProperty("script.remote.path");
            runtimeBootClass = prop.getProperty("runtimeBootClass");
            apppProp.load(appInStream);
            serverPort = apppProp.getProperty("server.port");
            System.setProperty("server.port", serverPort);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //bean名称生成器
    private BeanNameGenerator beanNameGenerator =new AnnotationBeanNameGenerator();
    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        registerBean(beanDefinitionRegistry, "springContextUtil", SpringContextUtil.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "dockerStatusHelper", MongoHelper.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "configHelper", MongoHelper.class, null, null, null, null);
        Map configHelperRefMap = new HashMap();
        configHelperRefMap.put("mongoHelper", "configHelper");
        registerBean(beanDefinitionRegistry, "serversDAO", ServersDAO.class, null, configHelperRefMap, null, null);
        registerBean(beanDefinitionRegistry, "lansDAO", LansDAO.class, null, configHelperRefMap, null, null);
        registerBean(beanDefinitionRegistry, "sdockerDAO", SDockerDAO.class, null, configHelperRefMap, null, null);
        Map dockerStatusDAORefMap = new HashMap();
        dockerStatusDAORefMap.put("mongoHelper", "dockerStatusHelper");
        registerBean(beanDefinitionRegistry, "dockerStatusDAO", DockerStatusDAO.class, null, dockerStatusDAORefMap, null, null);

        registerBean(beanDefinitionRegistry, "plainSocketFactory", PlainSocketFactory.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "sslSocketFactory", SSLSocketFactory.class, null, null, null, null);
        List<Map> httpSchemeContrutorList = new ArrayList();
        Map httpSchemeMap = new HashMap();
        httpSchemeMap.put("value", "http");
        httpSchemeMap.put("type", "String");
        Map httpPortSchemeMap = new HashMap();
        httpPortSchemeMap.put("value", "80");
        httpPortSchemeMap.put("type", "String");
        httpSchemeContrutorList.add(httpSchemeMap);
        httpSchemeContrutorList.add(httpPortSchemeMap);
        List<String> httpSchemeConstructorRefList = new ArrayList();
        httpSchemeConstructorRefList.add("plainSocketFactory");
        registerBean(beanDefinitionRegistry, "httpScheme", Scheme.class, httpSchemeContrutorList, null, httpSchemeConstructorRefList, null);
        List<String> httpsSchemeConstructorRefList = new ArrayList();
        httpsSchemeConstructorRefList.add("sslSocketFactory");
        List<Map> httpsSchemeContrutorList = new ArrayList();
        Map httpsSchemeMap = new HashMap();
        httpsSchemeMap.put("value", "https");
        httpsSchemeMap.put("type", "String");
        Map httpsPortSchemeMap = new HashMap();
        httpsPortSchemeMap.put("value", "443");
        httpsPortSchemeMap.put("type", "String");
        httpsSchemeContrutorList.add(httpsSchemeMap);
        httpsSchemeContrutorList.add(httpsPortSchemeMap);
        registerBean(beanDefinitionRegistry, "httpsScheme", Scheme.class, httpsSchemeContrutorList, null, httpSchemeConstructorRefList, null);
        Map refMap1 = new ManagedMap();
        Map refMap = new ManagedMap();
        refMap.put("https", new RuntimeBeanReference("httpsScheme"));
        refMap.put("http", new RuntimeBeanReference("httpScheme"));
        refMap1.put("items", refMap);
        registerBean(beanDefinitionRegistry, "schemeRegistry", SchemeRegistry.class, null, null, null, refMap1);
        List<String> clientConnectionManagerConstructorRefList = new ArrayList();
        clientConnectionManagerConstructorRefList.add("schemeRegistry");
        registerBean(beanDefinitionRegistry, "clientConnectionManager", ThreadSafeClientConnManager.class, null, null, clientConnectionManagerConstructorRefList, null);
        List<String> httpClientConstructorRefList = new ArrayList();
        httpClientConstructorRefList.add("clientConnectionManager");
        registerBean(beanDefinitionRegistry, "htttpParameters", MyHttpParameters.class, null, null, null, null);
        httpClientConstructorRefList.add("htttpParameters");
        registerBean(beanDefinitionRegistry, "httpClient", DefaultHttpClient.class, null, null, httpClientConstructorRefList, null);


        registerBean(beanDefinitionRegistry, "gridfsHelper", MongoHelper.class, null, null, null, null);
        Map fileAdapterRefMap = new HashMap();
        fileAdapterRefMap.put("resourceHelper", "gridfsHelper");
        registerBean(beanDefinitionRegistry, "fileAdapter", GridFSFileHandler.class, null, fileAdapterRefMap, null, null);

        registerBean(beanDefinitionRegistry, "ipHolder", IPHolder.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "dockerStatusService", DockerStatusServiceImpl.class, null, null, null, null);
//        registerBean(beanDefinitionRegistry, "oauth2properties", AutoReloadProperties.class, null, null, null, null);
        Map onlineServerRefMap = new HashMap();
        onlineServerRefMap.put("dockerStatusService", "dockerStatusService");
        onlineServerRefMap.put("ipHolder", "ipHolder");
        registerBean(beanDefinitionRegistry, "onlineServer", OnlineServerWithStatus.class, null, onlineServerRefMap, null, null);
        Map rpcServerAdapterRefMap = new HashMap();
        rpcServerAdapterRefMap.put("serverImpl", "rpcServer");
        registerBean(beanDefinitionRegistry, "rpcServerAdapter", RMIServerHandler.class, null, rpcServerAdapterRefMap, null, null);
        List<Map> rpcServerContrutorList = new ArrayList();
        Map rpcServerRefMap = new HashMap();
        rpcServerRefMap.put("rmiServerHandler", "rpcServerAdapter");
        Map rpcPortMap = new HashMap();
        rpcPortMap.put("value", rpcPort);
        rpcPortMap.put("type", "Integer");
        rpcServerContrutorList.add(rpcPortMap);
        registerBean(beanDefinitionRegistry, "rpcServer", RMIServerImplWrapper.class, rpcServerContrutorList, rpcServerRefMap, null, null);
        Map rpcServerSsllRefMap = new HashMap();
        rpcServerSsllRefMap.put("rmiServerHandler", "rpcServerAdapterSsl");
        List<Map> rpcServerSslContrutorList = new ArrayList();
        Map rpcServerSslMap = new HashMap();
        rpcServerSslMap.put("value", sslRpcPort);
        rpcServerSslMap.put("type", "Integer");
        rpcServerSslContrutorList.add(rpcServerSslMap);
        registerBean(beanDefinitionRegistry, "rpcServerSsl", RMIServerImplWrapper.class, rpcServerSslContrutorList, rpcServerSsllRefMap, null, null);
        Map rpcServerAdapterSslRefMap = new HashMap();
        rpcServerAdapterSslRefMap.put("serverImpl", "rpcServerSsl");
        registerBean(beanDefinitionRegistry, "rpcServerAdapterSsl", RMIServerHandler.class, null, rpcServerAdapterSslRefMap, null, null);
        Map scriptManagerRefMap = new HashMap();
        scriptManagerRefMap.put("dockerStatusService", "dockerStatusService");
        registerBean(beanDefinitionRegistry, "scriptManager", ScriptManager.class, null, scriptManagerRefMap, null, null);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        BeanDefinition dockerStatusHelper = configurableListableBeanFactory.getBeanDefinition("dockerStatusHelper");
        dockerStatusHelper.setInitMethodName("init");
        MutablePropertyValues dockerStatusHelperPropertyValues = dockerStatusHelper.getPropertyValues();
        dockerStatusHelperPropertyValues.addPropertyValue("host", mongoHost);
        dockerStatusHelperPropertyValues.addPropertyValue("connectionsPerHost", mongoConnectionsPerHost);
        dockerStatusHelperPropertyValues.addPropertyValue("dbName", dbName);
        dockerStatusHelperPropertyValues.addPropertyValue("username", mongoUsername);
        dockerStatusHelperPropertyValues.addPropertyValue("password", mongoPassword);

        BeanDefinition configHelper = configurableListableBeanFactory.getBeanDefinition("configHelper");
        configHelper.setInitMethodName("init");
        MutablePropertyValues configHelperPropertyValues = configHelper.getPropertyValues();
        configHelperPropertyValues.addPropertyValue("host", mongoHost);
        configHelperPropertyValues.addPropertyValue("connectionsPerHost", mongoConnectionsPerHost);
        configHelperPropertyValues.addPropertyValue("dbName", dbName);
        configHelperPropertyValues.addPropertyValue("username", mongoUsername);
        configHelperPropertyValues.addPropertyValue("password", mongoPassword);

        BeanDefinition dockerStatusDAO = configurableListableBeanFactory.getBeanDefinition("dockerStatusDAO");
        dockerStatusDAO.setInitMethodName("init");

        BeanDefinition serversDAO = configurableListableBeanFactory.getBeanDefinition("serversDAO");
        serversDAO.setInitMethodName("init");


        BeanDefinition lansDAO = configurableListableBeanFactory.getBeanDefinition("lansDAO");
        lansDAO.setInitMethodName("init");

        BeanDefinition sdockerDAO = configurableListableBeanFactory.getBeanDefinition("sdockerDAO");
        sdockerDAO.setInitMethodName("init");

        BeanDefinition htttpParameters = configurableListableBeanFactory.getBeanDefinition("htttpParameters");
        MutablePropertyValues htttpParametersPropertyValues = htttpParameters.getPropertyValues();
        htttpParametersPropertyValues.addPropertyValue("charset", "utf8");
        htttpParametersPropertyValues.addPropertyValue("connectionTimeout", "30000");
        htttpParametersPropertyValues.addPropertyValue("socketTimeout", "30000");

        BeanDefinition clientConnectionManager = configurableListableBeanFactory.getBeanDefinition("clientConnectionManager");
        clientConnectionManager.setDestroyMethodName("shutdown");
        MutablePropertyValues clientConnectionManagerPropertyValues = clientConnectionManager.getPropertyValues();
        clientConnectionManagerPropertyValues.addPropertyValue("maxTotal", "20");

        BeanDefinition plainSocketFactory = configurableListableBeanFactory.getBeanDefinition("plainSocketFactory");
        plainSocketFactory.setFactoryMethodName("getSocketFactory");

        BeanDefinition sslSocketFactory = configurableListableBeanFactory.getBeanDefinition("sslSocketFactory");
        sslSocketFactory.setFactoryMethodName("getSocketFactory");

        BeanDefinition gridfsHelper = configurableListableBeanFactory.getBeanDefinition("gridfsHelper");
        gridfsHelper.setInitMethodName("init");
        MutablePropertyValues gridfsHelperPropertyValues = gridfsHelper.getPropertyValues();
        gridfsHelperPropertyValues.addPropertyValue("host", gridHost);
        gridfsHelperPropertyValues.addPropertyValue("connectionsPerHost", girdConnectionsPerHost);
        gridfsHelperPropertyValues.addPropertyValue("dbName", gridDbName);
        gridfsHelperPropertyValues.addPropertyValue("username", gridUsername);
        gridfsHelperPropertyValues.addPropertyValue("password", gridPassword);

        BeanDefinition fileAdapter = configurableListableBeanFactory.getBeanDefinition("fileAdapter");
        fileAdapter.setInitMethodName("init");
        MutablePropertyValues fileAdapterPropertyValues = fileAdapter.getPropertyValues();
        fileAdapterPropertyValues.addPropertyValue("bucketName", "imfs");

        BeanDefinition ipHolder = configurableListableBeanFactory.getBeanDefinition("ipHolder");
        ipHolder.setInitMethodName("init");
        MutablePropertyValues ipHolderMutablePropertyValues = ipHolder.getPropertyValues();
        ipHolderMutablePropertyValues.addPropertyValue("ipPrefix", ipPrefix);
        ipHolderMutablePropertyValues.addPropertyValue("ethPrefix", ethPrefix);

//        BeanDefinition oauth2properties = configurableListableBeanFactory.getBeanDefinition("oauth2properties");
//        oauth2properties.setInitMethodName("init");
//        MutablePropertyValues oauth2propertiesMutablePropertyValues = oauth2properties.getPropertyValues();
//        oauth2propertiesMutablePropertyValues.addPropertyValue("path", oauthPath);

        BeanDefinition rpcServerAdapter = configurableListableBeanFactory.getBeanDefinition("rpcServerAdapter");
        rpcServerAdapter.setInitMethodName("serverStart");
        MutablePropertyValues rpcServerAdapterMutablePropertyValues = rpcServerAdapter.getPropertyValues();
        rpcServerAdapterMutablePropertyValues.addPropertyValue("rmiPort", rpcPort);

        BeanDefinition rpcServerAdapterSsl = configurableListableBeanFactory.getBeanDefinition("rpcServerAdapterSsl");
        rpcServerAdapterSsl.setInitMethodName("serverStart");
        MutablePropertyValues rpcServerAdapterSslMutablePropertyValues = rpcServerAdapterSsl.getPropertyValues();
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rmiPort", sslRpcPort);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("enableSsl", "true");
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslClientTrustJksPath", rpcSslClientTrustJksPath);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslServerJksPath", rpcSslServerJksPath);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslJksPwd", rpcSslJksPwd);

        BeanDefinition scriptManager = configurableListableBeanFactory.getBeanDefinition("scriptManager");
        scriptManager.setInitMethodName("init");
        scriptManager.setDestroyMethodName("shutdown");
        MutablePropertyValues scriptManagerMutablePropertyValues = scriptManager.getPropertyValues();
        scriptManagerMutablePropertyValues.addPropertyValue("localPath", localPath);
        scriptManagerMutablePropertyValues.addPropertyValue("remotePath", remotePath);
        scriptManagerMutablePropertyValues.addPropertyValue("runtimeBootClass", runtimeBootClass);

        BeanDefinition onlineServer = configurableListableBeanFactory.getBeanDefinition("onlineServer");
        onlineServer.setInitMethodName("start");
        onlineServer.setDestroyMethodName("shutdown");
        MutablePropertyValues onlineServerMutablePropertyValues = onlineServer.getPropertyValues();
        onlineServerMutablePropertyValues.addPropertyValue("serverType", serverType);
        onlineServerMutablePropertyValues.addPropertyValue("internalKey", internalKey);
        onlineServerMutablePropertyValues.addPropertyValue("rpcPort", rpcPort);
        onlineServerMutablePropertyValues.addPropertyValue("sslRpcPort", sslRpcPort);
        onlineServerMutablePropertyValues.addPropertyValue("publicDomain", publicDomain);
        onlineServerMutablePropertyValues.addPropertyValue("rpcSslClientTrustJksPath", rpcSslClientTrustJksPath);
        onlineServerMutablePropertyValues.addPropertyValue("rpcSslServerJksPath", rpcSslServerJksPath);
        onlineServerMutablePropertyValues.addPropertyValue("rpcSslJksPwd", rpcSslJksPwd);
        onlineServerMutablePropertyValues.addPropertyValue("configPath", "sdocker.properties");
    }
    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass, List<Map> constructorList, Map refMap, List<String> constuctorRefList, Map propertyRefMap){
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(beanClass);
        //设置依赖
        if(refMap != null){
            for(Object key :  refMap.keySet()){
                beanDefinitionBuilder.addPropertyReference(key.toString(), refMap.get(key).toString());
            }
        }
        if(propertyRefMap != null){
            for(Object key : propertyRefMap.keySet()){
                beanDefinitionBuilder.getBeanDefinition().getPropertyValues().add((String) key, propertyRefMap.get(key));
            }
        }
        BeanDefinition personManagerBeanDefinition = beanDefinitionBuilder
                .getRawBeanDefinition();
//        //注册bean定义
//        registry.registerBeanDefinition("personManager1", personManagerBeanDefinition);

//        AnnotatedBeanDefinition annotatedBeanDefinition  = new AnnotatedGenericBeanDefinition(beanClass);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(personManagerBeanDefinition);
        beanDefinitionBuilder.setScope(scopeMetadata.getScopeName());
        if(constructorList != null){
            ConstructorArgumentValues constructorArgumentValues = personManagerBeanDefinition.getConstructorArgumentValues();
            for (int i = 0; i < constructorList.size(); i++) {
                ConstructorArgumentValues.ValueHolder valueHolder = null;
                Map map = constructorList.get(i);
                Object value = map.get("value");
                String type = (String) map.get("type");
                if(type != null){
                    valueHolder = new ConstructorArgumentValues.ValueHolder(value, type);
                }else if(type == null){
                    valueHolder = new ConstructorArgumentValues.ValueHolder(value);
                }
                if(valueHolder != null){
                    beanDefinitionBuilder.addConstructorArgValue(value);
                }
            }
        }
        if(constuctorRefList != null){
            for (int i = 0; i < constuctorRefList.size(); i++) {
                beanDefinitionBuilder.addConstructorArgReference(constuctorRefList.get(i));
            }
        }
        //可以自动生成name
        String beanName = (name != null?name:this.beanNameGenerator.generateBeanName(personManagerBeanDefinition, registry));
        //bean注册的holer类.
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(personManagerBeanDefinition, beanName);
        //使用bean注册工具类进行注册.
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
    }
}
