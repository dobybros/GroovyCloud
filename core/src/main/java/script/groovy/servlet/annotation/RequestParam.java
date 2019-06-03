package script.groovy.servlet.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import script.groovy.servlets.GroovyServletManager;

@Target(value = {ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	public String key() default "";
	public boolean required() default false;
}
