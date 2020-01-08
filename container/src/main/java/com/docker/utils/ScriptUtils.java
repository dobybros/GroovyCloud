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
    }
}
