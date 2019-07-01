package com.dobybros.gateway.channels.tcp.codec;

import chat.logs.LoggerEx;
import com.dobybros.gateway.channels.encoders.ChunkEncoder;
import com.dobybros.gateway.pack.HailPack;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.charset.Charset;

public class HailProtocalEncoder extends ChunkEncoder {
    private static final String TAG = "ENCODER";
    private final Charset charset;

    public HailProtocalEncoder(Charset charset) {
        this.charset = charset;
    }

    // 在此处实现对MyProtocalPack包的编码工作，并把它写入输出流中
    public void encode(IoSession session, Object message,
                       ProtocolEncoderOutput out) throws Exception {
//		LoggerEx.info(TAG, "encode message " + message);
        if(message instanceof HailPack) {
            output(session, (HailPack)message, out);
        } else {
            LoggerEx.warn(TAG, "Unexpected type of message " + message.getClass() + ", will be ignored");
        }
        /*
        Pack value = (Pack) message;
        IoBuffer buf = IoBuffer.allocate(value.getLength());
        buf.setAutoExpand(true);//could be optimized. TODO
        value.persistent(buf);
        buf.flip();
        out.write(buf);
        */
//		LoggerEx.info(TAG, "message " + message + " sent");
    }

    public void dispose() throws Exception {
    }
}
