package com.docker.annotations;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    //过期时间  毫秒
    public long expired() default 36000;
    //前缀
    public String prefix();
    //key值
    public String key() default "";
    //cache的实现方式
    public String cacheMethod() default "";
}
