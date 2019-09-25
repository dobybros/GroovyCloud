package script.groovy.object;


import chat.errors.CoreException;
import groovy.lang.GroovyObject;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.lang.Nullable;
import script.groovy.runtime.MethodInterceptor;

import java.util.List;

public class MethodInvocation {
    public final String methodKey;
    public final Object target;
    public final Class<?> clazz;
    public final String method;
    public final Object[] arguments;
    public final List<MethodInterceptor> methodInterceptors;
    protected int currentInterceptorIndex = -1;

    public MethodInvocation(@Nullable Object target, @Nullable Class<?> clazz, String method, @Nullable Object[] arguments, List<MethodInterceptor> methodInterceptors,String methodKey) {
        this.methodKey = methodKey;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.methodInterceptors = methodInterceptors;
        this.clazz = clazz;

    }

    @Nullable
    public Object proceed() throws CoreException {
        if (this.currentInterceptorIndex == this.methodInterceptors.size() - 1) {
            return this.invokeMethod();
        } else {
            MethodInterceptor interceptor = this.methodInterceptors.get(++this.currentInterceptorIndex);
            if (interceptor != null) {
                return interceptor.invoke(this);
            }
        }
        return null;
    }

    public Object invokeMethod() throws CoreException {
        if (this.target != null && this.target instanceof GroovyObject) {
            GroovyObject gObj = (GroovyObject) this.target;
            //TODO Bind GroovyClassLoader base on current thread.
            return gObj.invokeMethod(this.method, this.arguments);
        }
        return null;
    }

}
