package com.proxy.im;

import com.dobybros.gateway.channels.websocket.data.ChannelContext;

/**
 * @author lick
 * @date 2019/11/12
 */
public abstract class ProxySessionListener {
    public void sessionCreated(ChannelContext context){}
    public void sessionClosed(ChannelContext context){}
    public void sessionIdle(ChannelContext context){}
    public void exceptionCaught(ChannelContext context, Throwable throwable){}
}