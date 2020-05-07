package script.groovy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lick on 2020/4/22.
 * Description：
 */
@Target(value = {ElementType.TYPE, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface JavaBean {
    public Class beanClass() default JavaBean.class;
    public Class fromBeanClass() default JavaBean.class;
    public String methodName() default "";
    public String[] params() default {};
}
