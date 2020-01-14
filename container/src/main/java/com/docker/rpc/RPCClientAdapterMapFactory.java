package com.docker.rpc;


import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by lick on 2019/10/15.
 * Description：
 */
public class RPCClientAdapterMapFactory {
    private static final String TAG = RPCClientAdapterMapFactory.class.getSimpleName();
    private static volatile RPCClientAdapterMapFactory instance;
    private RPCClientAdapterMap rpcClientAdapterMap;
    private RPCClientAdapterMap rpcClientAdapterMapSsl;

    public RPCClientAdapterMap getRpcClientAdapterMap() {
        if(rpcClientAdapterMap == null){
            rpcClientAdapterMap = new RPCClientAdapterMap();
        }
        return rpcClientAdapterMap;
    }

    public RPCClientAdapterMap getRpcClientAdapterMapSsl() {
        if(rpcClientAdapterMapSsl == null){
            rpcClientAdapterMapSsl = new RPCClientAdapterMap();
            rpcClientAdapterMapSsl.setEnableSsl(true);
            ClassPathResource resource = new ClassPathResource("groovycloud.properties");
            Properties pro = new Properties();
            try {
                pro.load(resource.getInputStream());
                rpcClientAdapterMapSsl.setRpcSslClientTrustJksPath(pro.getProperty("rpc.ssl.clientTrust.jks.path"));
                rpcClientAdapterMapSsl.setRpcSslServerJksPath(pro.getProperty("rpc.ssl.server.jks.path"));
                rpcClientAdapterMapSsl.setRpcSslJksPwd(pro.getProperty("rpc.ssl.jks.pwd"));
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Prepare lan.properties is failed, " + ExceptionUtils.getFullStackTrace(e));
            }
        }
        return rpcClientAdapterMapSsl;
    }

    public static synchronized RPCClientAdapterMapFactory getInstance(){
        if(instance == null){
            synchronized (RPCClientAdapterMapFactory.class){
                if(instance == null){
                    instance = new RPCClientAdapterMapFactory();
                }
            }
        }
        return instance;
    }

}
