package container.container.bean;

import chat.utils.IPHolder;
import com.dobybros.chat.handlers.ConsumeOfflineMessageHandler;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.chat.utils.AutoReloadProperties;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.script.ScriptManager;
import com.docker.tasks.Task;
import com.docker.utils.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

import java.util.ArrayList;
import java.util.List;

//import com.dobybros.chat.log.LogIndexQueue;

/**
 * Created by lick on 2019/5/27.
 * Description：
 */
@Configuration
public class CommonBean {
    private BeanApp instance;
    CommonBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    public SpringContextUtil springContextUtil() {
        return instance.getSpringContextUtil();
    }
    //    @Bean
//    public ConsumeQueueService bulkLogQueueService(){
//        ConsumeQueueService bulkLogQueueService = getBulkLogQueueService();
//        bulkLogQueueService.setDao(getBulkLogDAO());
//        return bulkLogQueueService;
//    }
//    @Bean(initMethod = "init")
//    public LogIndexQueue logIndexQueue(){
//        return getLogIndexQueue();
//    }
//    @Bean(initMethod = "init")
    @Bean
    public IPHolder ipHolder() {
        IPHolder ipHolder = instance.getIpHolder();
        ipHolder.setEthPrefix(instance.getEthPrefix());
        ipHolder.setIpPrefix(instance.getIpPrefix());
        return ipHolder;
    }

//    @Bean(initMethod = "init")
    @Bean
    public AutoReloadProperties oauth2ClientProperties() {
        return instance.getOauth2ClientProperties();
    }

    @Bean
    public ConsumeOfflineMessageHandler consumeOfflineMessageHandler() {
        return instance.getConsumeOfflineMessageHandler();
    }

    @Bean
    public OfflineMessageSavingTask offlineMessageSavingTask() {
        return instance.getOfflineMessageSavingTask();
    }

    @Bean
    public RPCMessageSendingTask messageSendingTask() {
        RPCMessageSendingTask messageSendingTask = instance.getMessageSendingTask();
        messageSendingTask.setNumOfThreads(4);
        return messageSendingTask;
    }

    @Bean
    public JsonFilterFactory jsonFilterFactory() {
        return instance.getJsonFilterFactory();
    }

    @Bean
    public RequestPermissionHandler requestPermissionHandler() {
        return instance.getRequestPermissionHandler();
    }

//    @Bean(initMethod = "init", destroyMethod = "shutdown")
    @Bean(destroyMethod = "shutdown")
    public ScriptManager scriptManager() {
        ScriptManager scriptManager = instance.getScriptManager();
        scriptManager.setLocalPath(instance.getLocalPath());
        scriptManager.setRemotePath(instance.getRemotePath());
        scriptManager.setBaseRuntimeClass(com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime.class);
        scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
        scriptManager.setDockerStatusService(instance.getDockerStatusService());
        scriptManager.setFileAdapter(instance.getFileAdapter());
        scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
        return scriptManager;
    }

//    @Bean(initMethod = "init")
    @Bean
    public OnlineUserManagerImpl onlineUserManager() {
        OnlineUserManagerImpl onlineUserManager = instance.getOnlineUserManager();
        onlineUserManager.setAdminOnlineUserClass(com.dobybros.gateway.onlineusers.impl.AdminOnlineUserImpl.class);
        return onlineUserManager;
    }

//    @Bean(initMethod = "start", destroyMethod = "shutdown")
    @Bean
    public OnlineServerWithStatus onlineServer() {
        OnlineServerWithStatus onlineServer = instance.getOnlineServer();
        onlineServer.setDockerStatusService(instance.getDockerStatusService());
        List<Task> tasks = new ArrayList<>();
        tasks.add(instance.getMessageSendingTask());
        tasks.add(instance.getOfflineMessageSavingTask());
        tasks.add(instance.getRpcClientAdapterMapTask());
        tasks.add(instance.getRpcClientAdapterMapTaskSsl());
        onlineServer.setTasks(tasks);
        onlineServer.setServerType(instance.getServerType());
        onlineServer.setHttpPort(Integer.valueOf(instance.getServerPort()));
        onlineServer.setInternalKey(instance.getInternalKey());
        onlineServer.setRpcPort(instance.getRpcPort());
        onlineServer.setSslRpcPort(instance.getSslRpcPort());
        onlineServer.setPublicDomain(instance.getPublicDomain());
        onlineServer.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
        onlineServer.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
        onlineServer.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        onlineServer.setMaxUsers(Integer.valueOf(instance.getMaxUsers()));
        onlineServer.setDockerRpcPort(instance.getDockerRpcPort());
        onlineServer.setDockerSslRpcPort(instance.getDockerSslRpcPort());
        onlineServer.setTcpPort(instance.getUpstreamPort());
        onlineServer.setSslRpcPort(instance.getUpstreamSslPort());
        onlineServer.setWsPort(instance.getUpstreamWsPort());
        onlineServer.setStatus(1);
        onlineServer.setConfigPath("container.properties");
        onlineServer.setIpHolder(instance.getIpHolder());
        return onlineServer;
    }
}
