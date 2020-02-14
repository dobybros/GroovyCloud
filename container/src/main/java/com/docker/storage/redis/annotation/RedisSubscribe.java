package com.docker.storage.redis.annotation;

import java.lang.annotation.*;

/**
 * Created by lick on 2020/2/13.
 * Description：
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RedisSubscribe {
    public String[] keys();
}
