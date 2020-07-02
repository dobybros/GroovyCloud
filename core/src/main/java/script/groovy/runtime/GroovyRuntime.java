package script.groovy.runtime;


import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.ScriptRuntime;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.classloader.ClassHolder;
import script.groovy.runtime.classloader.MyGroovyClassLoader;
import script.utils.CmdUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroovyRuntime extends ScriptRuntime {
    private static final String TAG = GroovyRuntime.class.getSimpleName();
    private ArrayList<ClassAnnotationHandler> annotationHandlers = new ArrayList<>();
    private ArrayList<ClassAnnotationGlobalHandler> annotationGlobalHandlers = new ArrayList<>();
    private ConcurrentHashMap<Object, ClassAnnotationHandler> annotationHandlerMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Object, ClassAnnotationGlobalHandler> annotationGlobalHandlerMap = new ConcurrentHashMap<>();
    private GroovyBeanFactory beanFactory;
    private List<FieldInjectionListener> fieldInjectionListeners;
    private List<String> libPaths;
    private URLClassLoader libClassLoader;
    private RuntimeBootListener runtimeBootListener;
    private ClassLoader parentClassLoader;
    private Class<?> groovyObjectExProxyClass;
    private HashMap<String, ClassHolder> cachedClasses;
    private Map<String, Class<?>> allClasses;


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

    public boolean addClassAnnotationGlobalHandler(ClassAnnotationGlobalHandler handler) {
        if (handler != null && !annotationGlobalHandlers.contains(handler)) {
            boolean bool = annotationGlobalHandlers.add(handler);
            annotationGlobalHandlerMap.put(handler.getKey(), handler);
            return bool;
        }

        return false;
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

    public boolean removeClassAnnotationGlobalHandler(ClassAnnotationGlobalHandler handler) {
        if (handler != null) {
            boolean bool = annotationGlobalHandlers.remove(handler);
            annotationGlobalHandlerMap.remove(handler.getKey(), handler);
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
                LoggerEx.error(TAG, "oldLibClassLoader close failed, " + ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    @Override
    public synchronized void start() throws CoreException {
        URLClassLoader newLibClassLoader = null, oldLibClassLoader = libClassLoader;

        if (parentClassLoader == null)
            parentClassLoader = GroovyRuntime.class.getClassLoader();
        List<URL> urls = new ArrayList<>();
        File pomFile = new File(path + "pom.xml");
        if (pomFile.exists()) {
            try {
                String pomStr = FileUtils.readFileToString(pomFile, Charset.defaultCharset());
                if(pomStr.contains("AllThisDependencies")){
                    final String result = CmdUtils.execute("mvn install -DskipTests -f " + FilenameUtils.separatorsToUnix(pomFile.getAbsolutePath()));
                    LoggerEx.info(TAG, "Maven download dependencies success, path: " + pomFile.getAbsolutePath());
                    int allThisDependenciesIndexStart = pomStr.indexOf("<!--AllThisDependencies");
                    int allThisDependenciesIndexEnd = pomStr.indexOf("AllThisDependencies-->");
                    String dependencies = pomStr.substring(allThisDependenciesIndexStart + "<!--AllThisDependencies".length(), allThisDependenciesIndexEnd);
                    JSONArray allDependencies = JSON.parseArray(dependencies);
                    if(allDependencies != null && !allDependencies.isEmpty()){
                        File libsPath = new File(path + "/libs");
                        if(!libsPath.exists()){
                            libsPath.mkdirs();
                        }
                        for (Object o : allDependencies){
                            if(o instanceof JSONObject){
                                JSONObject dependency = (JSONObject)o;
                                if(StringUtils.isNotBlank((String) dependency.get("groupId")) && StringUtils.isNotBlank((String) dependency.get("artifactId")) && StringUtils.isNotBlank((String) dependency.get("version"))){
                                    String[] groupDir =  ((String) dependency.get("groupId")).split("\\.");
                                    String groupDirStr = "";
                                    for (int i = 0; i < groupDir.length; i++) {
                                        groupDirStr += groupDir[i] + File.separator;
                                    }
                                    if(groupDirStr != ""){
                                        String jarDir = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository" + File.separator + groupDirStr + dependency.get("artifactId") + File.separator + dependency.get("version");
                                        String jarPath = jarDir + File.separator + dependency.get("artifactId") + "-" +  dependency.get("version") + ".jar";
                                        CmdUtils.execute("cp " + jarPath + " " + libsPath.getAbsolutePath());
                                    }
                                }else {
                                    LoggerEx.error(TAG, "The dependency is not illegal, dependency: " + JSON.toJSONString(dependency) + ",path: " + pomFile.getAbsolutePath());
                                }

                            }
                        }
                    }
                }
            } catch (Throwable e) {
                LoggerEx.error(TAG, "Maven download dependencies err, path: " + pomFile.getAbsolutePath() + ",errMsg: " + e.getMessage());
                throw new CoreException(ChatErrorCodes.ERROR_MAVEN_INSTALL_ERROR, "Maven download dependencies err, path: " + pomFile.getAbsolutePath() + ",errMsg: " + e.getMessage());
            }
        }
        File libsPath = new File(path + "/libs");
        if (libsPath.exists() && libsPath.isDirectory()) {
            Collection<File> jars = FileUtils.listFiles(libsPath,
                    FileFilterUtils.suffixFileFilter(".jar"),
                    FileFilterUtils.directoryFileFilter());
            String loadJarsPath = "";
            for (File jar : jars) {
                String path = "jar:file://" + jar.getAbsolutePath() + "!/";
                try {
                    urls.add(jar.toURI().toURL());
                    loadJarsPath += jar.getAbsolutePath() + ";";
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    LoggerEx.warn(TAG, "MalformedURL " + path + " while load jars, error " + e.getMessage());
                }
            }
            LoggerEx.info(TAG, "Loaded jars " + loadJarsPath);
        }

        if (!urls.isEmpty()) {
            URL[] theUrls = new URL[urls.size()];
            urls.toArray(theUrls);
            parentClassLoader = new URLClassLoader(theUrls, parentClassLoader);
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

        final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new ConcurrentHashMap<>();
        final Map<ClassAnnotationGlobalHandler, Map<String, Class<?>>> handlerGlobalMap = new ConcurrentHashMap<>();
        Class[] loadedClasses = runtimeBootListener.getLoadedClasses();
        if (loadedClasses != null) {
            cachedClasses = new HashMap<>();
            allClasses = new HashMap<>();
            for (Class clazz : loadedClasses) {
                ClassHolder classHolder = new ClassHolder();
                classHolder.setParsedClass(clazz);
                LoggerEx.info(TAG, "Loaded class " + clazz.getName());
                cachedClasses.put(clazz.getName(), classHolder);
                allClasses.put(clazz.getName(), clazz);
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
                if (annotationGlobalHandlers != null) {
                    Collection<ClassAnnotationGlobalHandler> handlers = annotationGlobalHandlers;
                    for (ClassAnnotationGlobalHandler handler : handlers) {
//						ClassAnnotationHandler handler = annotationHandlers.get(i);
//						handler.setGroovyRuntime(this);
                        Class<? extends Annotation> annotationClass = handler
                                .handleAnnotationClass(this);
                        if (annotationClass != null) {
                            Annotation annotation = clazz
                                    .getAnnotation(annotationClass);
                            if (annotation != null) {
                                Map<String, Class<?>> classes = handlerGlobalMap
                                        .get(handler);
                                if (classes == null) {
                                    classes = new HashMap<>();
                                    handlerGlobalMap.put(handler, classes);
                                }

                                //XXX the key original is groovy path, not absolute.
                                classes.put(clazz.getName(), clazz);
                            }
                        }
                    }
                }
            }
        }
        for (ClassAnnotationHandler annotationHandler : annotationHandlers) {
            if (annotationHandler.getGroovyRuntime() == null)
                annotationHandler.setGroovyRuntime(GroovyRuntime.this);
            if (annotationHandler instanceof GroovyBeanFactory) {
                beanFactory = (GroovyBeanFactory) annotationHandler;
                break;
            }
        }
        if (handlerGlobalMap != null && !handlerGlobalMap.isEmpty()) {
            Collection<ClassAnnotationGlobalHandler> handlers = annotationGlobalHandlers;
            for (ClassAnnotationGlobalHandler annotationHandler : handlers) {
                if (annotationHandler.isBean()) {
                    Map<String, Class<?>> values = handlerGlobalMap.get(annotationHandler);
                    if (values != null) {
                        for (Class<?> c : values.values()) {
                            if (ReflectionUtil.canBeInitiated(c))
                                beanFactory.getClassBean(c);
                        }
                    }
                }
            }

            for (ClassAnnotationGlobalHandler annotationHandler : handlers) {
                Map<String, Class<?>> values = handlerGlobalMap.get(annotationHandler);
                if (values != null) {
                    try {
                        annotationHandler.handleAnnotatedClasses(values, this);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.fatal(TAG,
                                "Handle annotated classes failed, "
                                        + values + " the handler " + annotationHandler
                                        + " is ignored!errMsg: " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        }
        if (handlerMap != null && !handlerMap.isEmpty()) {
            Collection<ClassAnnotationHandler> handlers = annotationHandlers;
            for (ClassAnnotationHandler annotationHandler : handlers) {
                if (annotationHandler.isBean()) {
                    Map<String, Class<?>> values = handlerMap.get(annotationHandler);
                    if (values != null) {
                        for (Class<?> c : values.values()) {
                            if (ReflectionUtil.canBeInitiated(c))
                                beanFactory.getClassBean(c);
                        }
                    }
                }
            }

            for (ClassAnnotationHandler annotationHandler : handlers) {
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
                                        + " is ignored!errMsg: " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
            for (ClassAnnotationGlobalHandler annotationHandler : annotationGlobalHandlers) {
                Map<String, Class<?>> values = handlerGlobalMap.get(annotationHandler);
                if (values != null) {
                    try {
                        annotationHandler.handleAnnotatedClassesInjectBean(this);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.fatal(TAG,
                                "Handle annotated classes failed, "
                                        + values + " the handler " + annotationHandler
                                        + " is ignored!errMsg: " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        }
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
                        + groovyObjectClass + " failed, " + ExceptionUtils.getFullStackTrace(e));
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

    private void unzip(File zipFile, String dir, String passwd) {
        ZipFile zFile = null;
        try {
            zFile = new ZipFile(zipFile);
            File destDir = new File(dir);
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }
            zFile.extractAll(dir);

            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            File[] extractedFiles = new File[extractedFileList.size()];
            extractedFileList.toArray(extractedFiles);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "password is error,destFile:" + dir);
        }
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
                        + groovyObjectClass + " failed, " + ExceptionUtils.getFullStackTrace(e));
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
                    + groovyObjectClass + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
        return obj;
    }

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
                                + " the handler " + annotationHandler + " error " + ExceptionUtils.getFullStackTrace(t));
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

    public Map<String, Class<?>> getAllClasses() {
        return allClasses;
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
