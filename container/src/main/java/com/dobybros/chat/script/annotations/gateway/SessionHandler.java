package com.dobybros.chat.script.annotations.gateway;

import java.lang.annotation.*;

// 使用后整个service只会有一个listener

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface SessionHandler {
}
