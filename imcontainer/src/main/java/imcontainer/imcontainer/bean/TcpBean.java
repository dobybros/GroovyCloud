package imcontainer.imcontainer.bean;

import com.dobybros.chat.handlers.ProxyContainerDuplexSender;
import com.dobybros.chat.handlers.QueueProxyContainerDuplexSender;
import com.dobybros.chat.handlers.RpcProxyContainerDuplexSender;
import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.websocket.netty.WebSocketChannelInitializer;
import com.dobybros.gateway.channels.websocket.netty.WebSocketManager;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 17:37
 */
@Configuration
public class TcpBean {
    private IMBeanApp instance;
    TcpBean(){
        instance = IMBeanApp.getInstance();
    }
    @Bean
    public UpStreamAnnotationHandler upStreamAnnotationHandler() {
        return new UpStreamAnnotationHandler();
    }

//    @Bean
//    public CustomEditorConfigurer customEditorConfigurer() {
//        CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
//        Map<Class<?>, Class<? extends PropertyEditor>> map = new HashMap<>();
//        map.put(java.net.SocketAddress.class, org.apache.mina.integration.beans.InetSocketAddressEditor.class);
//        customEditorConfigurer.setCustomEditors(map);
//        return customEditorConfigurer;
//    }

    @Bean
    public MessageEventHandler messageEventHandler(){
        return instance.getMessageEventHandler();
    }
    @Bean
    public ProxyContainerDuplexSender proxyContainerDuplexSender(){
        return instance.getProxyContainerDuplexSender();
    }
    @Bean
    public RpcProxyContainerDuplexSender rpcProxyContainerDuplexSender(){
        return instance.getRpcProxyContainerDuplexSender();
    }
    @Bean
    public QueueProxyContainerDuplexSender queueProxyContainerDuplexSender(){
        return instance.getQueueProxyContainerDuplexSender();
    }
    /*   netty websocket start   */
    @Bean
    public WebSocketChannelInitializer webSocketChannelInitializer() {
        return instance.getWebSocketChannelInitializer();
    }
    @Bean(destroyMethod = "close")
    public WebSocketManager webSocketManager() {
        return instance.getWebSocketManager();
    }
    /*   netty websocket end   */
}