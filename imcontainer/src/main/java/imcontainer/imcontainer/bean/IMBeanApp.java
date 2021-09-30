package imcontainer.imcontainer.bean;

import com.dobybros.chat.handlers.*;
import com.dobybros.chat.handlers.imextention.IMExtensionCache;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.gateway.channels.websocket.netty.WebSocketChannelInitializer;
import com.dobybros.gateway.channels.websocket.netty.WebSocketManager;
import com.dobybros.gateway.channels.websocket.netty.WebSocketProperties;
import com.dobybros.gateway.channels.websocket.netty.handler.IMWebSocketHandler;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.data.DockerStatus;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.script.ScriptManager;
import com.docker.tasks.Task;
import container.container.bean.BeanApp;
import org.apache.commons.lang.StringUtils;
import script.file.FileAdapter;
import script.file.LocalFileHandler;

import java.util.ArrayList;
import java.util.List;

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
    private WebSocketProperties webSocketProperties;
    private WebSocketManager webSocketManager;
    private WebSocketChannelInitializer webSocketChannelInitializer;

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
            instance.onlineServer.setStatus(DockerStatus.STATUS_STARTING);
            if(StringUtils.isNotBlank(instance.getScaleInstanceId())){
                instance.onlineServer.setScaleInstanceId(instance.getScaleInstanceId());
            }
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
            FileAdapter fileAdapter = null;
            if(instance.getRemotePath().startsWith("local:")){
                fileAdapter = new LocalFileHandler();
                ((LocalFileHandler)fileAdapter).setRootPath("");
                instance.scriptManager.setRemotePath(instance.getRemotePath().split("local:")[1]);
            }else {
                fileAdapter = getFileAdapter();
                instance.scriptManager.setRemotePath(instance.getRemotePath());
            }
            instance.scriptManager.setFileAdapter(fileAdapter);
            instance.scriptManager.setBaseRuntimeClass(GatewayGroovyRuntime.class);
            instance.scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
            instance.scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            instance.scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            instance.scriptManager.setUseHulkAdmin(Boolean.valueOf(instance.getUseHulkAdmin()));
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

    public synchronized GlobalLansProperties getGlobalLansProperties() {
        if(instance.globalLansProperties == null){
            instance.globalLansProperties = new GlobalLansProperties();
            instance.globalLansProperties.setPath("groovycloud.properties");
        }
        return instance.globalLansProperties;
    }

    /********** netty websocket start *********/
    public synchronized WebSocketProperties getWebSocketProperties() {
        if (instance.webSocketProperties == null) {
            instance.webSocketProperties = new WebSocketProperties(Integer.valueOf(instance.getPublicWsPort()),
                    Integer.valueOf(instance.getUpstreamWsPort()));
        }
        return instance.webSocketProperties;
    }

    public synchronized WebSocketChannelInitializer getWebSocketChannelInitializer() {
        if (instance.webSocketChannelInitializer == null)
            instance.webSocketChannelInitializer = new WebSocketChannelInitializer(instance.getWebSocketProperties(), IMWebSocketHandler.class);
        return instance.webSocketChannelInitializer;
    }

    public synchronized WebSocketManager getWebSocketManager() {
        if (instance.webSocketManager == null) {
            instance.webSocketManager = new WebSocketManager(instance.getWebSocketProperties(), instance.getWebSocketChannelInitializer());
        }
        return instance.webSocketManager;
    }
    /********** netty websocket end *********/



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
