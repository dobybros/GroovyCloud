package com.dobybros.chat.script.annotations.gateway;

import java.lang.annotation.*;

// 使用后整个service每个人都有各自的listener

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceUserSessionHandler {
}
