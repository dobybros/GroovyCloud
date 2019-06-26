package com.dobybros.chat.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface PaymentMonitor {
	public String key() default "";
}
