package com.docker.script.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceMemory {
}
