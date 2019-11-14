package container.container.bean;

import chat.utils.IPHolder;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.script.ScriptManager;
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
    private BeanApp instance;
    CommonBean(){
        instance = BeanApp.getInstance();
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

    @Bean
    public OnlineServerWithStatus onlineServer() {
        return instance.getOnlineServer();
    }

}
