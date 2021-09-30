package com.dobybros.gateway.channels.websocket.netty;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hzj on 2021/9/28 下午2:27
 */
public class WebSocketManager {

    public static final String TAG = WebSocketManager.class.getSimpleName();

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private WebSocketProperties webSocketProperties;
    private AtomicBoolean started = new AtomicBoolean(false);
    private WebSocketChannelInitializer webSocketChannelInitializer;

    public WebSocketManager(WebSocketProperties properties, WebSocketChannelInitializer initializer) {
        webSocketProperties = properties;
        webSocketChannelInitializer = initializer;
    }

    public void start() throws CoreException {
        if (started.compareAndSet(false, true)) {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .option(ChannelOption.SO_BACKLOG, webSocketProperties.getBacklog())     // 连接数
                        .childOption(ChannelOption.TCP_NODELAY, true)                       // 不延迟，消息立即发送
                        .childHandler(webSocketChannelInitializer);       // 消息处理者
                ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
                bootstrap.bind(webSocketProperties.getPort()).sync();
                LoggerEx.info(TAG, "Websocket server started at port " + webSocketProperties.getPort());
            } catch (Throwable t) {
                started.set(false);
                LoggerEx.fatal(TAG, "Websocket server start failed, eMsg: " + t.getMessage());
                throw new CoreException(IMCoreErrorCodes.ERROR_START_WEBSOCKET_FAILED, "Websocket server start failed, eMsg: " + t.getMessage());
            }
        } else {
            LoggerEx.warn(TAG, "Websocket server is starting, so ignore");
        }
    }

    public void close() {
        if (started.get()) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
