package script.groovy.runtime;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import javassist.URLClassPath;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public class GroovyBooter implements RuntimeBootListener {
    private static final String TAG = GroovyBooter.class.getSimpleName();
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
            LoggerEx.warn(TAG, "beforeDeploy failed, " + ExceptionUtils.getFullStackTrace(t));
        }
        MyGroovyClassLoader newClassLoader = null;
        MyGroovyClassLoader oldClassLoader = classLoader;
        boolean deploySuccessfully = false;
        ByteArrayOutputStream baos = null;
        List<File> compileFirstFiles = new CopyOnWriteArrayList<>();
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

                    importBuilder = new StringBuilder(content);
                    importBuilder.append("\r\n");
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
//                LoggerEx.info(TAG, "Generates imports " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
            }
            File loggerPath = new File(path + "/chat/logs/LoggerEx.groovy");
            compileFirstFiles.add(loggerPath);
            compileFirstFiles.add(importPath);
//            StringBuilder importBuilder = new StringBuilder(FileUtils.readFileToString(importPath, "utf8"));

            newClassLoader = MyGroovyClassLoader.newClassLoader(parentClassLoader, groovyRuntime);
            Collection<File> files = FileUtils.listFiles(new File(path),
                    FileFilterUtils.suffixFileFilter(".groovy"),
                    FileFilterUtils.directoryFileFilter());

            if (importBuilder != null) {
                //读取文件信息
                String[] libGroovyFiles = null;
                File coreFile = new File(path + "coregroovyfiles");
                if (coreFile.exists()) {
                    try {
                        String libGroovyFilesStr = FileUtils.readFileToString(coreFile, "utf-8");
                        if (libGroovyFilesStr != null) {
                            libGroovyFiles = libGroovyFilesStr.split("\r\n");
                        }
                    } catch (Throwable throwable) {
                        LoggerEx.warn(TAG, "Read core groovy path failed, reason is " + ExceptionUtils.getFullStackTrace(throwable));
                    }
                }
                for (File file : files) {
                    String absolutePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                    int pathPos = absolutePath.indexOf(path);
                    if (pathPos < 0 || absolutePath.endsWith("config/imports.groovy")) {
                        LoggerEx.info(TAG, "Find path " + path + " in file " + absolutePath + " failed, " + pathPos + ". Ignore...");
                        continue;
                    }
                    String key = absolutePath.substring(pathPos + path.length());
                    boolean ignore = false;
                    if (libGroovyFiles != null) {
                        List libGroovyFilesList = Arrays.asList(libGroovyFiles);
                        if (libGroovyFilesList.contains(key)) {
                            ignore = true;
                        }
                    }
                    if (ignore)
                        continue;
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
                    "Redeploy occur unknown error, " + ExceptionUtils.getFullStackTrace(t)
                            + " redeploy aborted!!!");
            if (t instanceof CoreException)
                throw (CoreException) t;
            else {
                LoggerEx.error(TAG, "Groovy unknown error " + ExceptionUtils.getFullStackTrace(t) + ", path: " + groovyRuntime.getPath());
                throw new CoreException(ChatErrorCodes.ERROR_GROOVY_UNKNOWN,
                        "Groovy unknown error " + t.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(baos);
            if (deploySuccessfully) {
                if (oldClassLoader != null) {
                    TimerEx.schedule(new TimerTaskEx(GroovyBooter.class.getSimpleName()) {
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
                                        + ExceptionUtils.getFullStackTrace(e));
                            }
                        }
                    }, TimeUnit.SECONDS.toMillis(60)); //release old class loader after 60 seconds.
                    LoggerEx.info(TAG, "Old class loader " + oldClassLoader + " will be released after 60 seconds");
                }
                long version = latestVersion.incrementAndGet();
                newClassLoader.setVersion(version);
                classLoader = newClassLoader;
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
                        + ExceptionUtils.getFullStackTrace(e));
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
