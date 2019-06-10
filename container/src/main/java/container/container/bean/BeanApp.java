package container.container.bean;

import chat.scheduled.QuartzHandler;
import chat.utils.IPHolder;
import com.dobybros.chat.handlers.ConsumeOfflineMessageHandler;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCClientAdapterMapTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.chat.utils.AutoReloadProperties;
import com.dobybros.file.adapters.GridFSFileHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.channels.websocket.codec.WebSocketCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.dobybros.http.MyHttpParameters;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.rpc.remote.stub.RemoteServersDiscovery;
import com.docker.script.ScriptManager;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.adapters.impl.ServersServiceImpl;
import com.docker.storage.adapters.impl.ServiceVersionServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
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
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

import java.rmi.RemoteException;

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
    private RPCClientAdapterMapTask rpcClientAdapterMapTask;
    private RPCClientAdapterMapTask rpcClientAdapterMapTaskSsl;
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
    private SchedulerFactory schedulerFactory;
    private QuartzHandler quartzHandler;

//    public synchronized QuartzHandler getQuartzHandler() {
//        if(quartzHandler == null){
//            quartzHandler = new QuartzHandler();
//            quartzHandler.setSchedulerFactory(getSchedulerFactory());
//        }
//        return quartzHandler;
//    }
    public synchronized SchedulerFactory getSchedulerFactory(){
        if(schedulerFactory == null){
            schedulerFactory = new StdSchedulerFactory();
        }
        return schedulerFactory;
    }
    public synchronized ServiceVersionServiceImpl getServiceVersionService() {
        if(serviceVersionService == null){
            serviceVersionService = new ServiceVersionServiceImpl();
            serviceVersionService.setServiceVersionDAO(getServiceVersionDAO());
        }
        return serviceVersionService;
    }

    public synchronized ServiceVersionDAO getServiceVersionDAO() {
        if(serviceVersionDAO == null){
            serviceVersionDAO = new ServiceVersionDAO();
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
        }
        return dockerStatusDAO;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapterSsl() {
        if(dockerRpcServerAdapterSsl == null){
            dockerRpcServerAdapterSsl = new RMIServerHandler();
        }
        return dockerRpcServerAdapterSsl;
    }

    public synchronized com.docker.rpc.impl.RMIServerImplWrapper getDockerRpcServerSsl() {
        if(dockerRpcServerSsl == null){
            try {
                dockerRpcServerSsl = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getDockerSslRpcPort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return dockerRpcServerSsl;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapter() {
        if(dockerRpcServerAdapter == null){
            dockerRpcServerAdapter = new RMIServerHandler();
        }
        return dockerRpcServerAdapter;
    }

    public synchronized com.docker.rpc.impl.RMIServerImplWrapper getDockerRpcServer() {
        if(dockerRpcServer == null){
            try {
                dockerRpcServer = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getDockerRpcPort()));
            } catch (RemoteException e) {
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

    public synchronized RPCClientAdapterMapTask getRpcClientAdapterMapTaskSsl() {
        if(rpcClientAdapterMapTaskSsl == null){
            rpcClientAdapterMapTaskSsl = new RPCClientAdapterMapTask();
        }
        return rpcClientAdapterMapTaskSsl;
    }

    public synchronized RPCClientAdapterMapTask getRpcClientAdapterMapTask() {
        if(rpcClientAdapterMapTask == null){
            rpcClientAdapterMapTask = new RPCClientAdapterMapTask();
        }
        return rpcClientAdapterMapTask;
    }

    public synchronized OnlineServerWithStatus getOnlineServer() {
        if(onlineServer == null){
            onlineServer = new OnlineServerWithStatus();
        }
        return onlineServer;
    }

    public synchronized OnlineUserManagerImpl getOnlineUserManager() {
        if(onlineUserManager == null){
            onlineUserManager = new OnlineUserManagerImpl();
        }
        return onlineUserManager;
    }

    public synchronized ScriptManager getScriptManager() {
        if(scriptManager == null){
            scriptManager = new ScriptManager();
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
        }
        return wsIoAcceptor;
    }

    public synchronized DefaultIoFilterChainBuilder getWsFilterChainBuilder() {
        if(wsFilterChainBuilder == null){
            wsFilterChainBuilder = new DefaultIoFilterChainBuilder();
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
        }
        return sslTcpIoAcceptor;
    }

    public DefaultIoFilterChainBuilder getSslTcpFilterChainBuilder() {
        if(sslTcpFilterChainBuilder == null){
            sslTcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
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
        }
        return sslContextFactory;
    }

    public synchronized KeyStoreFactory getKeystoreFactory() {
        if(keystoreFactory == null){
            keystoreFactory = new KeyStoreFactory();
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
        }
        return schemeRegistry;
    }

    public synchronized ThreadSafeClientConnManager getClientConnManager() {
        if(clientConnManager == null){
            clientConnManager = new ThreadSafeClientConnManager(getSchemeRegistry());
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
        }
        return dockerStatusHelper;
    }

    public synchronized MongoHelper getLogsHelper() {
        if(logsHelper == null){
            logsHelper = new MongoHelper();
        }
        return logsHelper;
    }

    public synchronized MongoHelper getConfigHelper() {
        if(configHelper == null){
            configHelper = new MongoHelper();
        }
        return configHelper;
    }

    public synchronized ServersDAO getServersDAO() {
        if(serversDAO == null){
            serversDAO = new ServersDAO();
        }
        return serversDAO;
    }

    public synchronized LansDAO getLansDAO() {
        if(lansDAO == null){
            lansDAO = new LansDAO();
        }
        return lansDAO;
    }

    public synchronized SDockerDAO getSdockerDAO() {
        if(sdockerDAO == null){
            sdockerDAO = new SDockerDAO();
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
        }
        return gridfsHelper;
    }

    public synchronized GridFSFileHandler getFileAdapter() {
        if(fileAdapter == null){
            fileAdapter = new GridFSFileHandler();
        }
        return fileAdapter;
    }

    public synchronized UpStreamHandler getUpstreamHandler() {
        if(upstreamHandler == null){
            upstreamHandler = new UpStreamHandler();
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
