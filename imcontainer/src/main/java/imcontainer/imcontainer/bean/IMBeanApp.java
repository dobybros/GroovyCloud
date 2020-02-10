package imcontainer.imcontainer.bean;

import com.dobybros.chat.handlers.*;
import com.dobybros.chat.handlers.imextention.IMExtensionCache;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.channels.websocket.codec.WebSocketCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.script.ScriptManager;
import com.docker.tasks.Task;
import container.container.bean.BeanApp;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import com.dobybros.chat.log.LogIndexQueue;
//import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:41
 */
public class IMBeanApp extends IMConfigApp {
    private static volatile IMBeanApp instance;
    private GlobalLansProperties globalLansProperties;
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
    private ConsumeQueueService bulkLogQueueService;
    private ConsumeOfflineMessageHandler consumeOfflineMessageHandler;
    private OfflineMessageSavingTask offlineMessageSavingTask;
    private RPCMessageSendingTask messageSendingTask;
    private OnlineUserManagerImpl onlineUserManager;
    private OnlineServerWithStatus onlineServer;
    private ScriptManager scriptManager;
    private MessageEventHandler messageEventHandler;
    private PingHandler pingHandler;
    private IMExtensionCache imExtensionCache;
    private ProxyContainerDuplexSender proxyContainerDuplexSender;
    private RpcProxyContainerDuplexSender rpcProxyContainerDuplexSender;
    private QueueProxyContainerDuplexSender queueProxyContainerDuplexSender;

    public synchronized IMExtensionCache getIMExtensionCache() {
        if(instance.imExtensionCache == null){
            instance.imExtensionCache = new IMExtensionCache();
            instance.imExtensionCache.setHost(getRedisHost());
        }
        return instance.imExtensionCache;
    }
    public synchronized PingHandler getPingHandler() {
        if(instance.pingHandler == null){
            instance.pingHandler = new PingHandler();
            instance.pingHandler.setUseProxy(getUseProxy());
        }
        return instance.pingHandler;
    }

    public synchronized MessageEventHandler getMessageEventHandler() {
        if(instance.messageEventHandler == null){
            instance.messageEventHandler = new MessageEventHandler();
        }
        return instance.messageEventHandler;
    }

    public synchronized OnlineServerWithStatus getOnlineServer() {
        if(instance.onlineServer == null){
            instance.onlineServer = new OnlineServerWithStatus();
            instance.onlineServer.setDockerStatusService(instance.getDockerStatusService());
            List<Task> tasks = new ArrayList<>();
            tasks.add(instance.getMessageSendingTask());
            tasks.add(instance.getOfflineMessageSavingTask());
            instance.onlineServer.setTasks(tasks);
            instance.onlineServer.setServerType(instance.getServerType());
            instance.onlineServer.setDockerName(instance.getDockerName());
            instance.onlineServer.setHttpPort(Integer.valueOf(instance.getServerPort()));
            instance.onlineServer.setInternalKey(instance.getInternalKey());
            instance.onlineServer.setRpcPort(instance.getRpcPort());
            instance.onlineServer.setSslRpcPort(instance.getSslRpcPort());
            instance.onlineServer.setPublicDomain(instance.getPublicDomain());
            instance.onlineServer.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            instance.onlineServer.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            instance.onlineServer.setRpcSslJksPwd(instance.getRpcSslJksPwd());
            instance.onlineServer.setMaxUsers(Integer.valueOf(instance.getMaxUsers()));
            instance.onlineServer.setTcpPort(instance.getUpstreamPort());
            instance.onlineServer.setWsPort(instance.getUpstreamWsPort());
            instance.onlineServer.setSslTcpPort(instance.getUpstreamSslPort());
            instance.onlineServer.setPublicWsPort(instance.getPublicWsPort());
            instance.onlineServer.setStatus(1);
            instance.onlineServer.setType(Integer.valueOf(instance.getType()));
            instance.onlineServer.setConfigPath("groovycloud.properties");
            instance.onlineServer.setIpHolder(instance.getIpHolder());
            instance.onlineServer.setMaxUserNumber(instance.getMaxUserNumber());
        }
        return instance.onlineServer;
    }
    public synchronized ScriptManager getScriptManager() {
        if (instance.scriptManager == null) {
            instance.scriptManager = new ScriptManager();
            instance.scriptManager.setLocalPath(instance.getLocalPath());
            instance.scriptManager.setRemotePath(instance.getRemotePath());
            instance.scriptManager.setBaseRuntimeClass(GatewayGroovyRuntime.class);
            instance.scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
            instance.scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            instance.scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            instance.scriptManager.setServerType(instance.getServerType());
        }
        return instance.scriptManager;
    }
    public synchronized OnlineUserManagerImpl getOnlineUserManager() {
        if(instance.onlineUserManager == null){
            instance.onlineUserManager = new OnlineUserManagerImpl();
            instance.onlineUserManager.setAdminOnlineUserClass(com.dobybros.gateway.onlineusers.impl.AdminOnlineUserImpl.class);
        }
        return instance.onlineUserManager;
    }

