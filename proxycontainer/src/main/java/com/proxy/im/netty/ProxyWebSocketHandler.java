package com.proxy.im.netty;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.websocket.data.NettyChannelContext;
import com.dobybros.gateway.channels.websocket.netty.codec.WebSocketFrameCodec;
import com.dobybros.gateway.channels.websocket.netty.handler.AbstractWebSocketServerHandler;
import com.docker.utils.SpringContextUtil;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.ProxyMessageReceivedListener;
import com.proxy.im.ProxySessionListener;
import com.proxy.im.ProxyUpStreamAnnotationHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.object.GroovyObjectEx;

import java.util.List;

/**
 * Created by hzj on 2021/9/29 下午3:39
 */
public class ProxyWebSocketHandler extends AbstractWebSocketServerHandler {

    private static final String TAG = ProxyWebSocketHandler.class.getSimpleName();

    ProxyAnnotationHandler proxyAnnotationHandler = (ProxyAnnotationHandler) SpringContextUtil.getBean("proxyAnnotationHandler");
    ProxyUpStreamAnnotationHandler proxyUpStreamAnnotationHandler = (ProxyUpStreamAnnotationHandler) SpringContextUtil.getBean("proxyUpStreamAnnotationHandler");

    public ProxyWebSocketHandler(boolean ssl) {
        super(ssl);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionCreated(NettyChannelContext.getNettyChannelContextByCtx(ctx));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionCreated error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionClosed(NettyChannelContext.getNettyChannelContextByCtx(ctx));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionClosed error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().sessionIdle(NettyChannelContext.getNettyChannelContextByCtx(ctx));
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "SessionIdle error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        for (GroovyObjectEx<ProxySessionListener> listener : proxyAnnotationHandler.getTcpListeners()) {
            try {
                listener.getObject().exceptionCaught(NettyChannelContext.getNettyChannelContextByCtx(ctx), cause);
            } catch (Throwable throwable) {
                LoggerEx.error(TAG, "ExceptionCaught error, class: " + listener.getObject().getClass() + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
            }
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, BinaryWebSocketFrame webSocketFrame) throws Exception {
        NettyChannelContext context = NettyChannelContext.getNettyChannelContextByCtx(ctx);
        Data data = null;
        try {
            data = WebSocketFrameCodec.decode(webSocketFrame, context);
            if (data != null) {
                List<GroovyObjectEx<ProxyMessageReceivedListener>> listeners = proxyUpStreamAnnotationHandler.getProxyMessageReceivedListeners();
                if (listeners != null && !listeners.isEmpty()) {
                    for (GroovyObjectEx<ProxyMessageReceivedListener> listener : listeners){
                        try {
                            listener.getObject().messageReceived(data, context);
                        }catch (Throwable throwable){
                            LoggerEx.error(TAG, "MessageReceived error, class: " + listener.getObject().getClass() + ",data: " + data + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "message received, handle error, eMsg: " + ExceptionUtils.getFullStackTrace(t));
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame webSocketFrame) throws Exception {
        NettyChannelContext context = NettyChannelContext.getNettyChannelContextByCtx(ctx);
        WebSocketFrameCodec.decode(webSocketFrame, context);
    }
}
