package com.docker.utils;

import chat.logs.LoggerEx;
import com.docker.script.MyBaseRuntime;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by lick on 2019/5/16.
 * Description：
 */
public class ScriptUtils {
    public static void serviceStubProxy(MyBaseRuntime myBaseRuntime, String TAG) {
        String path = myBaseRuntime.getPath();
        String code =
                "package script.groovy.runtime\n" +
                        "@script.groovy.annotation.RedeployMain\n" +
                        "class ServiceStubProxy extends com.docker.rpc.remote.stub.Proxy implements GroovyInterceptable{\n" +
                        "    private Class<?> remoteServiceStub;\n" +
                        "    ServiceStubProxy() {\n" +

                        "        super(null, null);\n" +
                        "    }\n" +
                        "    ServiceStubProxy(Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager, com.docker.rpc.remote.stub.RemoteServerHandler remoteServerHandler) {\n" +
                        "        super(serviceStubManager, remoteServerHandler)\n" +
                        "        this.remoteServiceStub = remoteServiceStub;\n" +
                        "    }\n" +
                        "    def methodMissing(String methodName,methodArgs) {\n" +
                        "        Long crc = chat.utils.ReflectionUtil.getCrc(remoteServiceStub, methodName, remoteServerHandler.getToService());\n" +
                        "        com.docker.rpc.remote.stub.RpcCacheManager.getInstance().putCrcMethodMap(crc, remoteServerHandler.getToService() + '_' + remoteServiceStub.getSimpleName() + '_' + methodName);\n" +
                        "        return invoke(crc, methodArgs);\n" +
                        "    }\n" +
                        "    public static def getProxy(Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager, com.docker.rpc.remote.stub.RemoteServerHandler remoteServerHandler) {\n" +
                        "        ServiceStubProxy proxy = new ServiceStubProxy(remoteServiceStub, serviceStubManager, remoteServerHandler)\n" +
                        "        def theProxy = proxy.asType(proxy.remoteServiceStub)\n" +
                        "        return theProxy\n" +
                        "    }\n" +
                        "    public void main() {\n" +
                        "        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(this.getClass().getClassLoader());\n" +
                        "        baseRuntime.prepareServiceStubProxy();" +
                        "    }\n" +
                        "    public void shutdown(){}\n" +
                        "}";
        try {
            FileUtils.writeStringToFile(new File(path + "/script/groovy/runtime/ServiceStubProxy.groovy"), code, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "write ServiceStubProxy.groovy file on " + (path + "/script/groovy/runtime/ServiceStubProxy.groovy") + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
        String loggerCode = "package chat.logs;\n" +
                "\n" +
                "import chat.utils.ChatUtils;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "import script.groovy.runtime.GroovyRuntime;\n" +
                "\n" +
                "public class LoggerEx {\n" +
                "    private static Logger logger = LoggerFactory.getLogger(\"\");\n" +
                "    private static String LEVEL_FATAL = \"FATAL\";\n" +
                "    private static LogListenerEx logListener;\n" +
                "    private LoggerEx() {\n" +
                "    }\n" +
                "    public interface LogListenerEx {\n" +
                "        public void debug(String log);\n" +
                "\n" +
                "        public void info(String log);\n" +
                "\n" +
                "        public void warn(String log);\n" +
                "\n" +
                "        public void error(String log);\n" +
                "\n" +
                "        public void fatal(String log);\n" +
                "    }\n" +
                "    public static String getClassTag(Class<?> clazz) {\n" +
                "        return clazz.getSimpleName();\n" +
                "    }\n" +
                "    public static void debug(String tag, String msg) {\n" +
                "        String log = getLogMsg(tag, msg);\n" +
                "        if (logListener != null)\n" +
                "            logListener.debug(log);\n" +
                "        else\n" +
                "            logger.debug(log);\n" +
                "    }\n" +
                "    public static void info(String tag, String msg) {\n" +
                "        String log = getLogMsg(tag, msg);\n" +
                "        if (logListener != null)\n" +
                "            logListener.info(log);\n" +
                "        else\n" +
                "            logger.info(log);\n" +
                "    }\n" +
                "    public static void info(String tag, String msg, Long spendTime) {\n" +
                "        String log = getLogMsg(tag, msg, spendTime);\n" +
                "        if (logListener != null)\n" +
                "            logListener.info(log);\n" +
                "        else\n" +
                "            logger.info(log);\n" +
                "    }\n" +
                "    public static void warn(String tag, String msg) {\n" +
                "        String log = getLogMsg(tag, msg);\n" +
                "        if (logListener != null)\n" +
                "            logListener.warn(log);\n" +
                "        else\n" +
                "            logger.warn(log);\n" +
                "    }\n" +
                "    public static void error(String tag, String msg) {\n" +
                "        String log = getLogMsg(tag, msg);\n" +
                "        if (logListener != null)\n" +
                "            logListener.error(log);\n" +
                "        else\n" +
                "            logger.error(log);\n" +
                "    }\n" +
                "    public static void fatal(String tag, String msg) {\n" +
                "        String log = getLogMsgFatal(tag, msg);\n" +
                "        if (logListener != null)\n" +
                "            logListener.fatal(log);\n" +
                "        else\n" +
                "            logger.error(log);\n" +
                "    }\n" +
                "    private static String getLogMsg(String tag, String msg) {\n" +
                "        StringBuilder builder = new StringBuilder();\n" +
                "        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(chat.logs.LoggerEx.class.getClassLoader());\n" +
                "        String serviceName = null;\n" +
                "        if(baseRuntime != null){\n" +
                "            serviceName = baseRuntime.getServiceName();\n" +
                "        }\n" +
                "        builder.append(\"\\$\\$time:: \" + ChatUtils.dateString()).\n" +
                "                append(\" \\$\\$tag:: \" + tag).\n" +
                "                append(\" \").\n" +
                "                append(\"[\" + msg + \"]\");\n" +
                "        if(serviceName != null){\n" +
                "            builder.append(\" \\$\\$serviceName:: \" + serviceName);\n" +
                "        }\n" +
                "        return builder.toString();\n" +
                "    }\n" +
                "   private static String getLogMsgFatal(String tag, String msg) {\n" +
                "        StringBuilder builder = new StringBuilder();\n" +
                "        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(chat.logs.LoggerEx.class.getClassLoader());\n" +
                "        String serviceName = null;\n" +
                "        if (baseRuntime != null) {\n" +
                "            serviceName = baseRuntime.getServiceName();\n" +
                "        }\n" +
                "        builder.append(LEVEL_FATAL).\n" +
                "                append(\" \\$\\$time:: \" + ChatUtils.dateString()).\n" +
                "                append(\" \\$\\$tag:: \" + tag).\n" +
                "                append(\" \").\n" +
                "                append(\"[\" + msg + \"]\");\n" +
                "        if (serviceName != null) {\n" +
                "            builder.append(\" \\$\\$serviceName:: \" + serviceName);\n" +
                "        }\n" +
                "        return builder.toString();\n" +
                "       }\n" +
                "    private static String getLogMsg(String tag, String msg, Long spendTime) {\n" +
                "        StringBuilder builder = new StringBuilder();\n" +
                "        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(chat.logs.LoggerEx.class.getClassLoader());\n" +
                "        String serviceName = null;\n" +
                "        if(baseRuntime != null){\n" +
                "            serviceName = baseRuntime.getServiceName();\n" +
                "        }\n" +
                "        builder.append(\"\\$\\$time:: \" + ChatUtils.dateString()).\n" +
                "                append(\" \\$\\$tag:: \" + tag).\n" +
                "                append(\" [\" + msg + \"]\").\n" +
                "                append(\" \\$\\$spendTime:: \" + spendTime);\n" +
                "        if(serviceName != null){\n" +
                "            builder.append(\" \\$\\$serviceName:: \" + serviceName);\n" +
                "        }\n" +
                "        return builder.toString();\n" +
                "    }\n" +
                "    public static LogListenerEx getLogListener() {\n" +
                "        return logListener;\n" +
                "    }\n" +
                "    public static void setLogListener(LogListenerEx logListener) {\n" +
                "        LoggerEx.logListener = logListener;\n" +
                "    }\n" +
                "}";
        try {
            FileUtils.writeStringToFile(new File(path + "/chat/logs/LoggerEx.groovy"), loggerCode, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "write ServiceStubProxy.groovy file on " + (path + "/script/groovy/runtime/ServiceStubProxy.groovy") + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }
}
