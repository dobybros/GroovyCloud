package imcontainer.imcontainer.bean;

import com.dobybros.chat.handlers.ProxyContainerDuplexSender;
import com.dobybros.chat.handlers.QueueProxyContainerDuplexSender;
import com.dobybros.chat.handlers.RpcProxyContainerDuplexSender;
import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;

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

    @Bean
    public UpStreamHandler upstreamHandler() {
        return instance.getUpstreamHandler();
    }

    @Bean
    public CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> map = new HashMap<>();
        map.put(java.net.SocketAddress.class, org.apache.mina.integration.beans.InetSocketAddressEditor.class);
        customEditorConfigurer.setCustomEditors(map);
        return customEditorConfigurer;
    }

    @Bean
    public ProtocolCodecFilter tcpCodecFilter() {
        return instance.getTcpCodecFilter();
    }

    //TODO 检查
    @Bean
    public DefaultIoFilterChainBuilder tcpFilterChainBuilder() {
        return instance.getTcpFilterChainBuilder();
    }

    @Bean(destroyMethod = "unbind")
    public NioSocketAcceptorEx tcpIoAcceptor() {
        return instance.getTcpIoAcceptor();
    }

    @Bean
    public HailProtocalCodecFactory hailProtocalCodecFactory() {
        return instance.getHailProtocalCodecFactory();
    }

    @Bean
    public ProtocolCodecFilter sslTcpCodecFilter() {
        return instance.getSslTcpCodecFilter();
    }

    @Bean
    public KeyStoreFactory keystoreFactory() {
        return instance.getKeystoreFactory();
    }
    @Bean
    public SslContextFactory sslContextFactory() {
        return instance.getSslContextFactory();
    }
    @Bean
    public SslFilter sslFilter() {
        return instance.getSslFilter();
    }
    @Bean
    public DefaultIoFilterChainBuilder sslTcpFilterChainBuilder() {
        return instance.getSslTcpFilterChainBuilder();
    }
    @Bean(destroyMethod = "unbind")
    public NioSocketAcceptorEx sslTcpIoAcceptor() {
        return instance.getSslTcpIoAcceptor();
    }
    @Bean
    public ProtocolCodecFilter wsCodecFilter() {
        return instance.getWsCodecFilter();
    }
    @Bean
    public DefaultIoFilterChainBuilder wsFilterChainBuilder() {
        return instance.getWsFilterChainBuilder();
    }
    @Bean
    public NioSocketAcceptorEx wsIoAcceptor() {
        return instance.getWsIoAcceptor();
    }
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
}