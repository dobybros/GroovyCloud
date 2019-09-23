package script.groovy.runtime;



import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import script.ScriptRuntime;
import script.groovy.object.GroovyObjectEx;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import script.groovy.runtime.classloader.ClassHolder;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class GroovyRuntime extends ScriptRuntime {
    private static final String TAG = GroovyRuntime.class.getSimpleName();
    private ArrayList<ClassAnnotationHandler> annotationHandlers = new ArrayList<>();
    private ConcurrentHashMap<Object, ClassAnnotationHandler> annotationHandlerMap = new ConcurrentHashMap<>();
    private GroovyBeanFactory beanFactory;
    private List<FieldInjectionListener> fieldInjectionListeners;
    private List<String> libPaths;
    private URLClassLoader libClassLoader;
    private RuntimeBootListener runtimeBootListener;
    private ClassLoader parentClassLoader;
    private Class<?> groovyObjectExProxyClass;
    private HashMap<String, ClassHolder> cachedClasses;


    public void addLibPath(String libPath) {
        if (StringUtils.isBlank(libPath))
            return;
        if (libPaths == null) {
            libPaths = new ArrayList<>();
        }
        if (!libPaths.contains(libPath)) {
            libPaths.add(libPath);
        }
        return;
    }

    public boolean removeLibPath(String libPath) {
        if (StringUtils.isBlank(libPath) || libPaths == null)
            return false;
        return libPaths.remove(libPath);
    }

    public List<String> getLibPath() {
        return libPaths;
    }

    public RuntimeBootListener getRuntimeBootListener() {
        return runtimeBootListener;
    }

    public void setRuntimeBootListener(RuntimeBootListener runtimeBootListener) {
        this.runtimeBootListener = runtimeBootListener;
    }

    public GroovyRuntime addFieldInjectionListener(FieldInjectionListener listener) {
        if (fieldInjectionListeners == null) {
            fieldInjectionListeners = new ArrayList<>();
        }
        if (!fieldInjectionListeners.contains(listener))
            fieldInjectionListeners.add(listener);
        return this;
    }

    public void removeFieldInjectionListener(FieldInjectionListener listener) {
        if (fieldInjectionListeners != null)
            fieldInjectionListeners.remove(listener);
    }

    public List<FieldInjectionListener> getFieldInjectionListeners() {
        return fieldInjectionListeners;
    }

    public static GroovyRuntime getCurrentGroovyRuntime(ClassLoader currentClassLoader) {
        if (currentClassLoader == null)
            return null;
        ClassLoader classLoader = currentClassLoader.getParent();
        if (classLoader != null && classLoader instanceof MyGroovyClassLoader) {
            return ((MyGroovyClassLoader) classLoader).getGroovyRuntime();
        }
        return null;
    }

    public GroovyRuntime() {
//		instance = this;
    }

    public synchronized void init() throws CoreException {
//		instance = this;
        start();
    }

    public boolean addClassAnnotationHandler(ClassAnnotationHandler handler) {
        if (handler != null && !annotationHandlers.contains(handler)) {
            boolean bool = annotationHandlers.add(handler);
            annotationHandlerMap.put(handler.getKey(), handler);
            handler.setGroovyRuntime(this);
            return bool;
        }

        return false;
    }

    public boolean removeClassAnnotationHandler(ClassAnnotationHandler handler) {
        if (handler != null) {
            boolean bool = annotationHandlers.remove(handler);
            annotationHandlerMap.remove(handler.getKey(), handler);
            return bool;
        }
        return false;
    }

    private void closeLibClassloader(URLClassLoader oldLibClassLoader) {
        if (libClassLoader != null) {
            try {
                oldLibClassLoader.close();
                LoggerEx.info(TAG, "oldLibClassLoader " + oldLibClassLoader + " has been closed.");
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "oldLibClassLoader close failed, " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void start() throws CoreException {
        URLClassLoader newLibClassLoader = null, oldLibClassLoader = libClassLoader;

        if (parentClassLoader == null)
            parentClassLoader = GroovyRuntime.class.getClassLoader();
        File libsPath = new File(path + "/libs");
        if (libsPath.exists() && libsPath.isDirectory()) {
            List<URL> urls = new ArrayList<>();
            Collection<File> jars = FileUtils.listFiles(libsPath,
                    FileFilterUtils.suffixFileFilter(".jar"),
                    FileFilterUtils.directoryFileFilter());
            for (File jar : jars) {
                String path = "jar:file://" + jar.getAbsolutePath() + "!/";
                try {
                    urls.add(jar.toURI().toURL());
                    LoggerEx.info(TAG, "Loaded jar " + jar.getAbsolutePath());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    LoggerEx.warn(TAG, "MalformedURL " + path + " while load jars, error " + e.getMessage());
                }
            }
            if (!urls.isEmpty()) {
                URL[] theUrls = new URL[urls.size()];
                urls.toArray(theUrls);
                parentClassLoader = new URLClassLoader(theUrls, parentClassLoader);
            }
        }

        if (runtimeBootListener != null) {
            runtimeBootListener.start(parentClassLoader);
        }

        String[] strs = new String[]{
                "package script.groovy.runtime;",
                "import script.groovy.object.GroovyObjectEx",
                "class GroovyObjectExProxy implements GroovyInterceptable{",
                "private GroovyObjectEx<?> groovyObject;",
                "public GroovyObjectExProxy(GroovyObjectEx<?> groovyObject) {",
                "this.groovyObject = groovyObject;",
                "}",
                "def invokeMethod(String name, args) {",
                "Class<?> groovyClass = this.groovyObject.getGroovyClass();",
                "def calledMethod = groovyClass.metaClass.getMetaMethod(name, args);",
                "def returnObj = calledMethod?.invoke(this.groovyObject.getObject(), args);",
                "return returnObj;",
                "}",
                "}"
        };
        String proxyClassStr = StringUtils.join(strs, "\r\n");
        groovyObjectExProxyClass = runtimeBootListener.getClassLoader().parseClass(proxyClassStr,
                "/script/groovy/runtime/GroovyObjectExProxy.groovy");

        final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new LinkedHashMap<ClassAnnotationHandler, Map<String, Class<?>>>();
        Class[] loadedClasses = runtimeBootListener.getLoadedClasses();
        if (loadedClasses != null) {
            cachedClasses = new HashMap<>();
            for (Class clazz : loadedClasses) {
                ClassHolder classHolder = new ClassHolder();
                classHolder.setParsedClass(clazz);
                LoggerEx.info(TAG, "Loaded class " + clazz.getName());
                cachedClasses.put(clazz.getName(), classHolder);
                if (annotationHandlers != null) {
                    Collection<ClassAnnotationHandler> handlers = annotationHandlers;
                    for (ClassAnnotationHandler handler : handlers) {
//						ClassAnnotationHandler handler = annotationHandlers.get(i);
//						handler.setGroovyRuntime(this);
                        Class<? extends Annotation> annotationClass = handler
                                .handleAnnotationClass(this);
                        if (annotationClass != null) {
                            Annotation annotation = clazz
                                    .getAnnotation(annotationClass);
                            if (annotation != null) {
                                Map<String, Class<?>> classes = handlerMap
                                        .get(handler);
                                if (classes == null) {
                                    classes = new HashMap<>();
                                    handlerMap.put(handler, classes);
                                }

                                //XXX the key original is groovy path, not absolute.
                                classes.put(clazz.getName(), clazz);
                            }
                        }
                    }
                }
            }
        }

        if (handlerMap != null && !handlerMap.isEmpty()) {
            Thread handlerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Collection<ClassAnnotationHandler> handlers = annotationHandlers;
                    for (ClassAnnotationHandler annotationHandler : handlers) {
                        if (annotationHandler.getGroovyRuntime() == null)
                            annotationHandler.setGroovyRuntime(GroovyRuntime.this);
                        if (annotationHandler instanceof GroovyBeanFactory) {
                            beanFactory = (GroovyBeanFactory) annotationHandler;
                        }
                        Map<String, Class<?>> values = handlerMap.get(annotationHandler);
                        if (values != null) {
                            try {
                                annotationHandler.handleAnnotatedClasses(values,
                                        runtimeBootListener.getClassLoader());
                            } catch (Throwable t) {
                                t.printStackTrace();
                                LoggerEx.fatal(TAG,
                                        "Handle annotated classes failed, "
                                                + values + " the handler " + annotationHandler
                                                + " is ignored!");
                            }
                        }
                    }
                }
            });
            handlerThread.start();
            try {
                handlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LoggerEx.info(TAG, "Reload groovy scripts");
    }

    public String processAnnotationString(String str) {
        return str;
    }

    public static String path(Class<?> c) {
        return c.getName().replace(".", "/") + ".groovy";
    }

    public <T> GroovyObjectEx<T> create(Class<?> c) {
        return create(path(c), null);
    }

    public <T> GroovyObjectEx<T> create(String groovyPath) {
        return create(groovyPath, null);
    }

    public <T> GroovyObjectEx<T> create(String groovyPath,
                                        Class<? extends GroovyObjectEx<T>> groovyObjectClass) {
        GroovyObjectEx<T> goe = null;
        if (groovyObjectClass != null) {
            try {
                Constructor<? extends GroovyObjectEx<T>> constructor = groovyObjectClass
                        .getConstructor(String.class);
                goe = constructor.newInstance(groovyPath);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Initialize customized groovyObjectClass "
                        + groovyObjectClass + " failed, " + e.getMessage());
                return null;
            }
        } else {
            goe = new GroovyObjectEx<T>(groovyPath);
        }
        goe.setGroovyRuntime(this);
        return goe;
    }

    public <T> Object newObject(Class<?> c) {
        return newObject(path(c), null);
    }

    public <T> Object newObject(String groovyPath) {
        return newObject(groovyPath, null);
    }

    public <T> Object newObject(String groovyPath,
                                Class<? extends GroovyObjectEx<T>> groovyObjectClass) {
        GroovyObjectEx<T> goe = null;
        if (groovyObjectClass != null) {
            try {
                Constructor<? extends GroovyObjectEx<T>> constructor = groovyObjectClass
                        .getConstructor(String.class);
                goe = constructor.newInstance(groovyPath);
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Initialize customized groovyObjectClass "
                        + groovyObjectClass + " failed, " + e.getMessage());
                return null;
            }
        } else {
            goe = new GroovyObjectEx<T>(groovyPath);
        }
        goe.setGroovyRuntime(this);

        Object obj = null;
        try {
            Constructor<?> constructor = groovyObjectExProxyClass.getConstructor(GroovyObjectEx.class);
            obj = constructor.newInstance(goe);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "New proxy instance "
                    + groovyObjectClass + " failed, " + e.getMessage());
        }
        return obj;
    }

