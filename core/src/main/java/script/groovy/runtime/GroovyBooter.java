package script.groovy.runtime;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import groovy.lang.GroovyObject;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import script.groovy.runtime.classloader.ClassHolder;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public class GroovyBooter implements RuntimeBootListener {
    private static final String TAG = GroovyRuntime.class.getSimpleName();
    private MyGroovyClassLoader classLoader;
    private AtomicLong latestVersion = new AtomicLong(0);

    private GroovyRuntime groovyRuntime;

    public GroovyBooter() {
    }

    @Override
    public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
        this.groovyRuntime = groovyRuntime;
    }


    @Override
    public GroovyRuntime getGroovyRuntime() {
//        if (classLoader != null) {
//            return classLoader.getGroovyRuntime();
//        }
        return this.groovyRuntime;
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
        List<File> compileFirstFiles = new ArrayList<>();
        final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new LinkedHashMap<ClassAnnotationHandler, Map<String, Class<?>>>();
        try {
            File importPath = new File(path + "/config/imports.groovy");
            StringBuilder importBuilder = null;
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
            } else {
                String[] strs = new String[]{
                        "package config",
                        "\r\n",
                };
                String content = StringUtils.join(strs, "\r\n");
                FileUtils.writeStringToFile(importPath, content, "utf8");
                importBuilder = new StringBuilder(content);
                importBuilder.append("\r\n");
                LoggerEx.info(TAG, "Generates imports " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
            }
            compileFirstFiles.add(importPath);
//            StringBuilder importBuilder = new StringBuilder(FileUtils.readFileToString(importPath, "utf8"));

            newClassLoader = MyGroovyClassLoader.newClassLoader(parentClassLoader, groovyRuntime);
            Collection<File> files = FileUtils.listFiles(new File(path),
                    FileFilterUtils.suffixFileFilter(".groovy"),
                    FileFilterUtils.directoryFileFilter());

            if (importBuilder != null) {
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

                    importBuilder.append("import ").append(key).append("\r\n");
                }
                importBuilder.append("//THE END\r\n");

                FileUtils.writeStringToFile(importPath, importBuilder.toString(), "utf8");
            }

            for (File file : compileFirstFiles) {
                newClassLoader.parseClass(file);
            }

            deploySuccessfully = true;
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
                    TimerEx.schedule(new TimerTaskEx() {
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
