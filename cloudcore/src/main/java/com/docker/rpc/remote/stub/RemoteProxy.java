package com.docker.rpc.remote.stub;

import chat.utils.ReflectionUtil;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.Method;

public class RemoteProxy extends Proxy implements MethodInterceptor {
    private static final String TAG = RemoteProxy.class.getSimpleName();

    Enhancer enhancer = new Enhancer();

    public RemoteProxy(AutowireCapableBeanFactory beanFactory, ServiceStubManager serviceStubManager, RemoteServerHandler remoteServerHandler) {
        super(beanFactory, serviceStubManager, remoteServerHandler);
    }


    public Object getProxy(Class clazz) {
        //设置需要创建的子类
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        //通过字节码技术动态创建子类实例
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {
        // TODO Auto-generated method stub
        if(method.getDeclaringClass().equals(Object.class)) {
            return proxy.invokeSuper(obj, args);
        }
        Long crc = ReflectionUtil.getCrc(method, remoteServerHandler.getToService());
        return invoke(crc, args);
    }
}