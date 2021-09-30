package com.dobybros.gateway.channels.websocket.netty.codec;

import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.channels.websocket.data.NettyChannelContext;
import com.dobybros.gateway.pack.Pack;
import com.dobybros.gateway.pack.PackVersioning;
import com.docker.server.OnlineServer;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Created by hzj on 2021/9/28 下午10:12
 */
public class WebSocketFrameCodec {

    public static final Integer MAX_PACK_LENGTH = 48 * 1024;
    public static final String TAG = WebSocketFrameCodec.class.getSimpleName();

    /**
     * decode server (6)
     */
    public static void decode(TextWebSocketFrame webSocketFrame, NettyChannelContext nettyChannelContext) {
        if (nettyChannelContext.getReadServer().compareAndSet(false, true)) {
            // 读取Server（6）
            String server = webSocketFrame.text();
            OnlineServer onlineServer = OnlineServer.getInstance();
            if(onlineServer == null || onlineServer.getServer() == null || !onlineServer.getServer().equals(server)) {
                LoggerEx.error(TAG, "Session closed, consume server " + new String(server) +
                        " failed, current " + onlineServer.getServer() + ", netty context " + nettyChannelContext);
                nettyChannelContext.close();
            }
        } else {
            LoggerEx.error(TAG, "will close context, consume message: " + webSocketFrame.text());
            nettyChannelContext.close();
        }
    }

    /**
     * header：1(version) + 2(encodeVersion) + 1(encode)
     * pack: 1(type) + 4(length) + content
     */
    public static Data decode(BinaryWebSocketFrame webSocketFrame, NettyChannelContext nettyChannelContext) {
        ByteBuf byteBuf = webSocketFrame.content();
        if (nettyChannelContext.getReadIdentity().compareAndSet(false, true)) {
            // （1 + 2 + 1）
            // 读取version
            nettyChannelContext.setPackVersion(byteBuf.readByte());
            // 读取encode version
            nettyChannelContext.setEncodeVersion(byteBuf.readShort());
            // 读取encode
            nettyChannelContext.setEncode(byteBuf.readByte());

        }
        // 读取消息包（1 + 4 + content）
        Pack pack = PackVersioning.get(nettyChannelContext.getPackVersion(), nettyChannelContext.getEncode(), nettyChannelContext.getEncodeVersion());
        pack.readHeadFromByteBuf(byteBuf);

        // 检查length，不能大于最大length
        int length = pack.getLength();
        if (length < 0 || length > MAX_PACK_LENGTH) {
            LoggerEx.warn(TAG, "Read inproper length of pack, ignore the whole buffer and wait for next arrive. length = " + length + "; maxLength = " + MAX_PACK_LENGTH);
            Pack resPack = DataVersioning.getResult(nettyChannelContext.getPackVersion(),
                    nettyChannelContext.getEncode(),
                    nettyChannelContext.getEncodeVersion(), IMCoreErrorCodes.ERROR_CHARACTER_OVER_MAXIMUM_LIMITS, null, null);
            nettyChannelContext.write(resPack);
            return null;
        }

        byte[] content = new byte[pack.getLength()];
        byteBuf.readBytes(content);
        pack.setContent(content);
        return DataVersioning.get(pack);
    }

}
