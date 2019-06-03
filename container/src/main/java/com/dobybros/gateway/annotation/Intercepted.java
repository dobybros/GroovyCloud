/**
 * 
 */
package com.dobybros.gateway.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented

public @interface Intercepted  {
}
