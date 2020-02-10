package com.docker.annotations;

import java.lang.annotation.*;

/**
 * @author lick
 * @date 2019/11/15
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ProxyContainerTransportType {
    public static final int TYPE_RPC = 1;
    public static final int TYPE_QUEUE = 2;
    public int type();
    public String[] contentType();
}
