package script.groovy.runtime;

import chat.errors.CoreException;
import com.sun.org.apache.xpath.internal.operations.Bool;
import script.groovy.object.GroovyObjectEx;
import script.groovy.object.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface MethodInterceptor {

    public Object invoke(MethodInvocation methodInvocation) throws CoreException;
}
