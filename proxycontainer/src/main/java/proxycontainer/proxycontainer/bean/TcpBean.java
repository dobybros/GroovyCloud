package proxycontainer.proxycontainer.bean;

import com.dobybros.chat.handlers.ProxyContainerDuplexSender;
import com.dobybros.chat.handlers.RpcProxyContainerDuplexSender;
import com.dobybros.gateway.channels.websocket.netty.WebSocketChannelInitializer;
import com.dobybros.gateway.channels.websocket.netty.WebSocketManager;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.ProxyUpStreamAnnotationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 17:37
 */
@Configuration
public class TcpBean {
    private ProxyBeanApp instance;
    TcpBean(){
        instance = ProxyBeanApp.getInstance();
    }
    @Bean
    public ProxyUpStreamAnnotationHandler proxyUpStreamAnnotationHandler() {
        return new ProxyUpStreamAnnotationHandler();
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
    public ProxyAnnotationHandler proxyAnnotationHandler(){
        return instance.getProxyAnnotationHandler();
    }
    @Bean
    public ProxyContainerDuplexSender proxyContainerDuplexSender(){
        return instance.getProxyContainerDuplexSender();
    }
    @Bean
    public RpcProxyContainerDuplexSender rpcProxyContainerDuplexSender(){
        return instance.getRpcProxyContainerDuplexSender();
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