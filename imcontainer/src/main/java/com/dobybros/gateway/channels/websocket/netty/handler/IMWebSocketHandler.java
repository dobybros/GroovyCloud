package com.dobybros.gateway.channels.websocket.netty.handler;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.channels.Channel;
import com.dobybros.gateway.channels.msgs.MessageReceivedListener;
import com.dobybros.gateway.channels.tcp.UpStreamAnnotationHandler;
import com.dobybros.gateway.channels.websocket.data.NettyChannelContext;
import com.dobybros.gateway.channels.websocket.netty.codec.WebSocketFrameCodec;
import com.dobybros.gateway.errors.GatewayErrorCodes;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.dobybros.gateway.pack.Pack;
import com.docker.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import script.groovy.object.GroovyObjectEx;

/**
 * Created by hzj on 2021/9/28 下午3:49
 */
public class IMWebSocketHandler extends AbstractWebSocketServerHandler {

    private static final String TAG = IMWebSocketHandler.class.getSimpleName();

    public static final String ATTRIBUTE_VERSION = "VERSION";

    private OnlineUserManager onlineUserManager = (OnlineUserManager)SpringContextUtil.getBean("onlineUserManager");
    private UpStreamAnnotationHandler upStreamAnnotationHandler = (UpStreamAnnotationHandler)SpringContextUtil.getBean("upStreamAnnotationHandler");


    public IMWebSocketHandler(boolean ssl) {
        super(ssl);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeUserChannel(ctx, Channel.ChannelListener.CLOSE, null);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        NettyChannelContext nettyChannelContext = closeContext(ctx);
        LoggerEx.info(TAG, "user event triggered, nettyChannelContext" + nettyChannelContext + ", evt: " + evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            closeUserChannel(ctx, Channel.ChannelListener.CLOSE_ERROR, cause);
        } finally {
            closeContext(ctx);
        }
    }


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, BinaryWebSocketFrame webSocketFrame) throws Exception {
        NettyChannelContext context = NettyChannelContext.getNettyChannelContextByCtx(ctx);
        Data data = null;
        try {
            data = WebSocketFrameCodec.decode(webSocketFrame, context);
            if (data != null) {
                Byte type = data.getType();
                if (type != Pack.TYPE_IN_IDENTITY && !context.getReadIdentity().get())
                    throw new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_MISSING_ONLINEUSER, "Online user is missing for receiving message");
                GroovyObjectEx<MessageReceivedListener> listener = upStreamAnnotationHandler.getMessageReceivedMap().get(type);
                if(listener == null)
                    listener = upStreamAnnotationHandler.getMessageReceivedMap().get(type);
                if(listener != null) {
                    Class<? extends Data> dataClass = listener.getObject().getDataClass();
                    if(dataClass != null) {
                        listener.getObject().messageReceived(data, context);
                    }
                }
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "Message " + data + " received failed, " + t + " eMsg: " + t.getMessage());
            CoreException coreException = null;
            if(t instanceof CoreException)
                coreException = (CoreException) t;
            if(coreException == null)
                coreException = new CoreException(GatewayErrorCodes.ERROR_TCPCHANNEL_UNKNOWN, "Unknown error occured while receiving message from tcp channel, channel context " + context + " message " + data + " error " + t.getMessage());
            if(coreException.getCode() <= GatewayErrorCodes.TCPCHANNEL_CLOSE_START && coreException.getCode() > GatewayErrorCodes.TCPCHANNEL_CLOSE_END){
                boolean closeSuccess = closeUserChannel(ctx, Channel.ChannelListener.CLOSE_ERROR, null);
                if(!closeSuccess){
                    context.close();
                }
            } else if(coreException.getCode() <= GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_START && coreException.getCode() > GatewayErrorCodes.TCPCHANNEL_CLOSE_IMMEDIATELY_END){
                boolean closeSuccess = closeUserChannel(ctx, Channel.ChannelListener.CLOSE_ERROR, null);
                if(!closeSuccess){
                    context.close();
                }
            } else {
                context.close();
            }
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame webSocketFrame) throws Exception {
        NettyChannelContext context = NettyChannelContext.getNettyChannelContextByCtx(ctx);
        WebSocketFrameCodec.decode(webSocketFrame, context);
    }

    private NettyChannelContext closeContext(ChannelHandlerContext ctx) {
        if (ctx != null) {
            NettyChannelContext nettyChannelContext = NettyChannelContext.getNettyChannelContextByCtx(ctx);
            if (nettyChannelContext != null)
                nettyChannelContext.close();
            return nettyChannelContext;
        }
        return null;
    }

    private Boolean closeUserChannel(ChannelHandlerContext ctx, int closeError, Throwable exceptionCause) throws CoreException {
        if (ctx != null) {
            NettyChannelContext nettyChannelContext = NettyChannelContext.getNettyChannelContextByCtx(ctx);
            if (nettyChannelContext != null) {
                OnlineUser onlineUser = onlineUserManager.getOnlineUser(nettyChannelContext.getUserId());
                if (onlineUser != null) {
                    OnlineServiceUser onlineServiceUser = onlineUser.getOnlineServiceUser(nettyChannelContext.getService());
                    if(onlineServiceUser != null){
                        Channel channel = onlineServiceUser.getChannel(nettyChannelContext.getTerminal());
                        if(channel != null) {
                            if (exceptionCause != null) {
                                Channel.ChannelListener listener = channel.getChannelListener();
                                if(listener != null)
                                    try {
                                        listener.exceptionCaught(exceptionCause);
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                        LoggerEx.error(TAG, "TcpChannel exceptionCaught " + exceptionCause + "|" + exceptionCause.getMessage() + " occur error " + e.getMessage() + " channel " + channel);
                                    }
                            }
                            onlineUser.removeChannel(channel, closeError);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
