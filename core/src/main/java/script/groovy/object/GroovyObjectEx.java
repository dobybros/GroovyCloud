package script.groovy.object;

import chat.errors.GroovyErrorCodes;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import groovy.lang.GroovyObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.codehaus.groovy.runtime.NullObject;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.Nullable;
import script.groovy.annotation.Bean;
import script.groovy.runtime.FieldInjectionListener;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;
import chat.errors.CoreException;
import script.groovy.runtime.MethodInterceptor;
import script.groovy.runtime.classloader.ClassHolder;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class GroovyObjectEx<T> {
    private static final String TAG = GroovyObjectEx.class.getSimpleName();
    private String groovyPath;
    private Object lock = new Object();
    private GroovyRuntime groovyRuntime;
    private GroovyObjectListener objectListener;
    private Map<String, List<MethodInterceptor>> methodInterceptorMap;


    public GroovyObjectEx(String groovyPath) {
        this.groovyPath = groovyPath;
    }

    public Object invokeRootMethod(String method, Object... parameters) throws CoreException {
        String methodKey = method;//ReflectionUtil.getMethodKey(getGroovyClass(),method);
        if (methodInterceptorMap != null && methodKey != null) {
            List<MethodInterceptor> methodInterceptors = methodInterceptorMap.get(methodKey);
            if(methodInterceptors != null && !methodInterceptors.isEmpty()){
                MethodInvocation invocation = new MethodInvocation(getObject(), method, parameters, methodInterceptors);
                return invocation.proceed();
            }else{
                return invokeMethod(method, parameters);
            }
        } else {
            return invokeMethod(method, parameters);
        }
    }

    public Object invokeMethod(String method, Object... parameters) throws CoreException {
        T obj = getObject();
        if (obj != null && obj instanceof GroovyObject) {
            GroovyObject gObj = (GroovyObject) obj;
            //TODO Bind GroovyClassLoader base on current thread.
            return gObj.invokeMethod(method, parameters);
        }
        return null;
    }

    private String getCrc(Class clazz, String methodName, int paramCount) {
        Long crc = ReflectionUtil.getCrc(clazz, methodName);
        return crc + "" + paramCount;
    }


    public Class<T> getGroovyClass() throws CoreException {
        MyGroovyClassLoader classLoader = groovyRuntime.getClassLoader();
        if (classLoader == null)
            throw new CoreException(GroovyErrorCodes.ERROR_GROOVY_CLASSLOADERNOTFOUND, "Classloader is null");
        ClassHolder holder = classLoader.getClass(groovyPath);
        if (holder == null)
            throw new CoreException(GroovyErrorCodes.ERROR_GROOVY_CLASSNOTFOUND, "Groovy " + groovyPath + " doesn't be found in classLoader " + classLoader);
        return (Class<T>) holder.getParsedClass();
    }

    public T getObject() throws CoreException {
        return getObject(true);
    }

    public T getObject(boolean forceFill) throws CoreException {
        MyGroovyClassLoader classLoader = groovyRuntime.getClassLoader();
        if (classLoader == null)
            throw new CoreException(GroovyErrorCodes.ERROR_GROOVY_CLASSLOADERNOTFOUND, "Classloader is null");
        ClassHolder holder = classLoader.getClass(groovyPath);
        if (holder == null)
            throw new CoreException(GroovyErrorCodes.ERROR_GROOVY_CLASSNOTFOUND, "Groovy " + groovyPath + " doesn't be found in classLoader " + classLoader);

        GroovyObject gObj = holder.getCachedObject();
        if (gObj == null) {
            Class<?> groovyClass = holder.getParsedClass();
            synchronized (lock) {
                if (groovyClass != null) {
                    try {
                        gObj = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
                        if (forceFill)
                            GroovyObjectEx.fillGroovyObject(gObj, groovyRuntime);
                        holder.setCachedObject(gObj);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new CoreException(GroovyErrorCodes.ERROR_GROOY_NEWINSTANCE_FAILED, "New instance for class " + groovyClass + " failed " + e.getMessage() + " in classLoader " + classLoader);
                    }
                    if (objectListener != null)
                        objectListener.objectPrepared(gObj);
                }
            }
        }
        return (T) gObj;
    }

    public static void fillGroovyObjects(Collection<GroovyObjectEx> objs, GroovyRuntime groovyRuntime) {
        for (GroovyObjectEx groovyObjectEx : objs) {
            try {
                GroovyObjectEx.fillGroovyObject((GroovyObject) groovyObjectEx.getObject(), groovyRuntime);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "fillGroovyObject " + groovyObjectEx.getGroovyPath() + " failed, " + e.getMessage());
            }
        }
    }

    public static void fillGroovyObject(GroovyObject gObj, GroovyRuntime groovyRuntime) throws IllegalAccessException {
        GroovyBeanFactory beanFactory = groovyRuntime.getBeanFactory();
        if (beanFactory != null) {
            Field[] fields = ReflectionUtil.getFields(gObj.getClass());
            if (fields != null) {
                for (Field field : fields) {
                    //Bean handler
                    Bean bean = field.getAnnotation(Bean.class);
                    if (bean != null) {
                        String beanName = bean.name();
                        Class<?> gClass = null;
                        if (StringUtils.isBlank(beanName)) {
                            if (field.getType().isAssignableFrom(GroovyObjectEx.class)) {
                                Type fieldType = field.getGenericType();
                                if (fieldType instanceof ParameterizedType) {
                                    ParameterizedType pType = (ParameterizedType) fieldType;
                                    Type[] aTypes = pType.getActualTypeArguments();
                                    if (aTypes != null && aTypes.length == 1) {
                                        gClass = (Class<?>) aTypes[0];
                                    }
                                }
                            } else {
                                gClass = field.getType();
                            }
                        }
                        gClass = groovyRuntime.getClass(gClass.getName());
                        GroovyObjectEx<?> beanValue;
                        if (StringUtils.isBlank(beanName)) {
                            beanValue = beanFactory.getBean(gClass);
                        } else {
                            beanValue = beanFactory.getBean(beanName);
                        }
                        if (beanValue != null) {
                            if (field.getType().isAssignableFrom(GroovyObjectEx.class)) {
                                if (!field.isAccessible())
                                    field.setAccessible(true);
                                field.set(gObj, beanValue);
                            } else {
                                if (!field.isAccessible())
                                    field.setAccessible(true);
//								Object obj = groovyRuntime.getProxyObject(beanValue);
                                Object obj = null;
                                try {
                                    obj = beanValue.getObject();
                                    field.set(gObj, gClass.cast(obj));
                                } catch (CoreException e) {
                                    e.printStackTrace();
                                    LoggerEx.warn(TAG, "Assign value failed, " + field + " error " + e.getMessage());
                                }
                            }
                        }
                    }
                    List<FieldInjectionListener> injectListeners = groovyRuntime.getFieldInjectionListeners();
                    if (injectListeners != null) {
                        for (FieldInjectionListener listener : injectListeners) {
                            try {
                                Class<? extends Annotation> annotationClass = listener.annotationClass();
                                if (annotationClass != null) {
                                    Annotation annotation = field.getAnnotation(annotationClass);
                                    if (annotation != null) {
                                        listener.inject(annotation, field, gObj);
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                                LoggerEx.error(TAG, "handle field inject listener " + listener + " failed, " + t.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    public String getGroovyPath() {
        return groovyPath;
    }

    public GroovyRuntime getGroovyRuntime() {
        return groovyRuntime;
    }

    public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
        this.groovyRuntime = groovyRuntime;
    }

    public GroovyObjectListener getObjectListener() {
        return objectListener;
    }

    public void setObjectListener(GroovyObjectListener objectListener) {
        this.objectListener = objectListener;
    }

    public void addMethodInterceptors(String key, MethodInterceptor methodInterceptor) {
        if (key != null && methodInterceptor != null) {
            if (methodInterceptorMap == null) {
                methodInterceptorMap = new HashMap<>();
            }

            if (!methodInterceptorMap.containsKey(key)) {
                List methodInterceptors = new ArrayList<MethodInterceptor>();
                methodInterceptors.add(methodInterceptor);
                methodInterceptorMap.put(key, methodInterceptors);
            } else {
                List<MethodInterceptor> methodInterceptors = methodInterceptorMap.get(key);
                if (methodInterceptors != null) {
                    methodInterceptors.add(methodInterceptor);
                }
            }
            LoggerEx.info("SCAN", "Mapping interceptor key " + key + ", value + " + methodInterceptor.getClass().getSimpleName());
        }
    }

    public interface GroovyObjectListener {
        public void objectPrepared(Object obj) throws CoreException;
    }
}