package connectors.mongodb.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface DBDocument {
	@Deprecated
	public String collectionClass() default "";
	public Class<?> collection() default Object.class;
//	public String[] collectionClasses() default {};
	public String[] filters() default "";
}
