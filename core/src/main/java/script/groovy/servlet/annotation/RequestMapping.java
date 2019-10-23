package script.groovy.servlet.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import script.groovy.servlets.GroovyServletManager;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	public String method() default "GET";
	public String uri();
	public String responseType() default GroovyServletManager.RESPONSETYPE_JSON;
	public String[] perms() default "";
	public boolean asyncSupported() default false;
}
