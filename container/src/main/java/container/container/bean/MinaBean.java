package container.container.bean;

import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import groovy.util.logging.Log4j;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 17:37
 */
@Configuration
public class MinaBean {
    private BeanApp instance;
    MinaBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    public UpStreamAnnotationHandler upStreamAnnotationHandler() {
        return new UpStreamAnnotationHandler();
    }

    @Bean
    public UpStreamHandler upstreamHandler() {
        UpStreamHandler upStreamHandler = instance.getUpstreamHandler();
        upStreamHandler.setReadIdleTime(720);
        upStreamHandler.setWriteIdleTime(720);
        return upStreamHandler;
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
        DefaultIoFilterChainBuilder tcpFilterChainBuilder = instance.getTcpFilterChainBuilder();
        Map map = new LinkedHashMap();
        map.put("codecFilter", instance.getTcpCodecFilter());
        tcpFilterChainBuilder.setFilters(map);
        return tcpFilterChainBuilder;
    }

//    @Bean(initMethod = "bind", destroyMethod = "unbind")
    @Bean(destroyMethod = "unbind")
    public NioSocketAcceptorEx tcpIoAcceptor() {
        NioSocketAcceptorEx tcpIoAcceptor = instance.getTcpIoAcceptor();
        tcpIoAcceptor.setHandler(instance.getUpstreamHandler());
        tcpIoAcceptor.setFilterChainBuilder(instance.getTcpFilterChainBuilder());
        tcpIoAcceptor.setReuseAddress(true);
        tcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamPort())));
        return tcpIoAcceptor;
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
        KeyStoreFactory keyStoreFactory = instance.getKeystoreFactory();
        keyStoreFactory.setPassword(instance.getKeystorePwd());
        URL keystorePathUrl = null;
        try {
            keystorePathUrl = new URL(instance.getKeystorePath());
            if (keystorePathUrl != null) {
                keyStoreFactory.setDataUrl(keystorePathUrl);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return keyStoreFactory;
    }
    @Bean
    public SslContextFactory sslContextFactory() {
        SslContextFactory sslContextFactory = instance.getSslContextFactory();
        try {
            sslContextFactory.setKeyManagerFactoryKeyStore(instance.getKeystoreFactory().newInstance());
            sslContextFactory.setProtocol("TLSV1.2");
            sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
            sslContextFactory.setKeyManagerFactoryKeyStorePassword(instance.getKeymanagerPwd());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContextFactory;
    }
    @Bean
    public SslFilter sslFilter() {
        return instance.getSslFilter();
    }
    @Bean
    public DefaultIoFilterChainBuilder sslTcpFilterChainBuilder() {
        DefaultIoFilterChainBuilder defaultIoFilterChainBuilder = instance.getSslTcpFilterChainBuilder();
        Map map = new LinkedHashMap();
        map.put("codecFilter", instance.getSslTcpCodecFilter());
        map.put("sslFilter", instance.getSslFilter());
        defaultIoFilterChainBuilder.setFilters(map);
        return defaultIoFilterChainBuilder;
    }
//    @Bean(initMethod = "bind", destroyMethod = "unbind")
    @Bean(destroyMethod = "unbind")
    public NioSocketAcceptorEx sslTcpIoAcceptor() {
        NioSocketAcceptorEx sslTcpIoAcceptor = instance.getSslTcpIoAcceptor();
        sslTcpIoAcceptor.setHandler(instance.getUpstreamHandler());
        sslTcpIoAcceptor.setFilterChainBuilder(instance.getSslTcpFilterChainBuilder());
        sslTcpIoAcceptor.setReuseAddress(true);
        sslTcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamSslPort())));
        return sslTcpIoAcceptor;
    }
    @Bean
    public ProtocolCodecFilter wsCodecFilter() {
        return instance.getWsCodecFilter();
    }
    @Bean
    public DefaultIoFilterChainBuilder wsFilterChainBuilder() {
        DefaultIoFilterChainBuilder wsFilterChainBuilder = instance.getWsFilterChainBuilder();
        Map map = new LinkedHashMap();
        map.put("codecFilter", instance.getWsCodecFilter());
        map.put("sslFilter", instance.getSslFilter());
        wsFilterChainBuilder.setFilters(map);
        return wsFilterChainBuilder;
    }
    @Bean
    public NioSocketAcceptorEx wsIoAcceptor() {
        NioSocketAcceptorEx wsIoAcceptor = instance.getWsIoAcceptor();
        wsIoAcceptor.setHandler(instance.getUpstreamHandler());
        wsIoAcceptor.setFilterChainBuilder(instance.getWsFilterChainBuilder());
        wsIoAcceptor.setReuseAddress(true);
        wsIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamWsPort())));
        return wsIoAcceptor;
    }
}