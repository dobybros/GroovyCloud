package com.proxy.im.mina;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.pack.Pack;
import com.proxy.im.SessionContext;
import org.apache.mina.core.session.IoSession;

/**
 * @author lick
 * @date 2019/11/13
 */
public class MinaSessionContext implements SessionContext {
    private IoSession session;
    public MinaSessionContext(IoSession session){
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
    public void close() {
        session.close();
    }

    @Override
    public void close(boolean immediately) {
        session.close(immediately);
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
        Pack hailPack = DataVersioning.getDataPack(session, data);
        session.write(hailPack);
    }

    @Override
    public void write(int code, String description, String forId) {
        Pack hailPack = DataVersioning.getResult(session, code, description, forId);
        session.write(hailPack);
    }
}
