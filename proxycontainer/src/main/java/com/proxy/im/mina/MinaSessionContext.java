package com.proxy.im.mina;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalDecoder;
import com.dobybros.gateway.pack.HailPack;
import com.dobybros.gateway.pack.Pack;
import com.proxy.im.SessionContext;
import org.apache.mina.core.session.IoSession;

/**
 * @author lick
 * @date 2019/11/13
 */
public class MinaSessionContext implements SessionContext {
    private IoSession session;

    public MinaSessionContext(IoSession session) {
        this.session = session;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        session.setAttribute(key, value);
    }

    @Override
    public Object getAttribute(Object key) {
        return session.getAttribute(key);
    }

    @Override
    public Short getEncodeVersion() {
        return HailProtocalDecoder.getEncodeVersion(session);
    }

    @Override
    public void close() {
        if (!isClosing()) {
            session.close();
        }
    }

    @Override
    public void close(boolean immediately) {
        if (!isClosing()) {
            session.close(immediately);
        }
    }

    @Override
    public boolean isClosing() {
        return session.isClosing();
    }

    @Override
    public void removeAttribute(Object key) {
        session.removeAttribute(key);
    }

    @Override
    public void write(Data data) {
        if (!isClosing()) {
            Pack hailPack = DataVersioning.getDataPack(session, data);
            session.write(hailPack);
        }
    }

    @Override
    public void write(byte[] data, byte type) {
        if (!isClosing()) {
            Pack pack = new HailPack();
            pack.setData(data, type);
            session.write(pack);
        }
    }

    @Override
    public void write(int code, String description, String forId) {
        if (!isClosing()) {
            Pack hailPack = DataVersioning.getResult(session, code, description, forId);
            session.write(hailPack);
        }
    }
}
