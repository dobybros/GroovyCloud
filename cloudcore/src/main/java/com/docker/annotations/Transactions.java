package com.docker.annotations;

import java.lang.annotation.*;

/**
 * Created by wenqi on 2018/12/5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactions {
     Transaction[] values() default {};
}
