package sdockerboot.container.bean;

import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.services.impl.ConsumeQueueService;
import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;
import com.dobybros.file.adapters.GridFSFileHandler;
import com.dobybros.gateway.channels.tcp.UpStreamHandler;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalCodecFactory;
import com.dobybros.http.MyHttpParameters;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.LansDAO;
import com.docker.storage.mongodb.daos.SDockerDAO;
import com.docker.storage.mongodb.daos.ServersDAO;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:41
 */

public class BeanApp extends ConfigApp{
    private GlobalLansProperties globalLansProperties;
    private PlainSocketFactory plainSocketFactory;
    private SSLSocketFactory sslSocketFactory;
    private Scheme httpScheme;
    private Scheme httpsScheme;
    private SchemeRegistry schemeRegistry;
    private ThreadSafeClientConnManager clientConnManager;
    private DefaultHttpClient httpClient;
    private MongoHelper dockerStatusHelper;
    private com.dobybros.chat.storage.mongodb.MongoHelper logsHelper;
    private MongoHelper configHelper;
    private ServersDAO serversDAO;
    private LansDAO lansDAO;
    private SDockerDAO sdockerDAO;
    private BulkLogDAO bulkLogDAO;
    private GridFSFileHandler fileAdapter;
    private com.dobybros.chat.storage.mongodb.MongoHelper gridfsHelper;
    private UpStreamHandler upstreamHandler;
    private ProtocolCodecFilter tcpCodecFilter;
    private DefaultIoFilterChainBuilder tcpFilterChainBuilder;
    private NioSocketAcceptorEx tcpIoAcceptor;
    private ProtocolCodecFilter sslTcpCodecFilter;
    private HailProtocalCodecFactory hailProtocalCodecFactory;
    private KeyStoreFactory keystoreFactory;
    private SslContextFactory sslContextFactory;
    private SslFilter sslFilter;
    private DefaultIoFilterChainBuilder sslTcpFilterChainBuilder;
    private NioSocketAcceptorEx sslTcpIoAcceptor;

    public NioSocketAcceptorEx getSslTcpIoAcceptor() {
        if(sslTcpIoAcceptor == null){
            sslTcpIoAcceptor = new NioSocketAcceptorEx();
        }
        return sslTcpIoAcceptor;
    }

    public DefaultIoFilterChainBuilder getSslTcpFilterChainBuilder() {
        if(sslTcpFilterChainBuilder == null){
            sslTcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
        }
        return sslTcpFilterChainBuilder;
    }

