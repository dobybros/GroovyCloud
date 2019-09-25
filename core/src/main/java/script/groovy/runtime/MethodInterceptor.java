package script.groovy.runtime;

import chat.errors.CoreException;
import script.groovy.object.MethodInvocation;

public interface MethodInterceptor {

    public Object invoke(MethodInvocation methodInvocation) throws CoreException;
}
