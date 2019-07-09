package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigProperty {
	public String name();
}
