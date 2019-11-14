package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {
    //前缀
    public String prefix();
    //key值
    public String key() default "";
    //cache的实现方式
    public String cacheMethod() default "";
}
