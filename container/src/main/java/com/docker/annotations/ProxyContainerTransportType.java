package com.docker.annotations;

/**
 * @author lick
 * @date 2019/11/15
 */
public @interface ProxyContainerTransportType {
    public static final int TYPE_RPC = 1;
    public static final int TYPE_QUEUE = 2;
    public int type();
    public String[] contentType();
}
