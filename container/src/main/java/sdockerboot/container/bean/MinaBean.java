package sdockerboot.container.bean;

import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 17:37
 */
@Configuration
public class MinaBean extends BeanApp {
    @Bean
    public UpStreamAnnotationHandler upStreamAnnotationHandler() {
        return new UpStreamAnnotationHandler();
    }

    @Bean
    public UpStreamHandler upstreamHandler() {
        UpStreamHandler upStreamHandler = getUpstreamHandler();
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
        return getTcpCodecFilter();
    }

    //TODO 检查
    @Bean
    public DefaultIoFilterChainBuilder tcpFilterChainBuilder() {
        DefaultIoFilterChainBuilder tcpFilterChainBuilder = getTcpFilterChainBuilder();
        Map map = new HashMap();
        map.put("codecFilter", getTcpCodecFilter());
        tcpFilterChainBuilder.setFilters(map);
        return tcpFilterChainBuilder;
    }

    @Bean(initMethod = "bind", destroyMethod = "unbind")
    public NioSocketAcceptorEx tcpIoAcceptor() {
        NioSocketAcceptorEx tcpIoAcceptor = getTcpIoAcceptor();
        tcpIoAcceptor.setHandler(getUpstreamHandler());
        tcpIoAcceptor.setFilterChainBuilder(getTcpFilterChainBuilder());
        tcpIoAcceptor.setReuseAddress(true);
        tcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(getUpstreamPort())));
        return tcpIoAcceptor;
    }

    @Bean
    public HailProtocalCodecFactory hailProtocalCodecFactory() {
        return getHailProtocalCodecFactory();
    }

    @Bean
    public ProtocolCodecFilter sslTcpCodecFilter() {
        return getSslTcpCodecFilter();
    }

    @Bean
    public KeyStoreFactory keystoreFactory() {
        KeyStoreFactory keyStoreFactory = getKeystoreFactory();
        keyStoreFactory.setPassword(getKeystorePwd());
        URL keystorePathUrl = null;
        try {
            keystorePathUrl = new URL(getKeystorePath());
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
        SslContextFactory sslContextFactory = getSslContextFactory();
        try {
            sslContextFactory.setKeyManagerFactoryKeyStore(getKeystoreFactory().newInstance());
            sslContextFactory.setProtocol("TLSV1.2");
            sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
            sslContextFactory.setKeyManagerFactoryKeyStorePassword(getKeymanagerPwd());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContextFactory;
    }
    @Bean
    public SslFilter sslFilter() {
        return getSslFilter();
    }
    @Bean
    public DefaultIoFilterChainBuilder sslTcpFilterChainBuilder() {
        DefaultIoFilterChainBuilder defaultIoFilterChainBuilder = getSslTcpFilterChainBuilder();
        Map map = new HashMap();
        map.put("codecFilter", getSslTcpCodecFilter());
        map.put("sslFilter", getSslFilter());
        defaultIoFilterChainBuilder.setFilters(map);
        return defaultIoFilterChainBuilder;
    }
    @Bean(initMethod = "bind", destroyMethod = "unbind")
    public NioSocketAcceptorEx sslTcpIoAcceptor() {
        NioSocketAcceptorEx sslTcpIoAcceptor = getSslTcpIoAcceptor();
        sslTcpIoAcceptor.setHandler(getUpstreamHandler());
        sslTcpIoAcceptor.setFilterChainBuilder(getSslTcpFilterChainBuilder());
        sslTcpIoAcceptor.setReuseAddress(true);
        sslTcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(getUpstreamSslPort())));
        return sslTcpIoAcceptor;
    }
}