package proxycontainer.proxycontainer.bean;

import com.docker.script.ScriptManager;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.ProxyUpStreamHandler;
import com.proxy.runtime.ProxyGroovyRuntime;
import imcontainer.imcontainer.bean.IMBeanApp;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;

import java.net.InetSocketAddress;

/**
 * @author lick
 * @date 2019/11/12
 */
public class ProxyBeanApp extends IMBeanApp {
    private static volatile ProxyBeanApp instance;
    private ProxyUpStreamHandler proxyUpStreamHandler;
    private ProxyAnnotationHandler proxyAnnotationHandler;
    private NioSocketAcceptorEx wsIoAcceptor;
    private NioSocketAcceptorEx sslTcpIoAcceptor;
    private NioSocketAcceptorEx tcpIoAcceptor;
    private ScriptManager scriptManager;

    public synchronized NioSocketAcceptorEx getTcpIoAcceptor() {
        if(instance.tcpIoAcceptor == null){
            instance.tcpIoAcceptor = new NioSocketAcceptorEx();
            instance.tcpIoAcceptor.setHandler(instance.getProxyUpStreamHandler());
            instance.tcpIoAcceptor.setFilterChainBuilder(instance.getTcpFilterChainBuilder());
            instance.tcpIoAcceptor.setReuseAddress(true);
            instance.tcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamPort())));
        }
        return instance.tcpIoAcceptor;
    }
    public synchronized NioSocketAcceptorEx getSslTcpIoAcceptor() {
        if(instance.sslTcpIoAcceptor == null){
            instance.sslTcpIoAcceptor = new NioSocketAcceptorEx();
            instance.sslTcpIoAcceptor.setHandler(instance.getProxyUpStreamHandler());
            instance.sslTcpIoAcceptor.setFilterChainBuilder(instance.getSslTcpFilterChainBuilder());
            instance.sslTcpIoAcceptor.setReuseAddress(true);
            instance.sslTcpIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamSslPort())));
        }
        return instance.sslTcpIoAcceptor;
    }
    public synchronized ScriptManager getScriptManager() {
        if (instance.scriptManager == null) {
            instance.scriptManager = new ScriptManager();
            instance.scriptManager.setLocalPath(instance.getLocalPath());
            instance.scriptManager.setRemotePath(instance.getRemotePath());
            instance.scriptManager.setBaseRuntimeClass(ProxyGroovyRuntime.class);
            instance.scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
            instance.scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            instance.scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            instance.scriptManager.setUseHulkAdmin(Boolean.valueOf(instance.getUseHulkAdmin()));
            instance.scriptManager.setServerType(instance.getServerType());
        }
        return instance.scriptManager;
    }
    public synchronized NioSocketAcceptorEx getWsIoAcceptor() {
        if(instance.wsIoAcceptor == null){
            instance.wsIoAcceptor = new NioSocketAcceptorEx();
            instance.wsIoAcceptor.setHandler(instance.getProxyUpStreamHandler());
            instance.wsIoAcceptor.setFilterChainBuilder(instance.getWsFilterChainBuilder());
            instance.wsIoAcceptor.setReuseAddress(true);
            instance.wsIoAcceptor.setDefaultLocalAddress(new InetSocketAddress(Integer.valueOf(instance.getUpstreamWsPort())));
        }
        return instance.wsIoAcceptor;
    }

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

    public static ProxyBeanApp getInstance() {
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