    public synchronized RPCMessageSendingTask getMessageSendingTask() {
        if(instance.messageSendingTask == null){
            instance.messageSendingTask = new RPCMessageSendingTask();
            instance.messageSendingTask.setNumOfThreads(4);
        }
        return instance.messageSendingTask;
    }
    public synchronized ProxyContainerDuplexSender getProxyContainerDuplexSender() {
        if (instance.proxyContainerDuplexSender == null) {
            instance.proxyContainerDuplexSender = new ProxyContainerDuplexSender();
        }
        return instance.proxyContainerDuplexSender;
    }
    public synchronized RpcProxyContainerDuplexSender getRpcProxyContainerDuplexSender() {
        if (instance.rpcProxyContainerDuplexSender == null) {
            instance.rpcProxyContainerDuplexSender = new RpcProxyContainerDuplexSender();
        }
        return instance.rpcProxyContainerDuplexSender;
    }
    public synchronized QueueProxyContainerDuplexSender getQueueProxyContainerDuplexSender() {
        if (instance.queueProxyContainerDuplexSender == null) {
            instance.queueProxyContainerDuplexSender = new QueueProxyContainerDuplexSender();
        }
        return instance.queueProxyContainerDuplexSender;
    }
    public synchronized OfflineMessageSavingTask getOfflineMessageSavingTask() {
        if(instance.offlineMessageSavingTask == null){
            instance.offlineMessageSavingTask = new OfflineMessageSavingTask();
        }
        return instance.offlineMessageSavingTask;
    }

    public synchronized ConsumeOfflineMessageHandler getConsumeOfflineMessageHandler() {
        if(instance.consumeOfflineMessageHandler == null){
            instance.consumeOfflineMessageHandler = new ConsumeOfflineMessageHandler();
        }
        return instance.consumeOfflineMessageHandler;
    }

    public synchronized ConsumeQueueService getBulkLogQueueService() {
        if(instance.bulkLogQueueService == null){
            instance.bulkLogQueueService = new ConsumeQueueService();
        }
        return instance.bulkLogQueueService;
    }

