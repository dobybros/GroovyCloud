package com.dobybros.gateway.channels.websocket.netty;

import chat.logs.LoggerEx;
import com.dobybros.gateway.channels.websocket.netty.handler.AbstractWebSocketServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * Created by hzj on 2021/9/28 下午3:44
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final String TAG = WebSocketChannelInitializer.class.getSimpleName();

    private WebSocketProperties webSocketProperties;

    private Class<? extends AbstractWebSocketServerHandler> webSocketHandlerClass;

    // todo ssl 只做准备，未实现
    private SslContext sslContext;

    public WebSocketChannelInitializer(WebSocketProperties properties, Class<? extends AbstractWebSocketServerHandler> handlerClass) {
        webSocketProperties = properties;
        if (this.webSocketProperties.isSsl()) {
            this.createSSL();
        }
        webSocketHandlerClass = handlerClass;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (this.webSocketProperties.isSsl() && this.sslContext != null) {
            pipeline.addLast(this.sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new IdleStateHandler(this.webSocketProperties.getReadIdleTime(),
                this.webSocketProperties.getWriteIdleTime(), this.webSocketProperties.getAllIdleTime(), TimeUnit.MINUTES));
        Constructor<? extends AbstractWebSocketServerHandler> constructor = webSocketHandlerClass.getDeclaredConstructor(boolean.class);
        AbstractWebSocketServerHandler webSocketHandler = constructor.newInstance(webSocketProperties.isSsl());
        pipeline.addLast(webSocketHandler);
    }

    private void createSSL() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksInputStream = new FileInputStream(ResourceUtils.getFile("classpath:gateserver.jks"));
            ks.load(ksInputStream, "123456".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "123456".toCharArray());
            this.sslContext = SslContextBuilder.forServer(kmf).clientAuth(ClientAuth.NONE).build();
        } catch (Throwable t) {
            LoggerEx.error(TAG, "create ssl error, eMsg: " + t.getMessage());
        }
    }

}
