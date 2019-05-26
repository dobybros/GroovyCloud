package sdockerboot.container.bean;

import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.http.MyHttpParameters;
import com.docker.utils.SpringContextUtil;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:56
 */
@Configuration
public class HttpBean extends BeanApp{
    @Bean
    public SpringContextUtil springContextUtil(){
        return new SpringContextUtil();
    }
    @Bean(initMethod = "init")
    public GlobalLansProperties globalLansProperties(){
        GlobalLansProperties globalLansProperties = getGlobalLansProperties();
        globalLansProperties.setPath("container.properties");
        return globalLansProperties;
    }
    @Bean
    public PlainSocketFactory plainSocketFactory(){
        return getPlainSocketFactory();
    }
    @Bean
    public SSLSocketFactory sslSocketFactory(){
        return getSslSocketFactory();
    }
    @Bean
    public Scheme httpScheme(){
        return getHttpScheme();
    }
    @Bean
    public Scheme httpsScheme(){
        return getHttpsScheme();
    }
    @Bean
    public SchemeRegistry schemeRegistry(){
        SchemeRegistry schemeRegistry = getSchemeRegistry();
        Map map = new HashMap();
        map.put("http", getHttpScheme());
        map.put("https", getHttpsScheme());
        schemeRegistry.setItems(map);
        return schemeRegistry;
    }
    @Bean(destroyMethod = "shutdown")
    public ThreadSafeClientConnManager clientConnectionManager(){
        ThreadSafeClientConnManager clientConnManager = getClientConnManager();
        clientConnManager.setMaxTotal(20);
        return clientConnManager;
    }
    @Bean
    public DefaultHttpClient httpClient(){
        return getHttpClient();
    }
}
