package container.container.bean;

import chat.utils.IPHolder;
import com.dobybros.chat.handlers.ConsumeOfflineMessageHandler;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.chat.utils.AutoReloadProperties;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.channels.websocket.codec.WebSocketCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.dobybros.http.MyHttpParameters;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.rpc.remote.stub.RPCInterceptorFactory;
import com.docker.rpc.remote.stub.RemoteServersManager;
import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.script.ScriptManager;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.ScheduledTaskServiceImpl;
import com.docker.storage.adapters.impl.ServersServiceImpl;
import com.docker.storage.adapters.impl.ServiceVersionServiceImpl;
import com.docker.storage.cache.CacheStorageFactory;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
import com.docker.tasks.Task;
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
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

//import com.dobybros.chat.log.LogIndexQueue;
//import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:41
 */
public class BeanApp extends ConfigApp{
    private static BeanApp instance;
    private SpringContextUtil springContextUtil;
    private GlobalLansProperties globalLansProperties;
    private PlainSocketFactory plainSocketFactory;
    private SSLSocketFactory sslSocketFactory;
    private Scheme httpScheme;
    private Scheme httpsScheme;
    private SchemeRegistry schemeRegistry;
    private ThreadSafeClientConnManager clientConnManager;
    private DefaultHttpClient httpClient;
    private MongoHelper dockerStatusHelper;
    private MongoHelper logsHelper;
    private MongoHelper configHelper;
    private MongoHelper scheduledTaskHelper;
    private DockerStatusDAO dockerStatusDAO;
    private ServersDAO serversDAO;
    private LansDAO lansDAO;
    private SDockerDAO sdockerDAO;
    private ServiceVersionDAO serviceVersionDAO;
//    private BulkLogDAO bulkLogDAO;
    private GridFSFileHandler fileAdapter;
    private MongoHelper gridfsHelper;
    private UpStreamHandler upstreamHandler;
    private ProtocolCodecFilter tcpCodecFilter;
    private DefaultIoFilterChainBuilder tcpFilterChainBuilder;
    private NioSocketAcceptorEx tcpIoAcceptor;
    private ProtocolCodecFilter sslTcpCodecFilter;
    private HailProtocalCodecFactory hailProtocalCodecFactory;
    private KeyStoreFactory keystoreFactory;
    private SslContextFactory sslContextFactory;
    private SslFilter sslFilter;
    private DefaultIoFilterChainBuilder sslTcpFilterChainBuilder;
    private NioSocketAcceptorEx sslTcpIoAcceptor;
    private WebSocketCodecFactory webSocketCodecFactory;
    private ProtocolCodecFilter wsCodecFilter;
    private DefaultIoFilterChainBuilder wsFilterChainBuilder;
    private NioSocketAcceptorEx wsIoAcceptor;
    private DockerStatusServiceImpl dockerStatusService;
    private ConsumeQueueService bulkLogQueueService;
//    private LogIndexQueue logIndexQueue;
    private IPHolder ipHolder;
    private AutoReloadProperties oauth2ClientProperties;
    private ConsumeOfflineMessageHandler consumeOfflineMessageHandler;
    private OfflineMessageSavingTask offlineMessageSavingTask;
    private RPCMessageSendingTask messageSendingTask;
    private JsonFilterFactory jsonFilterFactory;
    private RequestPermissionHandler requestPermissionHandler;
    private ScriptManager scriptManager;
    private OnlineUserManagerImpl onlineUserManager;
    private OnlineServerWithStatus onlineServer;
    private RPCClientAdapterMap rpcClientAdapterMap;
    private RPCClientAdapterMap rpcClientAdapterMapSsl;
    private RMIServerImplWrapper rpcServer;
//    private RMIHandler rpcServerAdapter;
    private RMIServerImplWrapper rpcServerSsl;
//    private RMIHandler rpcServerAdapterSsl;
    private MessageEventHandler messageEventHandler;
    private com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServer;
    private RMIServerHandler dockerRpcServerAdapter;
    private com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServerSsl;
    private RMIServerHandler dockerRpcServerAdapterSsl;
    private ServersServiceImpl serversService;
    private ServiceVersionServiceImpl serviceVersionService;
    private ScheduledTaskServiceImpl scheduledTaskService;
    private ScheduledTaskDAO scheduledTaskDAO;
    public synchronized ScheduledTaskServiceImpl getScheduledTaskService(){
        if(scheduledTaskService == null){
            scheduledTaskService = new ScheduledTaskServiceImpl();
            scheduledTaskService.setScheduledTaskDAO(instance.getScheduledTaskDAO());
        }
        return scheduledTaskService;
    }
    public synchronized ScheduledTaskDAO getScheduledTaskDAO(){
        if(scheduledTaskDAO == null){
            scheduledTaskDAO = new ScheduledTaskDAO();
            scheduledTaskDAO.setMongoHelper(instance.getScheduledTaskHelper());
        }
        return scheduledTaskDAO;
    }
    public synchronized ServiceVersionServiceImpl getServiceVersionService() {
        if(serviceVersionService == null){
            serviceVersionService = new ServiceVersionServiceImpl();
            serviceVersionService.setServiceVersionDAO(instance.getServiceVersionDAO());
        }
        return serviceVersionService;
    }

