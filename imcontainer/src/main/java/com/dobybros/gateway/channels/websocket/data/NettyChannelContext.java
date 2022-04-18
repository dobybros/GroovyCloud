package com.dobybros.gateway.channels.websocket.data;

import chat.logs.LoggerEx;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.pack.HailPack;
import com.dobybros.gateway.pack.Pack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hzj on 2021/9/28 下午7:10
 */
public class NettyChannelContext extends ChannelContext {

    public static final String TAG = NettyChannelContext.class.getSimpleName();
    public static final AttributeKey<NettyChannelContext> NETTY_CHANNEL_CONTEXT_KEY = AttributeKey.valueOf("nettyChannelContext");
    public static final AttributeKey<String> NETTY_CHANNEL_REAL_IP_KEY = AttributeKey.valueOf("nettyChannelRealIp");

    private ChannelHandlerContext channelHandlerContext;

    private AtomicBoolean readServer = new AtomicBoolean(false);
    private AtomicBoolean readIdentity = new AtomicBoolean(false);

    /**
     * 1(type) + 4(length) + content
     */
    @Override
    public void write(Pack pack) {
        ByteBuf byteBuf = Unpooled.directBuffer(1 + 4 + pack.getLength());
        try {
            pack.persistentToByteBuf(byteBuf);
            if (channelIsActive()) {
                channelHandlerContext.channel().writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "write pack error, eMsg: " + ExceptionUtils.getFullStackTrace(t));
            byteBuf.release();
        }

    }

    @Override
    public void write(byte[] data, byte type) {
        if (channelIsActive()) {
            Pack pack = new HailPack();
            pack.setData(data, type);
            write(pack);
        }
    }

    @Override
    public void write(int code, String description, String forId) {
        if (channelIsActive()) {
            Pack hailPack = DataVersioning.getResult(this, code, description, forId);
            write(hailPack);
        }
    }

    @Override
    public void close() {
        releaseCloseChannelTask();
        try {
            if (channelIsActive()) {
                channelHandlerContext.channel().close();
            }
        } catch (Throwable t) {
            LoggerEx.error(TAG, "close netty channel context error, eMsg: " + t.getMessage());
        }
    }

    @Override
    public String getContextIp() {
        try {
            if (channelHandlerContext != null) {
                Attribute<String> attribute = channelHandlerContext.attr(NettyChannelContext.NETTY_CHANNEL_REAL_IP_KEY);
                String ip = attribute.get();
                if (StringUtils.isBlank(ip)) {
                    ip = ((InetSocketAddress)channelHandlerContext.channel().remoteAddress()).getAddress().getHostAddress();
                }
                return ip;
            }
        } catch (Throwable t) {
            LoggerEx.warn(TAG, "get ip error, eMsg: " + t.getMessage());
        }
        return null;
    }

    @Override
    public Boolean channelIsActive() {
        return channelHandlerContext != null && channelHandlerContext.channel() != null && channelHandlerContext.channel().isActive();
    }

    public static NettyChannelContext getNettyChannelContextByCtx(ChannelHandlerContext ctx) {
        if (ctx != null)
            return ctx.attr(NETTY_CHANNEL_CONTEXT_KEY).get();
        return null;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public AtomicBoolean getReadServer() {
        return readServer;
    }

    public void setReadServer(AtomicBoolean readServer) {
        this.readServer = readServer;
    }

    public AtomicBoolean getReadIdentity() {
        return readIdentity;
    }

    public void setReadIdentity(AtomicBoolean readIdentity) {
        this.readIdentity = readIdentity;
    }

    @Override
    public String toString() {
        return "NettyChannelContext{" +
                "channelHandlerContext=" + channelHandlerContext +
                ", " + super.toString() +
                '}';
    }
}
