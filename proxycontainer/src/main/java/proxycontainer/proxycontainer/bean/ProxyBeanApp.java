package proxycontainer.proxycontainer.bean;

import com.dobybros.gateway.channels.websocket.netty.WebSocketChannelInitializer;
import com.dobybros.gateway.channels.websocket.netty.WebSocketManager;
import com.docker.script.ScriptManager;
import com.proxy.im.ProxyAnnotationHandler;
import com.proxy.im.netty.ProxyWebSocketHandler;
import com.proxy.runtime.ProxyGroovyRuntime;
import imcontainer.imcontainer.bean.IMBeanApp;
import script.file.FileAdapter;
import script.file.LocalFileHandler;

/**
 * @author lick
 * @date 2019/11/12
 */
public class ProxyBeanApp extends IMBeanApp {
    private static volatile ProxyBeanApp instance;
    private ProxyAnnotationHandler proxyAnnotationHandler;
    private ScriptManager scriptManager;
    private WebSocketChannelInitializer webSocketChannelInitializer;
    private WebSocketManager webSocketManager;

    public synchronized ScriptManager getScriptManager() {
        if (instance.scriptManager == null) {
            instance.scriptManager = new ScriptManager();
            instance.scriptManager.setLocalPath(instance.getLocalPath());
            FileAdapter fileAdapter = null;
            if(instance.getRemotePath().startsWith("local:")){
                fileAdapter = new LocalFileHandler();
                ((LocalFileHandler)fileAdapter).setRootPath("");
                instance.scriptManager.setRemotePath(instance.getRemotePath().split("local:")[1]);
            }else {
                fileAdapter = getFileAdapter();
                instance.scriptManager.setRemotePath(instance.getRemotePath());
            }
            instance.scriptManager.setFileAdapter(fileAdapter);
            instance.scriptManager.setBaseRuntimeClass(ProxyGroovyRuntime.class);
            instance.scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
            instance.scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            instance.scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            instance.scriptManager.setUseHulkAdmin(Boolean.valueOf(instance.getUseHulkAdmin()));
            instance.scriptManager.setServerType(instance.getServerType());
        }
        return instance.scriptManager;
    }

    public synchronized ProxyAnnotationHandler getProxyAnnotationHandler() {
        if (instance.proxyAnnotationHandler == null) {
            instance.proxyAnnotationHandler = new ProxyAnnotationHandler();
        }
        return instance.proxyAnnotationHandler;
    }

    @Override
    public synchronized WebSocketChannelInitializer getWebSocketChannelInitializer() {
        if (instance.webSocketChannelInitializer == null)
            instance.webSocketChannelInitializer = new WebSocketChannelInitializer(instance.getWebSocketProperties(), ProxyWebSocketHandler.class);
        return instance.webSocketChannelInitializer;
    }

    @Override
    public synchronized WebSocketManager getWebSocketManager() {
        if (instance.webSocketManager == null) {
            instance.webSocketManager = new WebSocketManager(instance.getWebSocketProperties(), instance.getWebSocketChannelInitializer());
        }
        return instance.webSocketManager;
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
