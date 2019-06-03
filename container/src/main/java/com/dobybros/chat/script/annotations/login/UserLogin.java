package com.dobybros.chat.script.annotations.login;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented

public @interface UserLogin {
}
