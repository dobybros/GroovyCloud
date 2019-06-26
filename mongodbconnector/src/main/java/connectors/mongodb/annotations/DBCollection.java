package connectors.mongodb.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface DBCollection {
	public String name();
	@Deprecated
	public String databaseClass() default "";
	public Class database() default Object.class;
}
