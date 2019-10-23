package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface CacheKey {
    public String field() default "";
}
