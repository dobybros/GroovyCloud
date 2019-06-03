package com.dobybros.chat.annotation;

import com.dobybros.chat.binary.data.Data;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented

public @interface MessageReceived {
	public Class<? extends Data> dataClass();
	public byte type();
}
