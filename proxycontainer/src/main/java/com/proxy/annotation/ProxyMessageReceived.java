package com.proxy.annotation;

import com.dobybros.chat.binary.data.Data;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented

public @interface ProxyMessageReceived {
	public Class<? extends Data> dataClass();
	public int type();
}
