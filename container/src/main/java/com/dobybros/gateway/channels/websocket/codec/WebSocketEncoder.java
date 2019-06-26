/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dobybros.gateway.channels.websocket.codec;

import chat.logs.LoggerEx;
import com.dobybros.gateway.channels.encoders.ChunkEncoder;
import com.dobybros.gateway.pack.HailPack;
import com.dobybros.gateway.pack.Pack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Encodes incoming buffers in a manner that makes the receiving client type transparent to the 
 * encoders further up in the filter chain. If the receiving client is a native client then
 * the buffer contents are simply passed through. If the receiving client is a websocket, it will encode
 * the buffer contents in to WebSocket DataFrame before passing it along the filter chain.
 * 
 * Note: you must wrap the IoBuffer you want to send around a WebSocketCodecPacket instance.
 * 
 * @author DHRUV CHOPRA
 */
public class WebSocketEncoder extends ChunkEncoder {
    private static final String TAG = WebSocketEncoder.class.getSimpleName();

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        boolean isHandshakeResponse = message instanceof WebSocketHandShakeResponse;
        boolean isDataFramePacket = message instanceof WebSocketCodecPacket;
//        boolean isDataFrameBytes = message instanceof byte[];
        boolean isPack = message instanceof Pack;
        boolean isRemoteWebSocket = session.containsAttribute(WebSocketUtils.SessionAttribute) && (true==(Boolean)session.getAttribute(WebSocketUtils.SessionAttribute));
        IoBuffer resultBuffer;
        if(isHandshakeResponse){
            WebSocketHandShakeResponse response = (WebSocketHandShakeResponse)message;
            resultBuffer = WebSocketEncoder.buildWSResponseBuffer(response);
            out.write(resultBuffer);
        } else if(isPack) {
            /*Pack value = (Pack) message;
            IoBuffer buf = IoBuffer.allocate(value.getLength());
            buf.setAutoExpand(true);//could be optimized. TODO
            value.persistent(buf);
            buf.flip();

//        	byte[] data = (byte[]) message;
//    		IoBuffer buf = IoBuffer.allocate(data.length);
//    		buf.put(data);
//    		buf.flip();
        	resultBuffer = isRemoteWebSocket ? WebSocketEncoder.buildWSDataFrameBuffer(buf) : buf;
            */

            if(message instanceof HailPack) {
                output(session, (HailPack)message, out);
            } else {
                LoggerEx.warn(TAG, "Unexpected type of message " + message.getClass() + ", will be ignored");
            }
        } else if(isDataFramePacket){
            WebSocketCodecPacket packet = (WebSocketCodecPacket)message;
            resultBuffer = isRemoteWebSocket ? WebSocketEncoder.buildWSDataFrameBuffer(packet.getPacket()) : packet.getPacket();
            out.write(resultBuffer);
        } else{
            throw (new Exception("message not a websocket type"));
        }
    }

    @Override
    public void output(IoSession session, ProtocolEncoderOutput out, IoBuffer buffer) {
        boolean isRemoteWebSocket = session.containsAttribute(WebSocketUtils.SessionAttribute) && (true==(Boolean)session.getAttribute(WebSocketUtils.SessionAttribute));
        out.write(isRemoteWebSocket ? WebSocketEncoder.buildWSDataFrameBuffer(buffer) : buffer);
    }

    // Web Socket handshake response go as a plain string.
    private static IoBuffer buildWSResponseBuffer(WebSocketHandShakeResponse response) {                
        IoBuffer buffer = IoBuffer.allocate(response.getResponse().getBytes().length, false);
        buffer.setAutoExpand(true);
        buffer.put(response.getResponse().getBytes());
        buffer.flip();
        return buffer;
    }
    
    // Encode the in buffer according to the Section 5.2. RFC 6455
    private static IoBuffer buildWSDataFrameBuffer(IoBuffer buf) {
        IoBuffer buffer = IoBuffer.allocate(buf.limit(), false);
        buffer.setAutoExpand(true);
        buffer.put((byte) 0x82);
        if(buffer.capacity() <= 125){
            byte capacity = (byte) (buf.limit());
            buffer.put(capacity);
        }
        else{
            buffer.put((byte)126);
            buffer.putShort((short)buf.limit());
        }        
        buffer.put(buf);
        buffer.flip();
        return buffer;
    }
    
}
