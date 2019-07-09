package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface Transaction {
	 String id();
	 int order();
	 String type();
}


