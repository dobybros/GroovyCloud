package sdockerboot.container;//package sdockerboot.sdockerboot;

import chat.utils.IPHolder;
import com.dobybros.chat.handlers.ConsumeOfflineMessageHandler;
import com.dobybros.chat.log.LogIndexQueue;
import com.dobybros.chat.onlineserver.OnlineServerWithStatusWithRPC;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.rpc.impl.RMIServerImplWrapper;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.storage.mongodb.MongoHelper;
import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCClientAdapterMapTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.chat.utils.AutoReloadProperties;
import com.dobybros.file.adapters.GridFSFileHandler;
import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.channels.websocket.codec.WebSocketCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.dobybros.http.MyHttpParameters;
import com.dobybros.chat.rpc.impl.RMIHandler;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.script.ScriptManager;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
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
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.*;
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by lick on 2019/5/9.
 * Description：
 */
//@Component
//@Configuration
//@PropertySource({"classpath:config/server_params.properties", "classpath:config/database.properties"})
public class GatewayBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private String mongoHost;
    private String mongoConnectionsPerHost;
    private String dbName;
    private String logsDBName;
    private String configDBName;
    private String mongoUsername;
    private String mongoPassword;

    private String gridHost;
    private String girdConnectionsPerHost;
    private String gridDbName;
    private String gridUsername;
    private String gridPassword;

    private String upstreamPort;
    private String keystorePwd;
    private String keystorePath;
    private String keymanagerPwd;
    private String upstreamSslPort;
    private String upstreamWsPort;

    private String ipPrefix;
    private String ethPrefix;
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
    private String maxUsers;
    private String dockerRpcPort;
    private String dockerSslRpcPort;

    GatewayBeanDefinitionRegistryPostProcessor() {
        InputStream inStream = GatewayBeanDefinitionRegistryPostProcessor.class.getClassLoader().getResourceAsStream("container.properties");
        InputStream appInStream = GatewayBeanDefinitionRegistryPostProcessor.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        Properties apppProp = new Properties();
        try {
            prop.load(inStream);
            mongoHost = prop.getProperty("database.host");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            dbName = prop.getProperty("dockerstatus.dbname");
            logsDBName = prop.getProperty("logs.dbname");
            configDBName = prop.getProperty("config.dbname");
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
            upstreamPort = prop.getProperty("upstream-port");
            keystorePwd = prop.getProperty("keystore.pwd");
            keystorePath = prop.getProperty("keystore.path");
            keymanagerPwd = prop.getProperty("keymanager.pwd");
            upstreamSslPort = prop.getProperty("upstream-ssl-port");
            upstreamWsPort = prop.getProperty("upstream-ws-port");
            maxUsers = prop.getProperty("server.max.users");
            dockerRpcPort = prop.getProperty("docker.rpc.port");
            dockerSslRpcPort = prop.getProperty("docker.rpc.sslport");
            apppProp.load(appInStream);
            serverPort = apppProp.getProperty("server.port");
            System.setProperty("server.port", serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //bean名称生成器
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        registerBean(beanDefinitionRegistry, "springContextUtil", SpringContextUtil.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "globalLansProperties", GlobalLansProperties.class, null, null, null, null);
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
        registerBean(beanDefinitionRegistry, "httpsScheme", Scheme.class, httpsSchemeContrutorList, null, httpsSchemeConstructorRefList, null);
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


        registerBean(beanDefinitionRegistry, "dockerStatusHelper", com.docker.storage.mongodb.MongoHelper.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "logsHelper", MongoHelper.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "configHelper", com.docker.storage.mongodb.MongoHelper.class, null, null, null, null);
        Map dockerStatusDAORefMap = new HashMap();
        dockerStatusDAORefMap.put("mongoHelper", "dockerStatusHelper");
        registerBean(beanDefinitionRegistry, "dockerStatusDAO", DockerStatusDAO.class, null, dockerStatusDAORefMap, null, null);
        Map configHelperRefMap = new HashMap();
        configHelperRefMap.put("mongoHelper", "configHelper");
        registerBean(beanDefinitionRegistry, "serversDAO", ServersDAO.class, null, configHelperRefMap, null, null);
        registerBean(beanDefinitionRegistry, "lansDAO", LansDAO.class, null, configHelperRefMap, null, null);
        registerBean(beanDefinitionRegistry, "sdockerDAO", SDockerDAO.class, null, configHelperRefMap, null, null);
        Map bulkLogDAORefMap = new HashMap();
        bulkLogDAORefMap.put("mongoHelper", "logsHelper");
        registerBean(beanDefinitionRegistry, "bulkLogDAO", BulkLogDAO.class, null, bulkLogDAORefMap, null, null);
        registerBean(beanDefinitionRegistry, "gridfsHelper", MongoHelper.class, null, null, null, null);
        Map fileAdapterRefMap = new HashMap();
        fileAdapterRefMap.put("resourceHelper", "gridfsHelper");
        registerBean(beanDefinitionRegistry, "fileAdapter", GridFSFileHandler.class, null, fileAdapterRefMap, null, null);
//mina

        registerBean(beanDefinitionRegistry, "upStreamAnnotationHandler", UpStreamAnnotationHandler.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "upstreamHandler", UpStreamHandler.class, null, null, null, null);
        Map customEditorConfigurerRefMap = new ManagedMap();
        Map customEditorConfigurerRefMap1 = new ManagedMap();
        customEditorConfigurerRefMap.put("java.net.SocketAddress", "org.apache.mina.integration.beans.InetSocketAddressEditor");
        customEditorConfigurerRefMap1.put("customEditors", customEditorConfigurerRefMap);
        registerBean(beanDefinitionRegistry, "customEditorConfigurer", CustomEditorConfigurer.class, null, null, null, customEditorConfigurerRefMap1);
        registerBean(beanDefinitionRegistry, "hailProtocalCodecFactory", HailProtocalCodecFactory.class, null, null, null, null);
        List<String> tcpCodecFilterConstructorRefList = new ArrayList();
        tcpCodecFilterConstructorRefList.add("hailProtocalCodecFactory");
        registerBean(beanDefinitionRegistry, "tcpCodecFilter", ProtocolCodecFilter.class, null, null, tcpCodecFilterConstructorRefList, null);
        Map tcpFilterChainBuilderRefMap = new ManagedMap();
        Map tcpFilterChainBuilderRefMap1 = new ManagedMap();
        tcpFilterChainBuilderRefMap.put("codecFilter", new RuntimeBeanReference("tcpCodecFilter"));
        tcpFilterChainBuilderRefMap1.put("filters", tcpFilterChainBuilderRefMap);
        registerBean(beanDefinitionRegistry, "tcpFilterChainBuilder", DefaultIoFilterChainBuilder.class, null, null, null, tcpFilterChainBuilderRefMap1);
        Map tcpIoAcceptorSslRefMap = new HashMap();
        tcpIoAcceptorSslRefMap.put("handler", "upstreamHandler");
        tcpIoAcceptorSslRefMap.put("filterChainBuilder", "tcpFilterChainBuilder");
        registerBean(beanDefinitionRegistry, "tcpIoAcceptor", NioSocketAcceptorEx.class, null, tcpIoAcceptorSslRefMap, null, null);
        List<String> sslTcpCodecFilterConstructorRefList = new ArrayList();
        sslTcpCodecFilterConstructorRefList.add("hailProtocalCodecFactory");
        registerBean(beanDefinitionRegistry, "sslTcpCodecFilter", ProtocolCodecFilter.class, null, null, sslTcpCodecFilterConstructorRefList, null);
        registerBean(beanDefinitionRegistry, "keystoreFactory", KeyStoreFactory.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "keyStore", null, null, null, null, null);
        Map sslContextFactoryRefMap = new HashMap();
        sslContextFactoryRefMap.put("keyManagerFactoryKeyStore", "keyStore");
        registerBean(beanDefinitionRegistry, "sslContextFactory", SslContextFactory.class, null, sslContextFactoryRefMap, null, null);
        registerBean(beanDefinitionRegistry, "sslContext", null, null, null, null, null);
        List<String> sslFilterConstructorRefList = new ArrayList();
        sslFilterConstructorRefList.add("sslContext");
        registerBean(beanDefinitionRegistry, "sslFilter", SslFilter.class, null, null, sslFilterConstructorRefList, null);
        Map sslTcpFilterChainBuilderRefMap = new ManagedMap();
        Map sslTcpFilterChainBuilderRefMap1 = new ManagedMap();
        sslTcpFilterChainBuilderRefMap.put("codecFilter", new RuntimeBeanReference("sslTcpCodecFilter"));
        sslTcpFilterChainBuilderRefMap.put("sslFilter", new RuntimeBeanReference("sslFilter"));
        sslTcpFilterChainBuilderRefMap1.put("filters", sslTcpFilterChainBuilderRefMap);
        registerBean(beanDefinitionRegistry, "sslTcpFilterChainBuilder", DefaultIoFilterChainBuilder.class, null, null, null, sslTcpFilterChainBuilderRefMap1);
        Map sslTcpIoAcceptorRefMap = new HashMap();
        sslTcpIoAcceptorRefMap.put("handler", "upstreamHandler");
        sslTcpIoAcceptorRefMap.put("filterChainBuilder", "sslTcpFilterChainBuilder");
        registerBean(beanDefinitionRegistry, "sslTcpIoAcceptor", NioSocketAcceptorEx.class, null, sslTcpIoAcceptorRefMap, null, null);
        //做到这
        registerBean(beanDefinitionRegistry, "webSocketCodecFactory", WebSocketCodecFactory.class, null, null, null, null);
        List<String> wsCodecFilterConstructorRefList = new ArrayList();
        wsCodecFilterConstructorRefList.add("webSocketCodecFactory");
        registerBean(beanDefinitionRegistry, "wsCodecFilter", ProtocolCodecFilter.class, null, null, wsCodecFilterConstructorRefList, null);
        Map wsFilterChainBuilderRefMap = new ManagedMap();
        Map wsFilterChainBuilderRefMap1 = new ManagedMap();
        wsFilterChainBuilderRefMap.put("codecFilter", new RuntimeBeanReference("wsCodecFilter"));
        wsFilterChainBuilderRefMap.put("sslFilter", new RuntimeBeanReference("sslFilter"));
        wsFilterChainBuilderRefMap1.put("filters", wsFilterChainBuilderRefMap);
        registerBean(beanDefinitionRegistry, "wsFilterChainBuilder", DefaultIoFilterChainBuilder.class, null, null, null, wsFilterChainBuilderRefMap1);
        Map wsIoAcceptorRefMap = new HashMap();
        wsIoAcceptorRefMap.put("handler", "upstreamHandler");
        wsIoAcceptorRefMap.put("filterChainBuilder", "wsFilterChainBuilder");
        registerBean(beanDefinitionRegistry, "wsIoAcceptor", NioSocketAcceptorEx.class, null, wsIoAcceptorRefMap, null, null);

        registerBean(beanDefinitionRegistry, "dockerStatusService", DockerStatusServiceImpl.class, null, null, null, null);
        Map bulkLogQueueServiceRefMap = new HashMap();
        bulkLogQueueServiceRefMap.put("dao", "bulkLogDAO");
        registerBean(beanDefinitionRegistry, "bulkLogQueueService", ConsumeQueueService.class, null, bulkLogQueueServiceRefMap, null, null);
        registerBean(beanDefinitionRegistry, "logIndexQueue", LogIndexQueue.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "ipHolder", IPHolder.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "oauth2ClientProperties", AutoReloadProperties.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "consumeOfflineMessageHandler", ConsumeOfflineMessageHandler.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "offlineMessageSavingTask", OfflineMessageSavingTask.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "messageSendingTask", RPCMessageSendingTask.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "rpcClientAdapterMapTask", RPCClientAdapterMapTask.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "rpcClientAdapterMapTaskSsl", RPCClientAdapterMapTask.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "requestPermissionHandler", RequestPermissionHandler.class, null, null, null, null);
        registerBean(beanDefinitionRegistry, "jsonFilterFactory", JsonFilterFactory.class, null, null, null, null);
        Map scriptManagerRefMap = new HashMap();
        scriptManagerRefMap.put("dockerStatusService", "dockerStatusService");
        registerBean(beanDefinitionRegistry, "scriptManager", ScriptManager.class, null, scriptManagerRefMap, null, null);
        registerBean(beanDefinitionRegistry, "onlineUserManager", OnlineUserManagerImpl.class, null, null, null, null);
        List<Map> rpcServerContrutorList = new ArrayList();
        Map rpcServerRefMap = new HashMap();
        rpcServerRefMap.put("rmiHandler", "rpcServerAdapter");
        Map rpcPortMap = new HashMap();
        rpcPortMap.put("value", rpcPort);
        rpcPortMap.put("type", "Integer");
        rpcServerContrutorList.add(rpcPortMap);
        registerBean(beanDefinitionRegistry, "rpcServer", RMIServerImplWrapper.class, rpcServerContrutorList, rpcServerRefMap, null, null);
        Map rpcServerAdapterRefMap = new HashMap();
        rpcServerAdapterRefMap.put("serverImpl", "rpcServer");
        registerBean(beanDefinitionRegistry, "rpcServerAdapter", RMIHandler.class, null, rpcServerAdapterRefMap, null, null);
        Map rpcServerSsllRefMap = new HashMap();
        rpcServerSsllRefMap.put("rmiHandler", "rpcServerAdapterSsl");
        List<Map> rpcServerSslContrutorList = new ArrayList();
        Map rpcServerSslMap = new HashMap();
        rpcServerSslMap.put("value", sslRpcPort);
        rpcServerSslMap.put("type", "Integer");
        rpcServerSslContrutorList.add(rpcServerSslMap);
        registerBean(beanDefinitionRegistry, "rpcServerSsl", RMIServerImplWrapper.class, rpcServerSslContrutorList, rpcServerSsllRefMap, null, null);
        Map rpcServerAdapterSslRefMap = new HashMap();
        rpcServerAdapterSslRefMap.put("serverImpl", "rpcServerSsl");
        registerBean(beanDefinitionRegistry, "rpcServerAdapterSsl", RMIHandler.class, null, rpcServerAdapterSslRefMap, null, null);
        registerBean(beanDefinitionRegistry, "messageEventHandler", MessageEventHandler.class, null, null, null, null);
        List<Map> dockerRpcServerContrutorList = new ArrayList();
        Map dockerRpcServerMap = new HashMap();
        dockerRpcServerMap.put("value", dockerRpcPort);
        dockerRpcServerMap.put("type", "Integer");
        dockerRpcServerContrutorList.add(dockerRpcServerMap);
        Map dockerRpcServerRefMap = new HashMap();
        dockerRpcServerRefMap.put("rmiServerHandler", "dockerRpcServerAdapter");
        registerBean(beanDefinitionRegistry, "dockerRpcServer", com.docker.rpc.impl.RMIServerImplWrapper.class, dockerRpcServerContrutorList, dockerRpcServerRefMap, null, null);
        Map dockerRpcServerAdapterRefMap = new HashMap();
        dockerRpcServerAdapterRefMap.put("serverImpl", "dockerRpcServer");
        registerBean(beanDefinitionRegistry, "dockerRpcServerAdapter", RMIServerHandler.class, null, dockerRpcServerAdapterRefMap, null, null);
        List<Map> dockerRpcServerSslContrutorList = new ArrayList();
        Map dockerRpcServerSslMap = new HashMap();
        dockerRpcServerSslMap.put("value", dockerSslRpcPort);
        dockerRpcServerSslMap.put("type", "Integer");
        dockerRpcServerSslContrutorList.add(dockerRpcServerMap);
        Map dockerRpcServerSslRefMap = new HashMap();
        dockerRpcServerSslRefMap.put("rmiServerHandler", "dockerRpcServerAdapterSsl");
        registerBean(beanDefinitionRegistry, "dockerRpcServerSsl", com.docker.rpc.impl.RMIServerImplWrapper.class, dockerRpcServerSslContrutorList, dockerRpcServerSslRefMap, null, null);
        Map dockerRpcServerAdapterSslRefMap = new HashMap();
        dockerRpcServerAdapterSslRefMap.put("serverImpl", "dockerRpcServerSsl");
        registerBean(beanDefinitionRegistry, "dockerRpcServerAdapterSsl", RMIServerHandler.class, null, dockerRpcServerAdapterSslRefMap, null, null);
        Map onlineServerRefMap = new HashMap();
        onlineServerRefMap.put("dockerStatusService", "dockerStatusService");
//        onlineServerRefMap.put("ipHolder", "ipHolder");
        Map onlineServerPropertyMap = new ManagedMap();
        List onlineServerPropertyList = new ManagedList();
        onlineServerPropertyList.add(new RuntimeBeanReference("rpcClientAdapterMapTask"));
        onlineServerPropertyList.add(new RuntimeBeanReference("rpcClientAdapterMapTaskSsl"));
        onlineServerPropertyList.add(new RuntimeBeanReference("messageSendingTask"));
        onlineServerPropertyList.add(new RuntimeBeanReference("offlineMessageSavingTask"));
        onlineServerPropertyMap.put("tasks", onlineServerPropertyList);
        registerBean(beanDefinitionRegistry, "onlineServer", OnlineServerWithStatusWithRPC.class, null, onlineServerRefMap, null, onlineServerPropertyMap);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        BeanDefinition globalLansProperties = configurableListableBeanFactory.getBeanDefinition("globalLansProperties");
        globalLansProperties.setInitMethodName("init");
        MutablePropertyValues globalLansPropertiesPropertyValues = globalLansProperties.getPropertyValues();
        globalLansPropertiesPropertyValues.addPropertyValue("path", "sdocker.properties");
        BeanDefinition plainSocketFactory = configurableListableBeanFactory.getBeanDefinition("plainSocketFactory");
        plainSocketFactory.setFactoryMethodName("getSocketFactory");
        BeanDefinition sslSocketFactory = configurableListableBeanFactory.getBeanDefinition("sslSocketFactory");
        sslSocketFactory.setFactoryMethodName("getSocketFactory");
        BeanDefinition htttpParameters = configurableListableBeanFactory.getBeanDefinition("htttpParameters");
        MutablePropertyValues htttpParametersPropertyValues = htttpParameters.getPropertyValues();
        htttpParametersPropertyValues.addPropertyValue("charset", "utf8");
        htttpParametersPropertyValues.addPropertyValue("connectionTimeout", "30000");
        htttpParametersPropertyValues.addPropertyValue("socketTimeout", "30000");
        BeanDefinition clientConnectionManager = configurableListableBeanFactory.getBeanDefinition("clientConnectionManager");
        clientConnectionManager.setDestroyMethodName("shutdown");
        MutablePropertyValues clientConnectionManagerPropertyValues = clientConnectionManager.getPropertyValues();
        clientConnectionManagerPropertyValues.addPropertyValue("maxTotal", "20");



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
        configHelperPropertyValues.addPropertyValue("dbName", configDBName);
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
        BeanDefinition bulkLogDAO = configurableListableBeanFactory.getBeanDefinition("bulkLogDAO");
        bulkLogDAO.setInitMethodName("init");
        BeanDefinition logsHelper = configurableListableBeanFactory.getBeanDefinition("logsHelper");
        logsHelper.setInitMethodName("init");
        logsHelper.setDestroyMethodName("disconnect");
        MutablePropertyValues logsHelperPropertyValues = logsHelper.getPropertyValues();
        logsHelperPropertyValues.addPropertyValue("host", mongoHost);
        logsHelperPropertyValues.addPropertyValue("connectionsPerHost", mongoConnectionsPerHost);
        logsHelperPropertyValues.addPropertyValue("dbName", logsDBName);
        logsHelperPropertyValues.addPropertyValue("username", mongoUsername);
        logsHelperPropertyValues.addPropertyValue("password", mongoPassword);
        BeanDefinition gridfsHelper = configurableListableBeanFactory.getBeanDefinition("gridfsHelper");
        gridfsHelper.setInitMethodName("init");
        gridfsHelper.setDestroyMethodName("disconnect");
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
        //mina
        BeanDefinition upstreamHandler = configurableListableBeanFactory.getBeanDefinition("upstreamHandler");
        MutablePropertyValues upstreamHandlerPropertyValues = upstreamHandler.getPropertyValues();
        upstreamHandlerPropertyValues.addPropertyValue("readIdleTime", "720");
        upstreamHandlerPropertyValues.addPropertyValue("writeIdleTime", "720");
        BeanDefinition tcpIoAcceptor = configurableListableBeanFactory.getBeanDefinition("tcpIoAcceptor");
        tcpIoAcceptor.setInitMethodName("bind");
        tcpIoAcceptor.setDestroyMethodName("unbind");
        MutablePropertyValues tcpIoAcceptorPropertyValues = tcpIoAcceptor.getPropertyValues();
        tcpIoAcceptorPropertyValues.addPropertyValue("reuseAddress", "true");
        tcpIoAcceptorPropertyValues.addPropertyValue("defaultLocalAddress", upstreamPort);
        BeanDefinition keystoreFactory = configurableListableBeanFactory.getBeanDefinition("keystoreFactory");
        MutablePropertyValues keystoreFactoryPropertyValues = keystoreFactory.getPropertyValues();
        keystoreFactoryPropertyValues.addPropertyValue("password", keystorePwd.toCharArray());
        URL keystorePathUrl = null;
        try {
            keystorePathUrl = new URL(keystorePath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        keystoreFactoryPropertyValues.addPropertyValue("dataUrl", keystorePathUrl);
        BeanDefinition keyStore = configurableListableBeanFactory.getBeanDefinition("keyStore");
        keyStore.setFactoryMethodName("newInstance");
        keyStore.setFactoryBeanName("keystoreFactory");
        BeanDefinition sslContextFactory = configurableListableBeanFactory.getBeanDefinition("sslContextFactory");
        MutablePropertyValues sslContextFactoryPropertyValues = sslContextFactory.getPropertyValues();
        sslContextFactoryPropertyValues.addPropertyValue("protocol", "TLSV1.2");
        sslContextFactoryPropertyValues.addPropertyValue("keyManagerFactoryAlgorithm", "SunX509");
        sslContextFactoryPropertyValues.addPropertyValue("keyManagerFactoryKeyStorePassword", keymanagerPwd.toCharArray());
        BeanDefinition sslContext = configurableListableBeanFactory.getBeanDefinition("sslContext");
        sslContext.setFactoryMethodName("newInstance");
        sslContext.setFactoryBeanName("sslContextFactory");
        BeanDefinition sslTcpIoAcceptor = configurableListableBeanFactory.getBeanDefinition("sslTcpIoAcceptor");
        MutablePropertyValues sslTcpIoAcceptorPropertyValues = sslTcpIoAcceptor.getPropertyValues();
        sslTcpIoAcceptorPropertyValues.addPropertyValue("defaultLocalAddress", upstreamSslPort);
        sslTcpIoAcceptorPropertyValues.addPropertyValue("reuseAddress", "true");
        BeanDefinition wsIoAcceptor = configurableListableBeanFactory.getBeanDefinition("wsIoAcceptor");
        MutablePropertyValues wsIoAcceptorPropertyValues = wsIoAcceptor.getPropertyValues();
        wsIoAcceptorPropertyValues.addPropertyValue("defaultLocalAddress", upstreamWsPort);
        wsIoAcceptorPropertyValues.addPropertyValue("reuseAddress", "true");
        BeanDefinition logIndexQueue = configurableListableBeanFactory.getBeanDefinition("logIndexQueue");
        logIndexQueue.setInitMethodName("init");
        BeanDefinition ipHolder = configurableListableBeanFactory.getBeanDefinition("ipHolder");
        ipHolder.setInitMethodName("init");
        MutablePropertyValues ipHolderMutablePropertyValues = ipHolder.getPropertyValues();
        ipHolderMutablePropertyValues.addPropertyValue("ipPrefix", ipPrefix);
        ipHolderMutablePropertyValues.addPropertyValue("ethPrefix", ethPrefix);
        BeanDefinition oauth2ClientProperties = configurableListableBeanFactory.getBeanDefinition("oauth2ClientProperties");
        oauth2ClientProperties.setInitMethodName("init");
        MutablePropertyValues oauth2ClientPropertiesPropertyValues = oauth2ClientProperties.getPropertyValues();
        oauth2ClientPropertiesPropertyValues.addPropertyValue("path", "sdocker.properties");
        BeanDefinition messageSendingTask = configurableListableBeanFactory.getBeanDefinition("messageSendingTask");
        MutablePropertyValues messageSendingTaskPropertyValues = messageSendingTask.getPropertyValues();
        messageSendingTaskPropertyValues.addPropertyValue("numOfThreads", "4");
        BeanDefinition rpcClientAdapterMapTaskSsl = configurableListableBeanFactory.getBeanDefinition("rpcClientAdapterMapTaskSsl");
        MutablePropertyValues rpcClientAdapterMapTaskSslPropertyValues = rpcClientAdapterMapTaskSsl.getPropertyValues();
        rpcClientAdapterMapTaskSslPropertyValues.addPropertyValue("enableSsl", "true");
        rpcClientAdapterMapTaskSslPropertyValues.addPropertyValue("rpcSslClientTrustJksPath", rpcSslClientTrustJksPath);
        rpcClientAdapterMapTaskSslPropertyValues.addPropertyValue("rpcSslServerJksPath", rpcSslServerJksPath);
        rpcClientAdapterMapTaskSslPropertyValues.addPropertyValue("rpcSslJksPwd", rpcSslJksPwd);
        BeanDefinition scriptManager = configurableListableBeanFactory.getBeanDefinition("scriptManager");
        scriptManager.setInitMethodName("init");
        scriptManager.setDestroyMethodName("shutdown");
        MutablePropertyValues scriptManagerMutablePropertyValues = scriptManager.getPropertyValues();
        scriptManagerMutablePropertyValues.addPropertyValue("localPath", localPath);
        scriptManagerMutablePropertyValues.addPropertyValue("remotePath", remotePath);
        scriptManagerMutablePropertyValues.addPropertyValue("baseRuntimeClass", "com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime");
        scriptManagerMutablePropertyValues.addPropertyValue("runtimeBootClass", runtimeBootClass);
        BeanDefinition rpcServerAdapter = configurableListableBeanFactory.getBeanDefinition("rpcServerAdapter");
        rpcServerAdapter.setInitMethodName("serverStart");
        MutablePropertyValues rpcServerAdapterMutablePropertyValues = rpcServerAdapter.getPropertyValues();
        rpcServerAdapterMutablePropertyValues.addPropertyValue("rmiPort", rpcPort);
        BeanDefinition rpcServerAdapterSsl = configurableListableBeanFactory.getBeanDefinition("rpcServerAdapterSsl");
        MutablePropertyValues rpcServerAdapterSslMutablePropertyValues = rpcServerAdapterSsl.getPropertyValues();
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rmiPort", sslRpcPort);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("enableSsl", "true");
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslClientTrustJksPath", rpcSslClientTrustJksPath);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslServerJksPath", rpcSslServerJksPath);
        rpcServerAdapterSslMutablePropertyValues.addPropertyValue("rpcSslJksPwd", rpcSslJksPwd);
        BeanDefinition dockerRpcServerAdapter = configurableListableBeanFactory.getBeanDefinition("dockerRpcServerAdapter");
        dockerRpcServerAdapter.setInitMethodName("serverStart");
        MutablePropertyValues dockerRpcServerAdapterMutablePropertyValues = dockerRpcServerAdapter.getPropertyValues();
        dockerRpcServerAdapterMutablePropertyValues.addPropertyValue("rmiPort", dockerRpcPort);
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
        onlineServerMutablePropertyValues.addPropertyValue("maxUsers", maxUsers);
        onlineServerMutablePropertyValues.addPropertyValue("dockerRpcPort", dockerRpcPort);
        onlineServerMutablePropertyValues.addPropertyValue("dockerSslRpcPort", dockerSslRpcPort);
        onlineServerMutablePropertyValues.addPropertyValue("tcpPort", upstreamPort);
        onlineServerMutablePropertyValues.addPropertyValue("sslTcpPort", upstreamSslPort);
        onlineServerMutablePropertyValues.addPropertyValue("wsPort", upstreamWsPort);
        onlineServerMutablePropertyValues.addPropertyValue("status", "1");
        onlineServerMutablePropertyValues.addPropertyValue("configPath", "sdocker.properties");
        BeanDefinition onlineUserManager = configurableListableBeanFactory.getBeanDefinition("onlineUserManager");
        onlineUserManager.setInitMethodName("init");
        MutablePropertyValues onlineUserManagerMutablePropertyValues = onlineUserManager.getPropertyValues();
        onlineUserManagerMutablePropertyValues.addPropertyValue("adminOnlineUserClass", "com.dobybros.gateway.onlineusers.impl.AdminOnlineUserImpl");
    }

    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass, List<Map> constructorList, Map refMap, List<String> constuctorRefList, Map propertyRefMap) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(beanClass);
        //设置依赖
        if (refMap != null) {
            for (Object key : refMap.keySet()) {
                beanDefinitionBuilder.addPropertyReference(key.toString(), refMap.get(key).toString());
            }
        }
        if (propertyRefMap != null) {
            for (Object key : propertyRefMap.keySet()) {
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
        if (constructorList != null) {
            ConstructorArgumentValues constructorArgumentValues = personManagerBeanDefinition.getConstructorArgumentValues();
            for (int i = 0; i < constructorList.size(); i++) {
                ConstructorArgumentValues.ValueHolder valueHolder = null;
                Map map = constructorList.get(i);
                Object value = map.get("value");
                String type = (String) map.get("type");
                if (type != null) {
                    valueHolder = new ConstructorArgumentValues.ValueHolder(value, type);
                } else if (type == null) {
                    valueHolder = new ConstructorArgumentValues.ValueHolder(value);
                }
                if (valueHolder != null) {
                    beanDefinitionBuilder.addConstructorArgValue(value);
                }
            }
        }
        if (constuctorRefList != null) {
            for (int i = 0; i < constuctorRefList.size(); i++) {
                beanDefinitionBuilder.addConstructorArgReference(constuctorRefList.get(i));
            }
        }
        //可以自动生成name
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(personManagerBeanDefinition, registry));
        //bean注册的holer类.
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(personManagerBeanDefinition, beanName);
        //使用bean注册工具类进行注册.
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
    }
}
