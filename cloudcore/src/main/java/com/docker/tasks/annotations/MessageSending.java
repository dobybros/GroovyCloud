package com.docker.tasks.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MessageSending {
	public String messageType();
}