    public synchronized NioSocketAcceptorEx getWsIoAcceptor() {
        if(instance.wsIoAcceptor == null){
            instance.wsIoAcceptor = new NioSocketAcceptorEx();
            instance.wsIoAcceptor.setHandler(instance.getUpstreamHandler());
            instance.wsIoAcceptor.setFilterChainBuilder(instance.getWsFilterChainBuilder());
            instance.wsIoAcceptor.setReuseAddress(true);
            instance.wsIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamWsPort())));
        }
        return instance.wsIoAcceptor;
    }

    public synchronized DefaultIoFilterChainBuilder getWsFilterChainBuilder() {
        if(instance.wsFilterChainBuilder == null){
            instance.wsFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
//            map.put("sslFilter", instance.getSslFilter());
            map.put("codecFilter", instance.getWsCodecFilter());
            instance.wsFilterChainBuilder.setFilters(map);
        }
        return instance.wsFilterChainBuilder;
    }

    public synchronized ProtocolCodecFilter getWsCodecFilter() {
        if(instance.wsCodecFilter == null){
            instance.wsCodecFilter = new ProtocolCodecFilter(getWebSocketCodecFactory());
        }
        return instance.wsCodecFilter;
    }

    public synchronized WebSocketCodecFactory getWebSocketCodecFactory() {
        if(instance.webSocketCodecFactory == null){
            instance.webSocketCodecFactory = new WebSocketCodecFactory();
        }
        return instance.webSocketCodecFactory;
    }

    public synchronized NioSocketAcceptorEx getSslTcpIoAcceptor() {
        if(instance.sslTcpIoAcceptor == null){
            instance.sslTcpIoAcceptor = new NioSocketAcceptorEx();
            instance.sslTcpIoAcceptor.setHandler(instance.getUpstreamHandler());
            instance.sslTcpIoAcceptor.setFilterChainBuilder(instance.getSslTcpFilterChainBuilder());
            instance.sslTcpIoAcceptor.setReuseAddress(true);
            instance.sslTcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamSslPort())));
        }
        return instance.sslTcpIoAcceptor;
    }

    public DefaultIoFilterChainBuilder getSslTcpFilterChainBuilder() {
        if(instance.sslTcpFilterChainBuilder == null){
            instance.sslTcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
            map.put("codecFilter", instance.getSslTcpCodecFilter());
            map.put("sslFilter", instance.getSslFilter());
            instance.sslTcpFilterChainBuilder.setFilters(map);
        }
        return instance.sslTcpFilterChainBuilder;
    }

    public synchronized SslFilter getSslFilter() {
        if(instance.sslFilter == null){
            try {
                instance.sslFilter = new SslFilter(getSslContextFactory().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance.sslFilter;
    }

    public synchronized SslContextFactory getSslContextFactory() {
        if(instance.sslContextFactory == null){
            instance.sslContextFactory = new SslContextFactory();
            try {
                instance.sslContextFactory.setKeyManagerFactoryKeyStore(instance.getKeystoreFactory().newInstance());
                instance.sslContextFactory.setProtocol("TLSV1.2");
                instance.sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
                instance.sslContextFactory.setKeyManagerFactoryKeyStorePassword(instance.getKeymanagerPwd());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance.sslContextFactory;
    }

    public synchronized KeyStoreFactory getKeystoreFactory() {
        if(instance.keystoreFactory == null){
            instance.keystoreFactory = new KeyStoreFactory();
            instance.keystoreFactory.setPassword(instance.getKeystorePwd());
            URL keystorePathUrl = null;
            try {
                keystorePathUrl = new URL(instance.getKeystorePath());
                instance.keystoreFactory.setDataUrl(keystorePathUrl);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return instance.keystoreFactory;
    }

    public synchronized ProtocolCodecFilter getSslTcpCodecFilter() {
        if(instance.sslTcpCodecFilter == null){
            instance.sslTcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return instance.sslTcpCodecFilter;
    }

    public synchronized NioSocketAcceptorEx getTcpIoAcceptor() {
        if(instance.tcpIoAcceptor == null){
            instance.tcpIoAcceptor = new NioSocketAcceptorEx();
            instance.tcpIoAcceptor.setHandler(instance.getUpstreamHandler());
            instance.tcpIoAcceptor.setFilterChainBuilder(instance.getTcpFilterChainBuilder());
            instance.tcpIoAcceptor.setReuseAddress(true);
            instance.tcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamPort())));
        }
        return instance.tcpIoAcceptor;
    }

    public synchronized GlobalLansProperties getGlobalLansProperties() {
        if(instance.globalLansProperties == null){
            instance.globalLansProperties = new GlobalLansProperties();
            instance.globalLansProperties.setPath("groovycloud.properties");
        }
        return instance.globalLansProperties;
    }
    public synchronized UpStreamHandler getUpstreamHandler() {
        if(instance.upstreamHandler == null){
            instance.upstreamHandler = new UpStreamHandler();
            instance.upstreamHandler.setReadIdleTime(720);
            instance.upstreamHandler.setWriteIdleTime(720);
        }
        return instance.upstreamHandler;
    }

    public synchronized HailProtocalCodecFactory getHailProtocalCodecFactory() {
        if(instance.hailProtocalCodecFactory == null){
            instance.hailProtocalCodecFactory = new HailProtocalCodecFactory();
        }
        return instance.hailProtocalCodecFactory;
    }

    public synchronized ProtocolCodecFilter getTcpCodecFilter() {
        if(instance.tcpCodecFilter == null){
            instance.tcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return instance.tcpCodecFilter;
    }

    public synchronized DefaultIoFilterChainBuilder getTcpFilterChainBuilder() {
        if(instance.tcpFilterChainBuilder == null){
            instance.tcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
            Map map = new LinkedHashMap();
            map.put("codecFilter", instance.getTcpCodecFilter());
            instance.tcpFilterChainBuilder.setFilters(map);
        }
        return instance.tcpFilterChainBuilder;
    }
    public static IMBeanApp getInstance(){
        if(instance == null){
            synchronized (IMBeanApp.class){
                if (instance == null){
                    instance = new IMBeanApp();
                    BeanApp.getInstance();
                }
            }
        }
        return instance;
    }
}
