package com.docker.annotations;

import java.lang.annotation.*;

/**
 * Created by wenqi on 2018/12/14
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransactionResultNotify {
    String id();
}