    public synchronized ServiceVersionDAO getServiceVersionDAO() {
        if(serviceVersionDAO == null){
            serviceVersionDAO = new ServiceVersionDAO();
            serviceVersionDAO.setMongoHelper(instance.getDockerStatusHelper());
        }
        return serviceVersionDAO;
    }

    public synchronized ServersServiceImpl getServersService() {
        if(serversService == null){
            serversService = new ServersServiceImpl();
        }
        return serversService;
    }

    public synchronized DockerStatusDAO getDockerStatusDAO() {
        if(dockerStatusDAO == null){
            dockerStatusDAO = new DockerStatusDAO();
            dockerStatusDAO.setMongoHelper(instance.getDockerStatusHelper());
        }
        return dockerStatusDAO;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapterSsl() {
        if(dockerRpcServerAdapterSsl == null){
            dockerRpcServerAdapterSsl = new RMIServerHandler();
            dockerRpcServerAdapterSsl.setServerImpl(instance.getDockerRpcServerSsl());
            dockerRpcServerAdapterSsl.setIpHolder(instance.getIpHolder());
            dockerRpcServerAdapterSsl.setRmiPort(Integer.valueOf(instance.getSslRpcPort()));
            dockerRpcServerAdapterSsl.setEnableSsl(true);
            dockerRpcServerAdapterSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            dockerRpcServerAdapterSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            dockerRpcServerAdapterSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        }
        return dockerRpcServerAdapterSsl;
    }

    public synchronized com.docker.rpc.impl.RMIServerImplWrapper getDockerRpcServerSsl() {
        if(dockerRpcServerSsl == null){
            try {
                dockerRpcServerSsl = instance.getRpcServerSsl();
//                dockerRpcServerSsl = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
                dockerRpcServerSsl.setRmiServerHandler(instance.getDockerRpcServerAdapterSsl());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return dockerRpcServerSsl;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapter() {
        if(dockerRpcServerAdapter == null){
            dockerRpcServerAdapter = new RMIServerHandler();
            dockerRpcServerAdapter.setServerImpl(instance.getDockerRpcServer());
            dockerRpcServerAdapter.setIpHolder(instance.getIpHolder());
            dockerRpcServerAdapter.setRmiPort(Integer.valueOf(instance.getRpcPort()));
        }
        return dockerRpcServerAdapter;
    }

    public synchronized com.docker.rpc.impl.RMIServerImplWrapper getDockerRpcServer() {
        if(dockerRpcServer == null){
            try {
                dockerRpcServer = instance.getRpcServer();
//                dockerRpcServer = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
                dockerRpcServer.setRmiServerHandler(instance.getDockerRpcServerAdapter());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return dockerRpcServer;
    }

    public synchronized MessageEventHandler getMessageEventHandler() {
        if(messageEventHandler == null){
            messageEventHandler = new MessageEventHandler();
        }
        return messageEventHandler;
    }

//    public synchronized RMIHandler getRpcServerAdapterSsl() {
//        if(rpcServerAdapterSsl == null){
//            rpcServerAdapterSsl = new RMIHandler();
//        }
//        return rpcServerAdapterSsl;
//    }

    public synchronized RMIServerImplWrapper getRpcServerSsl() {
        if(rpcServerSsl == null){
            try {
                rpcServerSsl = new RMIServerImplWrapper(Integer.valueOf(getSslRpcPort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return rpcServerSsl;
    }

//    public synchronized RMIHandler getRpcServerAdapter() {
//        if(rpcServerAdapter == null){
//            rpcServerAdapter = new RMIHandler();
//        }
//        return rpcServerAdapter;
//    }

    public synchronized RMIServerImplWrapper getRpcServer() {
        if(rpcServer == null){
            try {
                rpcServer = new RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return rpcServer;
    }

    public synchronized RPCClientAdapterMap getRpcClientAdapterMapSsl() {
        if(rpcClientAdapterMapSsl == null){
            rpcClientAdapterMapSsl = new RPCClientAdapterMap();
            rpcClientAdapterMapSsl.setEnableSsl(true);
            rpcClientAdapterMapSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            rpcClientAdapterMapSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            rpcClientAdapterMapSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        }
        return rpcClientAdapterMapSsl;
    }

    public synchronized RPCClientAdapterMap getRpcClientAdapterMap() {
        if(rpcClientAdapterMap == null){
            rpcClientAdapterMap = new RPCClientAdapterMap();
        }
        return rpcClientAdapterMap;
    }

    public synchronized OnlineServerWithStatus getOnlineServer() {
        if(onlineServer == null){
            onlineServer = new OnlineServerWithStatus();
            onlineServer.setDockerStatusService(instance.getDockerStatusService());
            List<Task> tasks = new ArrayList<>();
            tasks.add(instance.getMessageSendingTask());
            tasks.add(instance.getOfflineMessageSavingTask());
            onlineServer.setTasks(tasks);
            onlineServer.setServerType(instance.getServerType());
            onlineServer.setDockerName(instance.getDockerName());
            onlineServer.setHttpPort(Integer.valueOf(instance.getServerPort()));
            onlineServer.setInternalKey(instance.getInternalKey());
            onlineServer.setRpcPort(instance.getRpcPort());
            onlineServer.setSslRpcPort(instance.getSslRpcPort());
            onlineServer.setPublicDomain(instance.getPublicDomain());
            onlineServer.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            onlineServer.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            onlineServer.setRpcSslJksPwd(instance.getRpcSslJksPwd());
            onlineServer.setMaxUsers(Integer.valueOf(instance.getMaxUsers()));
            onlineServer.setTcpPort(instance.getUpstreamPort());
            onlineServer.setWsPort(instance.getUpstreamWsPort());
            onlineServer.setStatus(1);
            onlineServer.setConfigPath("container.properties");
            onlineServer.setIpHolder(instance.getIpHolder());
        }
        return onlineServer;
    }

    public synchronized OnlineUserManagerImpl getOnlineUserManager() {
        if(onlineUserManager == null){
            onlineUserManager = new OnlineUserManagerImpl();
            onlineUserManager.setAdminOnlineUserClass(com.dobybros.gateway.onlineusers.impl.AdminOnlineUserImpl.class);
        }
        return onlineUserManager;
    }

    public synchronized ScriptManager getScriptManager() {
        if(scriptManager == null){
            scriptManager = new ScriptManager();
            scriptManager.setLocalPath(instance.getLocalPath());
            scriptManager.setRemotePath(instance.getRemotePath());
            scriptManager.setBaseRuntimeClass(com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime.class);
            scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
//            scriptManager.setDockerStatusService(instance.getDockerStatusService());
//            scriptManager.setFileAdapter(instance.getFileAdapter());
            scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            scriptManager.setServerType(instance.getServerType());
//            scriptManager.setServiceVersionService(instance.getServiceVersionService());
        }
        return scriptManager;
    }

    public synchronized RequestPermissionHandler getRequestPermissionHandler() {
        if(requestPermissionHandler == null){
            requestPermissionHandler = new RequestPermissionHandler();
        }
        return requestPermissionHandler;
    }

    public synchronized JsonFilterFactory getJsonFilterFactory() {
        if(jsonFilterFactory == null){
            jsonFilterFactory = new JsonFilterFactory();
        }
        return jsonFilterFactory;
    }

    public synchronized RPCMessageSendingTask getMessageSendingTask() {
        if(messageSendingTask == null){
            messageSendingTask = new RPCMessageSendingTask();
            messageSendingTask.setNumOfThreads(4);
        }
        return messageSendingTask;
    }

    public synchronized OfflineMessageSavingTask getOfflineMessageSavingTask() {
        if(offlineMessageSavingTask == null){
            offlineMessageSavingTask = new OfflineMessageSavingTask();
        }
        return offlineMessageSavingTask;
    }

    public synchronized ConsumeOfflineMessageHandler getConsumeOfflineMessageHandler() {
        if(consumeOfflineMessageHandler == null){
            consumeOfflineMessageHandler = new ConsumeOfflineMessageHandler();
        }
        return consumeOfflineMessageHandler;
    }

    public synchronized AutoReloadProperties getOauth2ClientProperties() {
        if(oauth2ClientProperties == null){
            oauth2ClientProperties = new AutoReloadProperties();
        }
        return oauth2ClientProperties;
    }

    public synchronized IPHolder getIpHolder() {
        if(ipHolder == null){
            ipHolder = new IPHolder();
            ipHolder.setEthPrefix(instance.getEthPrefix());
            ipHolder.setIpPrefix(instance.getIpPrefix());
        }
        return ipHolder;
    }

//    public synchronized LogIndexQueue getLogIndexQueue() {
//        if(logIndexQueue == null){
//            logIndexQueue = new LogIndexQueue();
//        }
//        return logIndexQueue;
//    }

    public synchronized ConsumeQueueService getBulkLogQueueService() {
        if(bulkLogQueueService == null){
            bulkLogQueueService = new ConsumeQueueService();
        }
        return bulkLogQueueService;
    }

    public synchronized DockerStatusServiceImpl getDockerStatusService() {
        if(dockerStatusService == null){
            dockerStatusService = new DockerStatusServiceImpl();
            dockerStatusService.setDockerStatusDAO(instance.getDockerStatusDAO());
        }
        return dockerStatusService;
    }

    public synchronized SpringContextUtil getSpringContextUtil() {
        if(springContextUtil == null){
            springContextUtil = new SpringContextUtil();
        }
        return springContextUtil;
    }

    public synchronized NioSocketAcceptorEx getWsIoAcceptor() {
        if(wsIoAcceptor == null){
            wsIoAcceptor = new NioSocketAcceptorEx();
            wsIoAcceptor.setHandler(instance.getUpstreamHandler());
            wsIoAcceptor.setFilterChainBuilder(instance.getWsFilterChainBuilder());
            wsIoAcceptor.setReuseAddress(true);
            wsIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamWsPort())));
        }
        return wsIoAcceptor;
    }

    public synchronized DefaultIoFilterChainBuilder getWsFilterChainBuilder() {
        if(wsFilterChainBuilder == null){
            wsFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
            map.put("sslFilter", instance.getSslFilter());
            map.put("codecFilter", instance.getWsCodecFilter());
            wsFilterChainBuilder.setFilters(map);
        }
        return wsFilterChainBuilder;
    }

    public synchronized ProtocolCodecFilter getWsCodecFilter() {
        if(wsCodecFilter == null){
            wsCodecFilter = new ProtocolCodecFilter(getWebSocketCodecFactory());
        }
        return wsCodecFilter;
    }

    public synchronized WebSocketCodecFactory getWebSocketCodecFactory() {
        if(webSocketCodecFactory == null){
            webSocketCodecFactory = new WebSocketCodecFactory();
        }
        return webSocketCodecFactory;
    }

    public synchronized NioSocketAcceptorEx getSslTcpIoAcceptor() {
        if(sslTcpIoAcceptor == null){
            sslTcpIoAcceptor = new NioSocketAcceptorEx();
            sslTcpIoAcceptor.setHandler(instance.getUpstreamHandler());
            sslTcpIoAcceptor.setFilterChainBuilder(instance.getSslTcpFilterChainBuilder());
            sslTcpIoAcceptor.setReuseAddress(true);
            sslTcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamSslPort())));
        }
        return sslTcpIoAcceptor;
    }

    public DefaultIoFilterChainBuilder getSslTcpFilterChainBuilder() {
        if(sslTcpFilterChainBuilder == null){
            sslTcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
            map.put("codecFilter", instance.getSslTcpCodecFilter());
            map.put("sslFilter", instance.getSslFilter());
            sslTcpFilterChainBuilder.setFilters(map);
        }
        return sslTcpFilterChainBuilder;
    }

    public synchronized SslFilter getSslFilter() {
        if(sslFilter == null){
            try {
                sslFilter = new SslFilter(getSslContextFactory().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslFilter;
    }

    public synchronized SslContextFactory getSslContextFactory() {
        if(sslContextFactory == null){
            sslContextFactory = new SslContextFactory();
            try {
                sslContextFactory.setKeyManagerFactoryKeyStore(instance.getKeystoreFactory().newInstance());
                sslContextFactory.setProtocol("TLSV1.2");
                sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
                sslContextFactory.setKeyManagerFactoryKeyStorePassword(instance.getKeymanagerPwd());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslContextFactory;
    }

    public synchronized KeyStoreFactory getKeystoreFactory() {
        if(keystoreFactory == null){
            keystoreFactory = new KeyStoreFactory();
            keystoreFactory.setPassword(instance.getKeystorePwd());
            URL keystorePathUrl = null;
            try {
                keystorePathUrl = new URL(instance.getKeystorePath());
                keystoreFactory.setDataUrl(keystorePathUrl);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return keystoreFactory;
    }

    public synchronized ProtocolCodecFilter getSslTcpCodecFilter() {
        if(sslTcpCodecFilter == null){
            sslTcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return sslTcpCodecFilter;
    }

    public synchronized NioSocketAcceptorEx getTcpIoAcceptor() {
        if(tcpIoAcceptor == null){
            tcpIoAcceptor = new NioSocketAcceptorEx();
            tcpIoAcceptor.setHandler(instance.getUpstreamHandler());
            tcpIoAcceptor.setFilterChainBuilder(instance.getTcpFilterChainBuilder());
            tcpIoAcceptor.setReuseAddress(true);
            tcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamPort())));
        }
        return tcpIoAcceptor;
    }

    public synchronized GlobalLansProperties getGlobalLansProperties() {
        if(globalLansProperties == null){
            globalLansProperties = new GlobalLansProperties();
        }
        return globalLansProperties;
    }

    public synchronized PlainSocketFactory getPlainSocketFactory() {
        if(plainSocketFactory == null){
            plainSocketFactory = PlainSocketFactory.getSocketFactory();
        }
        return plainSocketFactory;
    }

    public synchronized SSLSocketFactory getSslSocketFactory() {
        if(sslSocketFactory == null){
            sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }
        return sslSocketFactory;
    }

    public synchronized Scheme getHttpScheme() {
        if(httpScheme == null){
            httpScheme = new Scheme("http", 80, getPlainSocketFactory());
        }
        return httpScheme;
    }

    public synchronized Scheme getHttpsScheme() {
        if(httpsScheme == null){
            httpsScheme = new Scheme("https", 443, getSslSocketFactory());
        }
        return httpsScheme;
    }

    public synchronized SchemeRegistry getSchemeRegistry() {
        if(schemeRegistry == null){
            schemeRegistry = new SchemeRegistry();
            Map map = new HashMap();
            map.put("http", instance.getHttpScheme());
            map.put("https", instance.getHttpsScheme());
            schemeRegistry.setItems(map);
        }
        return schemeRegistry;
    }

    public synchronized ThreadSafeClientConnManager getClientConnManager() {
        if(clientConnManager == null){
            clientConnManager = new ThreadSafeClientConnManager(getSchemeRegistry());
            clientConnManager.setMaxTotal(20);
        }
        return clientConnManager;
    }

    public synchronized DefaultHttpClient getHttpClient() {
        if(httpClient == null){
            MyHttpParameters myHttpParameters = new MyHttpParameters();
            myHttpParameters.setCharset("utf8");
            myHttpParameters.setConnectionTimeout(30000);
            myHttpParameters.setSocketTimeout(30000);
            httpClient = new DefaultHttpClient(getClientConnManager(), myHttpParameters);
        }
        return httpClient;
    }

    public synchronized MongoHelper getDockerStatusHelper() {
        if(dockerStatusHelper == null){
            dockerStatusHelper = new MongoHelper();
            dockerStatusHelper.setHost(instance.getMongoHost());
            dockerStatusHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            dockerStatusHelper.setDbName(instance.getDbName());
            dockerStatusHelper.setUsername(instance.getMongoUsername());
            dockerStatusHelper.setPassword(instance.getMongoPassword());
        }
        return dockerStatusHelper;
    }
    public synchronized MongoHelper getScheduledTaskHelper() {
        if(scheduledTaskHelper == null){
            scheduledTaskHelper = new MongoHelper();
            scheduledTaskHelper.setHost(instance.getMongoHost());
            scheduledTaskHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            scheduledTaskHelper.setDbName("scheduled");
            scheduledTaskHelper.setUsername(instance.getMongoUsername());
            scheduledTaskHelper.setPassword(instance.getMongoPassword());
        }
        return scheduledTaskHelper;
    }

    public synchronized MongoHelper getLogsHelper() {
        if(logsHelper == null){
            logsHelper = new MongoHelper();
            logsHelper.setHost(instance.getMongoHost());
            logsHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            logsHelper.setDbName(instance.getLogsDBName());
            logsHelper.setUsername(instance.getMongoUsername());
            logsHelper.setPassword(instance.getMongoPassword());
        }
        return logsHelper;
    }

    public synchronized MongoHelper getConfigHelper() {
        if(configHelper == null){
            configHelper = new MongoHelper();
            configHelper.setHost(instance.getMongoHost());
            configHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            configHelper.setDbName(instance.getConfigDBName());
            configHelper.setUsername(instance.getMongoUsername());
            configHelper.setPassword(instance.getMongoPassword());
        }
        return configHelper;
    }

    public synchronized ServersDAO getServersDAO() {
        if(serversDAO == null){
            serversDAO = new ServersDAO();
            serversDAO.setMongoHelper(instance.getConfigHelper());
        }
        return serversDAO;
    }

    public synchronized LansDAO getLansDAO() {
        if(lansDAO == null){
            lansDAO = new LansDAO();
            lansDAO.setMongoHelper(instance.getConfigHelper());
        }
        return lansDAO;
    }

    public synchronized SDockerDAO getSdockerDAO() {
        if(sdockerDAO == null){
            sdockerDAO = new SDockerDAO();
            sdockerDAO.setMongoHelper(instance.getConfigHelper());
        }
        return sdockerDAO;
    }

//    public synchronized BulkLogDAO getBulkLogDAO() {
//        if(bulkLogDAO == null){
//            bulkLogDAO = new BulkLogDAO();
//        }
//        return bulkLogDAO;
//    }

    public synchronized MongoHelper getGridfsHelper() {
        if(gridfsHelper == null){
            gridfsHelper = new MongoHelper();
            gridfsHelper.setHost(instance.getGridHost());
            gridfsHelper.setConnectionsPerHost(Integer.valueOf(instance.getGirdConnectionsPerHost()));
            gridfsHelper.setDbName(instance.getGridDbName());
            gridfsHelper.setUsername(instance.getGridUsername());
            gridfsHelper.setPassword(instance.getGridPassword());
        }
        return gridfsHelper;
    }

    public synchronized GridFSFileHandler getFileAdapter() {
        if(fileAdapter == null){
            fileAdapter = new GridFSFileHandler();
            fileAdapter.setResourceHelper(instance.getGridfsHelper());
            fileAdapter.setBucketName(instance.getFileBucket());
        }
        return fileAdapter;
    }

    public synchronized UpStreamHandler getUpstreamHandler() {
        if(upstreamHandler == null){
            upstreamHandler = new UpStreamHandler();
            upstreamHandler.setReadIdleTime(720);
            upstreamHandler.setWriteIdleTime(720);
        }
        return upstreamHandler;
    }

    public synchronized HailProtocalCodecFactory getHailProtocalCodecFactory() {
        if(hailProtocalCodecFactory == null){
            hailProtocalCodecFactory = new HailProtocalCodecFactory();
        }
        return hailProtocalCodecFactory;
    }

    public synchronized ProtocolCodecFilter getTcpCodecFilter() {
        if(tcpCodecFilter == null){
            tcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return tcpCodecFilter;
    }

    public synchronized DefaultIoFilterChainBuilder getTcpFilterChainBuilder() {
        if(tcpFilterChainBuilder == null){
            tcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
            map.put("codecFilter", instance.getTcpCodecFilter());
            tcpFilterChainBuilder.setFilters(map);
        }
        return tcpFilterChainBuilder;
    }
    public synchronized static BeanApp getInstance(){
        if(instance == null){
            synchronized (BeanApp.class){
                if (instance == null){
                    instance = new BeanApp();
                }
            }
        }
        return instance;
    }
}
