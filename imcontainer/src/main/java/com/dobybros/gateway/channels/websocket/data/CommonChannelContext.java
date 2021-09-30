package com.dobybros.gateway.channels.websocket.data;

import com.dobybros.gateway.pack.Pack;

/**
 * Created by hzj on 2021/9/29 下午8:56
 */
public class CommonChannelContext extends ChannelContext {

    public CommonChannelContext(String userId, String service, Integer terminal) {
        this.setUserId(userId);
        this.setService(service);
        this.setTerminal(terminal);
    }

    @Override
    public void write(Pack pack) {

    }

    @Override
    public void write(byte[] data, byte type) {

    }

    @Override
    public void write(int code, String description, String forId) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getContextIp() {
        return null;
    }

    @Override
    public Boolean channelIsActive() {
        return null;
    }
}
