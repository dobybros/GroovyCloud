package com.proxy.runtime;

import com.docker.script.MyBaseRuntime;
import com.docker.utils.SpringContextUtil;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.ProxyUpStreamAnnotationHandler;

import java.util.Properties;

/**
 * @author lick
 * @date 2019/11/12
 */
public class ProxyGroovyRuntime extends MyBaseRuntime {
    private final String TAG = ProxyGroovyRuntime.class.getSimpleName();
    @Override
    public void prepare(String service, Properties properties, String localScriptPath) {
        super.prepare(service, properties, localScriptPath);
        ProxyAnnotationHandler proxyAnnotationHandler = (ProxyAnnotationHandler) SpringContextUtil.getBean("proxyAnnotationHandler");
        if(proxyAnnotationHandler != null){
            addClassAnnotationGlobalHandler(proxyAnnotationHandler);
        }
        ProxyUpStreamAnnotationHandler proxyUpStreamAnnotationHandler = (ProxyUpStreamAnnotationHandler)SpringContextUtil.getBean("proxyUpStreamAnnotationHandler");
        if(proxyUpStreamAnnotationHandler != null){
            addClassAnnotationGlobalHandler(proxyUpStreamAnnotationHandler);
        }
    }
}