//    public Object getProxyObject(GroovyObjectEx<?> groovyObject) {
//        Object obj = null;
//        try {
//            GroovyBeanFactory factory = beanFactory;
//            Class<?> proxyClass = factory.getProxyClass(groovyObject.getGroovyClass().getName());
//            if(proxyClass != null) {
//                Constructor<?> constructor = proxyClass.getConstructor(GroovyObjectEx.class);
//                obj = constructor.newInstance(groovyObject);
//            }
//        } catch (Throwable  e) {
//            e.printStackTrace();
//            LoggerEx.error(TAG, "New proxy instance(getProxyObject) "
//                    + groovyObject.getGroovyPath() + " failed, " + e.getMessage());
//        }
//        return obj;
//    }

    @Override
    public void close() {
        Collection<ClassAnnotationHandler> handlers = annotationHandlers;
        for (ClassAnnotationHandler annotationHandler : handlers) {
            try {
                annotationHandler.handlerShutdown();
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.fatal(TAG,
                        "Handle annotated classes shutdown failed "
                                + " the handler " + annotationHandler + " error " + t.getMessage());
            }
        }
        runtimeBootListener.close();
        if (libClassLoader != null) {
            closeLibClassloader(libClassLoader);
        }
        annotationHandlerMap.clear();
        annotationHandlers.clear();
    }

    public MyGroovyClassLoader getClassLoader() {
        if (runtimeBootListener == null)
            return null;
        return runtimeBootListener.getClassLoader();
    }

    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Collection<ClassAnnotationHandler> getAnnotationHandlers() {
        return annotationHandlers;
    }

    public void setAnnotationHandlers(
            List<ClassAnnotationHandler> annotationHandlers) {
        if (annotationHandlers != null) {
            for (ClassAnnotationHandler handler : annotationHandlers) {
                handler.setGroovyRuntime(this);
                this.annotationHandlers.add(handler);
                this.annotationHandlerMap.put(handler.getKey(), handler);
            }
        }
    }


    public ClassAnnotationHandler getClassAnnotationHandler(Object key) {
        return this.annotationHandlerMap.get(key);
    }

    public GroovyBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(GroovyBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public HashMap<String, ClassHolder> getCachedClasses() {
        return cachedClasses;
    }

    public void setCachedClasses(HashMap<String, ClassHolder> cachedClasses) {
        this.cachedClasses = cachedClasses;
    }

    public Class<?> getClass(String classStr) {
        if (StringUtils.isBlank(classStr))
            return null;

        ClassHolder holder = getClassLoader().getClass(classStr);
        if (holder != null) {
            return holder.getParsedClass();
        }
        return null;
    }
}
