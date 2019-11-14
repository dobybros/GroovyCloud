package proxycontainer.proxycontainer.bean;

import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.proxy.im.ProxyUpStreamHandler;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.ProxyUpStreamAnnotationHandler;
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
    private ProxyBeanApp instance;
    TcpBean(){
        instance = ProxyBeanApp.getInstance();
    }
    @Bean
    public ProxyUpStreamAnnotationHandler tcpUpStreamAnnotationHandler() {
        return new ProxyUpStreamAnnotationHandler();
    }

    @Bean
    public ProxyUpStreamHandler upstreamHandler() {
        return instance.getProxyUpStreamHandler();
    }

    @Bean
    public CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> map = new HashMap();
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
    public ProxyAnnotationHandler tcpAnnotationHandler(){
        return instance.getProxyAnnotationHandler();
    }
}