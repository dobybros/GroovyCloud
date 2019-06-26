package script.groovy.runtime.classloader;

import groovy.lang.GroovyObject;

public class ClassHolder {
    private Class<?> parsedClass;
    private GroovyObject cachedObject;

    public Class<?> getParsedClass() {
        return parsedClass;
    }

    public void setParsedClass(Class<?> parsedClass) {
        this.parsedClass = parsedClass;
    }

    public GroovyObject getCachedObject() {
        return cachedObject;
    }

    public void setCachedObject(GroovyObject cachedObject) {
        this.cachedObject = cachedObject;
    }
}