package com.proxy.im;

/**
 * @author lick
 * @date 2019/11/12
 */
public abstract class ProxySessionListener {
    public void sessionCreated(SessionContext sessionContext){}
    public void sessionOpened(SessionContext sessionContext){}
    public void sessionClosed(SessionContext sessionContext){}
    public void sessionIdle(SessionContext sessionContext){}
    public void exceptionCaught(SessionContext sessionContext, Throwable throwable){}
}