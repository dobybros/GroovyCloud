package com.dobybros.gateway.channels.encoders;

import com.dobybros.gateway.channels.data.Chunk;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.pack.HailPack;
import com.dobybros.gateway.pack.Pack;
import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public abstract class ChunkEncoder extends ProtocolEncoderAdapter {
    public static final int CHUNK_SIZE = 24 * 1024;
    public void output(IoSession session, HailPack pack,
                       ProtocolEncoderOutput out) throws Exception {
        //TODO should support input stream for output, better for memory.
        byte[] data = pack.getContent();
        if(data != null && data.length > CHUNK_SIZE) {
            HailPack headPack = new HailPack();
            headPack.setEncodeVersion(pack.getEncodeVersion());
            headPack.setEncode(pack.getEncode());
            headPack.setType(pack.getType());
            headPack.setLength(-1);

            IoBuffer buf = IoBuffer.allocate(/*headPack.getLength() + */5);
//            buf.setAutoExpand(true);//could be optimized. TODO
            headPack.persistent(buf);
            buf.flip();

            output(session, out, buf);
            out.flush();

            int count = 0;
            for(int i = 0; i < data.length; i += CHUNK_SIZE) {
                byte[] chunkData = ArrayUtils.subarray(data, i, i + CHUNK_SIZE);
                Chunk chunk = new Chunk();
//                chunk.setId(ChatUtils.generateFixedRandomString());
                chunk.setContent(chunkData);
                chunk.setOriginalType((int) pack.getType());
                chunk.setEncode(pack.getEncode());
                chunk.setChunkNum(count++);
                chunk.setOffset(i);
                chunk.setTotalSize(data.length);

                Pack chunkPack = DataVersioning.getDataPack(session, chunk);
                IoBuffer chunkBuf = IoBuffer.allocate(chunkPack.getLength() + 5);
                chunkPack.persistent(chunkBuf);
                chunkBuf.flip();

                output(session, out, chunkBuf);
                out.flush();
            }
            return;
        }
        IoBuffer buf = IoBuffer.allocate(pack.getLength() + 5);
//        buf.setAutoExpand(true);//could be optimized. TODO
        pack.persistent(buf);
        buf.flip();

        output(session, out, buf);
        out.flush();
    }

    public void output(IoSession session, ProtocolEncoderOutput out, IoBuffer buffer) {
        out.write(buffer);
    }

}
