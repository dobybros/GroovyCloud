package proxycontainer.proxycontainer.bean;

import com.dobybros.chat.props.GlobalLansProperties;
import imcontainer.imcontainer.bean.IMBeanApp;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:56
 */
@Configuration
public class HttpBean{
    private IMBeanApp instance;
    HttpBean(){
        instance = IMBeanApp.getInstance();
    }
//    @Bean(initMethod = "init")
    @Bean
    public GlobalLansProperties globalLansProperties(){
        return instance.getGlobalLansProperties();
    }
    @Bean
    public PlainSocketFactory plainSocketFactory(){
        return instance.getPlainSocketFactory();
    }
    @Bean
    public SSLSocketFactory sslSocketFactory(){
        return instance.getSslSocketFactory();
    }
    @Bean
    public Scheme httpScheme(){
        return instance.getHttpScheme();
    }
    @Bean
    public Scheme httpsScheme(){
        return instance.getHttpsScheme();
    }
    @Bean
    public SchemeRegistry schemeRegistry(){
        return instance.getSchemeRegistry();
    }
    @Bean(destroyMethod = "shutdown")
    public ThreadSafeClientConnManager clientConnectionManager(){
        return instance.getClientConnManager();
    }
    @Bean
    public DefaultHttpClient httpClient(){
        return instance.getHttpClient();
    }
}
