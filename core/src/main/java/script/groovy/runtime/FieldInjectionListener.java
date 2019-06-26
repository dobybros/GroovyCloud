package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class FieldInjectionListener<T extends Annotation> {
    public Class<T> annotationClass() {
        return null;
    }

    public abstract void inject(T annotation, Field field, Object obj);
}
