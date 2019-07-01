package connectors.mongodb.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface DocumentField {
	public String key() default "";
	public String mapKey() default "";
//	public String mapType() default "";
}
