package script.groovy.runtime;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.ComponentScan;
import script.groovy.runtime.classloader.MyGroovyClassLoader;
import script.groovy.servlets.GroovyServletDispatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public class SpringBooter implements RuntimeBootListener {
    private static final String TAG = GroovyRuntime.class.getSimpleName();
    private MyGroovyClassLoader classLoader;
    private AtomicLong latestVersion = new AtomicLong(0);

    private GroovyRuntime groovyRuntime;

    public SpringBooter() {
    }

    @Override
    public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
        this.groovyRuntime = groovyRuntime;
    }


    @Override
    public GroovyRuntime getGroovyRuntime() {
        if (classLoader != null) {
            return classLoader.getGroovyRuntime();
        }
        return null;
    }

    public void beforeDeploy() {

    }

    @Override
    public synchronized void start(ClassLoader parentClassLoader) throws CoreException {
        if (groovyRuntime == null)
            throw new NullPointerException("GroovyRuntime is empty while redeploy for Booter " + this);
        String path = groovyRuntime.getPath();
        try {
            beforeDeploy();
        } catch (Throwable t) {
            LoggerEx.warn(TAG, "beforeDeploy failed, " + t.getMessage());
        }

        MyGroovyClassLoader newClassLoader = null;
        MyGroovyClassLoader oldClassLoader = classLoader;
        boolean deploySuccessfully = false;
        ByteArrayOutputStream baos = null;
        final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new LinkedHashMap<ClassAnnotationHandler, Map<String, Class<?>>>();
        try {
            File importPath = new File(path + "/config/imports.groovy");
            StringBuilder importBuilder = null;
            StringBuilder stringBuilder1 = new StringBuilder();
            if (importPath.isFile() && importPath.exists()) {
                LoggerEx.info(TAG, "Start imports " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                String content = FileUtils.readFileToString(importPath, "utf8");
                if (!content.endsWith("//THE END\r\n")) {
                    final CommandLine cmdLine = CommandLine.parse("groovy " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                    ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(15));//设置超时时间
                    DefaultExecutor executor = new DefaultExecutor();
                    baos = new ByteArrayOutputStream();
                    executor.setStreamHandler(new PumpStreamHandler(baos, baos));
                    executor.setWatchdog(watchdog);
                    executor.setExitValue(0);//由于ping被到时间终止，所以其默认退出值已经不是0，而是1，所以要设置它
                    int exitValue = executor.execute(cmdLine);
                    final String result = baos.toString().trim();
                    LoggerEx.info(TAG, "import log " + result);
                    LoggerEx.info(TAG, "Imported " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));

                    importBuilder = new StringBuilder(content);
                    importBuilder.append("\r\n");
                } else {
                    LoggerEx.info(TAG, "Already added imports for " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                }
            }
            newClassLoader = MyGroovyClassLoader.newClassLoader(parentClassLoader, groovyRuntime);
            Collection<File> files = FileUtils.listFiles(new File(path),
                    FileFilterUtils.suffixFileFilter(".groovy"),
                    FileFilterUtils.directoryFileFilter());

            for (File file : files) {
                String absolutePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                int pathPos = absolutePath.indexOf(path);
                if (pathPos < 0 || absolutePath.endsWith("config/imports.groovy")) {
                    LoggerEx.warn(TAG, "Find path " + path + " in file " + absolutePath + " failed, " + pathPos + ". Ignore...");
                    continue;
                }
                String key = absolutePath.substring(pathPos + path.length());

                List<String> libPaths = groovyRuntime.getLibPath();
                if (libPaths != null) {
                    boolean ignore = false;
                    for (String libPath : libPaths) {
                        if (key.startsWith(libPath)) {
                            ignore = true;
//                            LoggerEx.info(TAG, "Ignore lib classes " + key + " while parsing. hit lib " + libPath);
                            break;
                        }
                    }
                    if (ignore)
                        continue;
                }

                int pos = key.lastIndexOf(".");
                if (pos >= 0) {
                    key = key.substring(0, pos);
                }
                key = key.replace("/", ".");

                stringBuilder1.append("import ").append(key).append("\r\n");
            }
//            stringBuilder1.append("//THE END\r\n");


            //创建springboot扫描的包
            File file = new File(groovyRuntime.getPath());
            String filesStr = "";
            if (file != null) {
                File[] files1 = file.listFiles();
                for (int i = 0; i < files1.length; i++) {
                    if (files1[i].isDirectory()) {
                        filesStr += '"';
                        filesStr += files1[i].getName();
                        filesStr += '"';
                        if (i != (files1.length - 1)) {
                            filesStr += ",";
                        }
                    }
                }
            }
            if (!StringUtils.isEmpty(filesStr)) {
                //创建springboot启动类
                File appFile = null;
                String pathName = null;
                String[] pathSplits = groovyRuntime.getPath().split("/");
                if (pathSplits.length > 1) {
                    pathName = pathSplits[pathSplits.length - 2].split("_")[0];
                }
                if (pathName != null) {
                    pathName = pathName.substring(0, 1).toUpperCase() + pathName.substring(1, pathName.length());
                    String applicationProp = "package boot\n" +
                            "\n" +
                            "import org.springframework.web.bind.annotation.RequestMapping\n" +
                            "import org.springframework.web.bind.annotation.RequestMethod\n" +
                            "import org.springframework.web.bind.annotation.RestController\n" +
                            "\n" +
                            "@RestController\n" +
                            "public class Test {\n" +
                            "\n" +
                            "    @RequestMapping(value = \"testall\", method = RequestMethod.GET)\n" +
                            "    public String test(){\n" +
                            "        return \"test\";\n" +
                            "    }\n" +
                            "}";
                    File propFile = new File("../local/springboottest/tcadmin_v1/groovy/boot/BootTest.groovy");
                    try {
                        FileUtils.writeStringToFile(propFile, applicationProp, "utf8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String strsApplication =
                            "package boot \n" +
//                                    stringBuilder1.toString() +
                                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                                    "import org.springframework.context.annotation.ComponentScan;\n" +
                                    "import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;\n" +
                                    "import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;\n" +
                                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                                    "import script.groovy.servlets.GroovyServletDispatcher;\n" +
                                    "import org.springframework.context.annotation.Bean;\n" +
                                    "import java.io.InputStream;\n" +
                                    "import java.io.IOException;;\n" +
                                    "import org.springframework.boot.web.servlet.ServletRegistrationBean;\n" +
                                    "@SpringBootApplication(exclude = [MongoAutoConfiguration.class, MongoDataAutoConfiguration.class])\n" +
                                    "@ComponentScan([" + filesStr + "])\n" +
                                    "public class " + pathName + "Application {\n" +
                                    "@Bean\n" +
                                    "public ServletRegistrationBean servletRegistrationBean() {\n" +
                                    "ServletRegistrationBean registrationBean = new ServletRegistrationBean(new GroovyServletDispatcher());\n" +
                                    "registrationBean.setLoadOnStartup(1);\n" +
                                    "registrationBean.addUrlMappings(\"/rest/*\");\n" +
                                    "registrationBean.setName(\"groovyDispatcherServlet\");\n" +
                                    "return registrationBean;\n" +
                                    "}\n" +
                                    "public void main() {\n" +
                                    "new Thread(new Runnable() {\n" +
                                    "@Override\n" +
                                    "void run() {\n" +
                                    "Thread.currentThread().setContextClassLoader(" + pathName + "Application.class.getClassLoader());\n" +
                                    "System.setProperty(\"spring.config.name\", \"appTCadmin\");\n" +
                                    "org.springframework.boot.SpringApplication app = new org.springframework.boot.SpringApplication(" + pathName + "Application.class);\n" +
//                                    "app.setDefaultProperties(prop);\n" +
                                    "app.run([] as String[])\n" +
                                    "synchronized (this) {\n" +
                                    "this.wait()\n" +
                                    "}\n" +
                                    "}\n" +
                                    "}).start()\n" +
                                    "}\n" +
                                    "}";
                    appFile = new File(path + "boot/" + pathName + "Application.groovy");
                    try {
                        FileUtils.writeStringToFile(appFile, strsApplication, "utf8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                newClassLoader = MyGroovyClassLoader.newClassLoader(parentClassLoader, groovyRuntime);
                if (appFile != null) {
                    Class<?> clazz = newClassLoader.parseClass(appFile);
                    Object o = clazz.newInstance();
                    Method method = o.getClass().getMethod("main");
                    method.invoke(o);
                    deploySuccessfully = true;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            LoggerEx.fatal(TAG,
                    "Redeploy occur unknown error, " + t.getMessage()
                            + " redeploy aborted!!!");
            if (t instanceof CoreException)
                throw (CoreException) t;
            else
                throw new CoreException(ChatErrorCodes.ERROR_GROOVY_UNKNOWN,
                        "Groovy unknown error " + t.getMessage());
        } finally {
            IOUtils.closeQuietly(baos);
            if (deploySuccessfully) {
                if (oldClassLoader != null) {
                    TimerEx.schedule(new TimerTaskEx(SpringBooter.class.getSimpleName()) {
                        @Override
                        public void execute() {
                            LoggerEx.info(TAG, "Old class loader " + oldClassLoader + " is releasing");
                            try {
                                MetaClassRegistry metaReg = GroovySystem
                                        .getMetaClassRegistry();
                                Class<?>[] classes = oldClassLoader.getLoadedClasses();
                                for (Class<?> c : classes) {
                                    LoggerEx.info(TAG, classLoader
                                            + " remove meta class " + c);
                                    metaReg.removeMetaClass(c);
                                }

                                oldClassLoader.clearCache();
                                oldClassLoader.close();
                                LoggerEx.info(TAG, "oldClassLoader " + oldClassLoader
                                        + " is closed");
                            } catch (Throwable e) {
                                e.printStackTrace();
                                LoggerEx.error(TAG, oldClassLoader + " close failed, "
                                        + e.getMessage());
                            }
                        }
                    }, TimeUnit.SECONDS.toMillis(60)); //release old class loader after 60 seconds.
                    LoggerEx.info(TAG, "Old class loader " + oldClassLoader + " will be released after 60 seconds");
                }
                long version = latestVersion.incrementAndGet();
                newClassLoader.setVersion(version);
                classLoader = newClassLoader;

                LoggerEx.info(TAG, "Reload groovy scripts, current version is "
                        + version);
            } else {
                if (newClassLoader != null) {
                    try {
                        newClassLoader.clearCache();
                        newClassLoader.close();
                        LoggerEx.info(TAG, "newClassLoader " + newClassLoader
                                + " is closed");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Class<?>[] getLoadedClasses() {
        if (classLoader == null)
            return null;
        return classLoader.getLoadedClasses();
    }

    public static String path(Class<?> c) {
        return c.getName().replace(".", "/") + ".groovy";
    }

    @Override
    public void close() {
        if (classLoader != null) {
            try {
                MetaClassRegistry metaReg = GroovySystem
                        .getMetaClassRegistry();
                Class<?>[] classes = classLoader.getLoadedClasses();
                for (Class<?> c : classes) {
                    LoggerEx.info(TAG, classLoader
                            + " remove meta class " + c);
                    metaReg.removeMetaClass(c);
                }

                classLoader.clearCache();
                classLoader.close();
                LoggerEx.info(TAG, "oldClassLoader " + classLoader
                        + " is closed");
            } catch (Exception e) {
                e.printStackTrace();
                LoggerEx.error(TAG, classLoader + " close failed, "
                        + e.getMessage());
            }
        }
    }

    public AtomicLong getLatestVersion() {
        return latestVersion;
    }

    public MyGroovyClassLoader getClassLoader() {
        return classLoader;
    }

}
