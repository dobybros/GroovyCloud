package proxycontainer.proxycontainer.bean;

import com.proxy.im.ProxyUpStreamHandler;
import com.proxy.im.ProxyAnnotationHandler;
import imcontainer.imcontainer.bean.IMBeanApp;

/**
 * @author lick
 * @date 2019/11/12
 */
public class ProxyBeanApp extends IMBeanApp {
    private static ProxyBeanApp instance;
    private ProxyUpStreamHandler proxyUpStreamHandler;
    private ProxyAnnotationHandler proxyAnnotationHandler;

    public synchronized ProxyUpStreamHandler getProxyUpStreamHandler() {
        if (instance.proxyUpStreamHandler == null) {
            instance.proxyUpStreamHandler = new ProxyUpStreamHandler();
            instance.proxyUpStreamHandler.setReadIdleTime(720);
            instance.proxyUpStreamHandler.setWriteIdleTime(720);
        }
        return instance.proxyUpStreamHandler;
    }

    public synchronized ProxyAnnotationHandler getProxyAnnotationHandler() {
        if (instance.proxyAnnotationHandler == null) {
            instance.proxyAnnotationHandler = new ProxyAnnotationHandler();
        }
        return instance.proxyAnnotationHandler;
    }

    public synchronized static ProxyBeanApp getInstance() {
        if (instance == null) {
            synchronized (ProxyBeanApp.class) {
                if (instance == null) {
                    instance = new ProxyBeanApp();
                }
            }
        }
        return instance;
    }
}
