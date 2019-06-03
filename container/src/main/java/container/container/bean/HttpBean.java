package container.container.bean;

import com.dobybros.chat.props.GlobalLansProperties;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:56
 */
@Configuration
public class HttpBean{
    private BeanApp instance;
    HttpBean(){
        instance = BeanApp.getInstance();
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
        SchemeRegistry schemeRegistry = instance.getSchemeRegistry();
        Map map = new HashMap();
        map.put("http", instance.getHttpScheme());
        map.put("https", instance.getHttpsScheme());
        schemeRegistry.setItems(map);
        return schemeRegistry;
    }
    @Bean(destroyMethod = "shutdown")
    public ThreadSafeClientConnManager clientConnectionManager(){
        ThreadSafeClientConnManager clientConnManager = instance.getClientConnManager();
        clientConnManager.setMaxTotal(20);
        return clientConnManager;
    }
    @Bean
    public DefaultHttpClient httpClient(){
        return instance.getHttpClient();
    }
}
