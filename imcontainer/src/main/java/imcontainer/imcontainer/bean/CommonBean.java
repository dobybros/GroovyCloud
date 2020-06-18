package imcontainer.imcontainer.bean;

import chat.utils.IPHolder;
import com.dobybros.chat.handlers.ConsumeOfflineMessageHandler;
import com.dobybros.chat.handlers.PingHandler;
import com.dobybros.chat.handlers.imextention.IMExtensionCache;
import com.dobybros.chat.tasks.OfflineMessageSavingTask;
import com.dobybros.chat.tasks.RPCMessageSendingTask;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.script.ScriptManager;
import com.docker.tasks.RepairTaskHandler;
import com.docker.utils.AutoReloadProperties;
import com.docker.utils.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

//import com.dobybros.chat.log.LogIndexQueue;

/**
 * Created by lick on 2019/5/27.
 * Description：
 */
@Configuration
public class CommonBean {
    private IMBeanApp instance;
    CommonBean(){
        instance = IMBeanApp.getInstance();
    }
    @Bean
    public SpringContextUtil springContextUtil() {
        return instance.getSpringContextUtil();
    }

    @Bean
    public IPHolder ipHolder() {
        return instance.getIpHolder();
    }

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
        return instance.getMessageSendingTask();
    }

    @Bean
    public JsonFilterFactory jsonFilterFactory() {
        return instance.getJsonFilterFactory();
    }

    @Bean
    public RequestPermissionHandler requestPermissionHandler() {
        return instance.getRequestPermissionHandler();
    }

    @Bean(destroyMethod = "shutdown")
    public ScriptManager scriptManager() {
        return instance.getScriptManager();
    }

    @Bean(destroyMethod = "shutdown")
    public OnlineUserManagerImpl onlineUserManager() {
        return instance.getOnlineUserManager();
    }

    @Bean
    public OnlineServerWithStatus onlineServer() {
        return instance.getOnlineServer();
    }
    @Bean
    public PingHandler pingHandler(){
        return instance.getPingHandler();
    }
    @Bean(initMethod = "init")
    public IMExtensionCache imExtensionCache(){
        return instance.getIMExtensionCache();
    }
    @Bean
    public RepairTaskHandler repairTaskHandler(){return instance.getRepairTaskHandler();}
}
