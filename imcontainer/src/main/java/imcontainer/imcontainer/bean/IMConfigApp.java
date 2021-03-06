package imcontainer.imcontainer.bean;

import container.container.bean.BeanApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 19:23
 */
public class IMConfigApp extends BeanApp {
    private String upstreamPort;
    private String keystorePwd;
    private String keystorePath;
    private String keymanagerPwd;
    private String upstreamSslPort;
    private String upstreamWsPort;
    private String publicWsPort;
    private Boolean useProxy;
    private Long maxUserNumber;

    public IMConfigApp(){
        super();
        InputStream inStream = IMConfigApp.class.getClassLoader().getResourceAsStream("groovycloud.properties");
        Properties prop = new Properties();
        try {
            prop.load(inStream);
            upstreamPort = prop.getProperty("upstream-port");
            keystorePwd = prop.getProperty("keystore.pwd");
            keystorePath = prop.getProperty("keystore.path");
            keymanagerPwd = prop.getProperty("keymanager.pwd");
            upstreamSslPort = prop.getProperty("upstream-ssl-port");
            upstreamWsPort = prop.getProperty("upstream-ws-port");
            publicWsPort = prop.getProperty("public-ws-port");
            useProxy = Boolean.valueOf(prop.getProperty("useProxy"));
            String maxUserNumberStr = prop.getProperty("maxUserNumber");
            if(maxUserNumberStr != null){
                maxUserNumber = Long.parseLong(maxUserNumberStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUpstreamPort() {
        return upstreamPort;
    }

    public String getKeystorePwd() {
        return keystorePwd;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeymanagerPwd() {
        return keymanagerPwd;
    }

    public String getUpstreamSslPort() {
        return upstreamSslPort;
    }

    public String getUpstreamWsPort() {
        return upstreamWsPort;
    }

    public Boolean getUseProxy() {
        return useProxy;
    }

    public String getPublicWsPort() {
        return publicWsPort;
    }

    public Long getMaxUserNumber() {
        return maxUserNumber;
    }
}