    public synchronized SslFilter getSslFilter() {
        if(sslFilter == null){
            try {
                sslFilter = new SslFilter(getSslContextFactory().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslFilter;
    }

    public synchronized SslContextFactory getSslContextFactory() {
        if(sslContextFactory == null){
            sslContextFactory = new SslContextFactory();
        }
        return sslContextFactory;
    }

    public synchronized KeyStoreFactory getKeystoreFactory() {
        if(keystoreFactory == null){
            keystoreFactory = new KeyStoreFactory();
        }
        return keystoreFactory;
    }

    public synchronized ProtocolCodecFilter getSslTcpCodecFilter() {
        if(sslTcpCodecFilter == null){
            sslTcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return sslTcpCodecFilter;
    }

    public synchronized NioSocketAcceptorEx getTcpIoAcceptor() {
        if(tcpIoAcceptor == null){
            tcpIoAcceptor = new NioSocketAcceptorEx();
        }
        return tcpIoAcceptor;
    }

    public synchronized GlobalLansProperties getGlobalLansProperties() {
        if(globalLansProperties == null){
            globalLansProperties = new GlobalLansProperties();
        }
        return globalLansProperties;
    }

    public synchronized PlainSocketFactory getPlainSocketFactory() {
        if(plainSocketFactory == null){
            plainSocketFactory = PlainSocketFactory.getSocketFactory();
        }
        return plainSocketFactory;
    }

    public synchronized SSLSocketFactory getSslSocketFactory() {
        if(sslSocketFactory == null){
            sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }
        return sslSocketFactory;
    }

    public synchronized Scheme getHttpScheme() {
        if(httpScheme == null){
            httpScheme = new Scheme("http", 80, getPlainSocketFactory());
        }
        return httpScheme;
    }

    public synchronized Scheme getHttpsScheme() {
        if(httpsScheme == null){
            httpsScheme = new Scheme("https", 443, getSslSocketFactory());
        }
        return httpsScheme;
    }

    public synchronized SchemeRegistry getSchemeRegistry() {
        if(schemeRegistry == null){
            schemeRegistry = new SchemeRegistry();
        }
        return schemeRegistry;
    }

    public synchronized ThreadSafeClientConnManager getClientConnManager() {
        if(clientConnManager == null){
            clientConnManager = new ThreadSafeClientConnManager(getSchemeRegistry());
        }
        return clientConnManager;
    }

    public synchronized DefaultHttpClient getHttpClient() {
        if(httpClient == null){
            MyHttpParameters myHttpParameters = new MyHttpParameters();
            myHttpParameters.setCharset("utf8");
            myHttpParameters.setConnectionTimeout(30000);
            myHttpParameters.setSocketTimeout(30000);
            httpClient = new DefaultHttpClient(getClientConnManager(), myHttpParameters);
        }
        return httpClient;
    }

    public synchronized MongoHelper getDockerStatusHelper() {
        if(dockerStatusHelper == null){
            dockerStatusHelper = new MongoHelper();
        }
        return dockerStatusHelper;
    }

    public synchronized com.dobybros.chat.storage.mongodb.MongoHelper getLogsHelper() {
        if(logsHelper == null){
            logsHelper = new com.dobybros.chat.storage.mongodb.MongoHelper();
        }
        return logsHelper;
    }

    public synchronized MongoHelper getConfigHelper() {
        if(configHelper == null){
            configHelper = new MongoHelper();
        }
        return configHelper;
    }

    public synchronized ServersDAO getServersDAO() {
        if(serversDAO == null){
            serversDAO = new ServersDAO();
        }
        return serversDAO;
    }

    public synchronized LansDAO getLansDAO() {
        if(lansDAO == null){
            lansDAO = new LansDAO();
        }
        return lansDAO;
    }

    public synchronized SDockerDAO getSdockerDAO() {
        if(sdockerDAO == null){
            sdockerDAO = new SDockerDAO();
        }
        return sdockerDAO;
    }

    public synchronized BulkLogDAO getBulkLogDAO() {
        if(bulkLogDAO == null){
            bulkLogDAO = new BulkLogDAO();
        }
        return bulkLogDAO;
    }

    public synchronized com.dobybros.chat.storage.mongodb.MongoHelper getGridfsHelper() {
        if(gridfsHelper == null){
            gridfsHelper = new com.dobybros.chat.storage.mongodb.MongoHelper();
        }
        return gridfsHelper;
    }

    public synchronized GridFSFileHandler getFileAdapter() {
        if(fileAdapter == null){
            fileAdapter = new GridFSFileHandler();
        }
        return fileAdapter;
    }

    public synchronized UpStreamHandler getUpstreamHandler() {
        if(upstreamHandler == null){
            upstreamHandler = new UpStreamHandler();
        }
        return upstreamHandler;
    }

    public synchronized HailProtocalCodecFactory getHailProtocalCodecFactory() {
        if(hailProtocalCodecFactory == null){
            hailProtocalCodecFactory = new HailProtocalCodecFactory();
        }
        return hailProtocalCodecFactory;
    }

    public synchronized ProtocolCodecFilter getTcpCodecFilter() {
        if(tcpCodecFilter == null){
            tcpCodecFilter = new ProtocolCodecFilter(getHailProtocalCodecFactory());
        }
        return tcpCodecFilter;
    }

    public synchronized DefaultIoFilterChainBuilder getTcpFilterChainBuilder() {
        if(tcpFilterChainBuilder == null){
            tcpFilterChainBuilder = new DefaultIoFilterChainBuilder();
        }
        return tcpFilterChainBuilder;
    }
}
