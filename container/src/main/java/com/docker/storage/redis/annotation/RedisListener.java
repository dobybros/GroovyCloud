package com.docker.storage.redis.annotation;

import java.lang.annotation.*;

/**
 * Created by lick on 2020/3/5.
 * Description：
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RedisListener {

}
