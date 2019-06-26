package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface Summary {
	public String id();
	public int maxTry();
	public int maxConfirm();
	public int maxCancel();
}
