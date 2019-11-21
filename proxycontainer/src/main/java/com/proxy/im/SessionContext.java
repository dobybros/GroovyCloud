package com.proxy.im;

import com.dobybros.chat.binary.data.Data;

/**
 * @author lick
 * @date 2019/11/13
 */
public interface SessionContext {
    public void setAttribute(Object key, Object value);

    public Object getAttribute(Object key);

    public void removeAttribute(Object key);

    public void write(Data data);

    public void write(byte[] data, byte type);

    public void write(int code, String description, String forId);

    public void close();

    public void close(boolean immediately);

    public boolean isClosing();
}
