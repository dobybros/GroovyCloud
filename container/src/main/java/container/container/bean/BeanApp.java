package container.container.bean;

import chat.utils.IPHolder;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.http.MyHttpParameters;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.storage.adapters.impl.*;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
import com.docker.storage.redis.RedisListenerHandler;
import com.docker.storage.redis.RedisSubscribeHandler;
import com.docker.storage.zookeeper.ZookeeperFactory;
import com.docker.tasks.RepairTaskHandler;
import com.docker.utils.AutoReloadProperties;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import script.filter.JsonFilterFactory;
import script.groovy.servlets.RequestPermissionHandler;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

//import com.dobybros.chat.log.LogIndexQueue;
//import com.dobybros.chat.storage.mongodb.daos.BulkLogDAO;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 15:41
 */

public class BeanApp extends ConfigApp{
    private static final String TAG = BeanApp.class.getSimpleName();
    private static volatile BeanApp instance;
    private SpringContextUtil springContextUtil;
    private PlainSocketFactory plainSocketFactory;
    private SSLSocketFactory sslSocketFactory;
    private DefaultHttpClient httpClient;
    private Scheme httpScheme;
    private Scheme httpsScheme;
    private SchemeRegistry schemeRegistry;
    private ThreadSafeClientConnManager clientConnManager;
    private MongoHelper dockerStatusHelper;
    private MongoHelper logsHelper;
    private MongoHelper configHelper;
    private MongoHelper scheduledTaskHelper;
    private MongoHelper repairHelper;
    private DockerStatusDAO dockerStatusDAO;
    private ServersDAO serversDAO;
    private LansDAO lansDAO;
    private SDockerDAO sdockerDAO;
    private ServiceVersionDAO serviceVersionDAO;
    private GridFSFileHandler fileAdapter;
    private MongoHelper gridfsHelper;
    private DockerStatusServiceImpl dockerStatusService;
    private IPHolder ipHolder;
    private JsonFilterFactory jsonFilterFactory;
    private RequestPermissionHandler requestPermissionHandler;
    private AutoReloadProperties oauth2ClientProperties;
    private ScriptManager scriptManager;
    private OnlineServerWithStatus onlineServer;
    private RPCClientAdapterMap rpcClientAdapterMap;
    private RPCClientAdapterMap rpcClientAdapterMapSsl;
    private RMIServerImplWrapper rpcServer;
    private RMIServerImplWrapper rpcServerSsl;
    private RMIServerImplWrapper dockerRpcServer;
    private RMIServerHandler dockerRpcServerAdapter;
    private RMIServerImplWrapper dockerRpcServerSsl;
    private RMIServerHandler dockerRpcServerAdapterSsl;
    private ServersServiceImpl serversService;
    private ServiceVersionServiceImpl serviceVersionService;
    private ScheduledTaskServiceImpl scheduledTaskService;
    private RepairServiceImpl repairService;
    private ScheduledTaskDAO scheduledTaskDAO;
    private RepairDAO repairDAO;
    private RedisSubscribeHandler redisSubscribeHandler;
    private RepairTaskHandler repairTaskHandler;
    private RedisListenerHandler redisListenerHandler;
    private ZookeeperFactory zookeeperFactory;
    public synchronized ZookeeperFactory getZookeeperFactory() {
        if (instance.zookeeperFactory == null) {
            instance.zookeeperFactory = new ZookeeperFactory();
        }
        return instance.zookeeperFactory;
    }
    public synchronized RepairTaskHandler getRepairTaskHandler() {
        if (instance.repairTaskHandler == null) {
            instance.repairTaskHandler = new RepairTaskHandler();
        }
        return instance.repairTaskHandler;
    }
    public synchronized RedisSubscribeHandler getRedisSubscribeHandler() {
        if (instance.redisSubscribeHandler == null) {
            instance.redisSubscribeHandler = new RedisSubscribeHandler();
        }
        return instance.redisSubscribeHandler;
    }
    public synchronized ScheduledTaskServiceImpl getScheduledTaskService() {
        if (instance.scheduledTaskService == null) {
            instance.scheduledTaskService = new ScheduledTaskServiceImpl();
            instance.scheduledTaskService.setScheduledTaskDAO(instance.getScheduledTaskDAO());
        }
        return instance.scheduledTaskService;
    }
    public synchronized RedisListenerHandler getRedisListenerHandler() {
        if (instance.redisListenerHandler == null) {
            instance.redisListenerHandler = new RedisListenerHandler();
        }
        return instance.redisListenerHandler;
    }
    public synchronized RepairServiceImpl getRepairService() {
        if (instance.repairService == null) {
            instance.repairService = new RepairServiceImpl();
        }
        return instance.repairService;
    }

    public synchronized ScheduledTaskDAO getScheduledTaskDAO() {
        if (instance.scheduledTaskDAO == null) {
            instance.scheduledTaskDAO = new ScheduledTaskDAO();
            instance.scheduledTaskDAO.setMongoHelper(instance.getScheduledTaskHelper());
        }
        return instance.scheduledTaskDAO;
    }

    public synchronized RepairDAO getRepairDAO() {
        if (instance.repairDAO == null) {
            instance.repairDAO = new RepairDAO();
            instance.repairDAO.setMongoHelper(instance.getRepairHelper());
        }
        return instance.repairDAO;
    }

    public synchronized ServiceVersionServiceImpl getServiceVersionService() {
        if (instance.serviceVersionService == null) {
            instance.serviceVersionService = new ServiceVersionServiceImpl();
            instance.serviceVersionService.setServiceVersionDAO(instance.getServiceVersionDAO());
        }
        return instance.serviceVersionService;
    }

    public synchronized ServiceVersionDAO getServiceVersionDAO() {
        if (instance.serviceVersionDAO == null) {
            instance.serviceVersionDAO = new ServiceVersionDAO();
            instance.serviceVersionDAO.setMongoHelper(instance.getDockerStatusHelper());
        }
        return instance.serviceVersionDAO;
    }

    public synchronized ServersServiceImpl getServersService() {
        if (instance.serversService == null) {
            instance.serversService = new ServersServiceImpl();
        }
        return instance.serversService;
    }

    public synchronized DockerStatusDAO getDockerStatusDAO() {
        if (instance.dockerStatusDAO == null) {
            instance.dockerStatusDAO = new DockerStatusDAO();
            instance.dockerStatusDAO.setMongoHelper(instance.getDockerStatusHelper());
        }
        return instance.dockerStatusDAO;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapterSsl() {
        if (instance.dockerRpcServerAdapterSsl == null) {
            instance.dockerRpcServerAdapterSsl = new RMIServerHandler();
            instance.dockerRpcServerAdapterSsl.setServerImpl(instance.getDockerRpcServerSsl());
            instance.dockerRpcServerAdapterSsl.setIpHolder(instance.getIpHolder());
            instance.dockerRpcServerAdapterSsl.setRmiPort(Integer.valueOf(instance.getSslRpcPort()));
            instance.dockerRpcServerAdapterSsl.setEnableSsl(true);
            instance.dockerRpcServerAdapterSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            instance.dockerRpcServerAdapterSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            instance.dockerRpcServerAdapterSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        }
        return instance.dockerRpcServerAdapterSsl;
    }

    public synchronized RMIServerImplWrapper getDockerRpcServerSsl() {
        if (instance.dockerRpcServerSsl == null) {
            try {
                instance.dockerRpcServerSsl = instance.getRpcServerSsl();
//                dockerRpcServerSsl = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
                instance.dockerRpcServerSsl.setRmiServerHandler(instance.getDockerRpcServerAdapterSsl());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return instance.dockerRpcServerSsl;
    }

    public synchronized RMIServerHandler getDockerRpcServerAdapter() {
        if (instance.dockerRpcServerAdapter == null) {
            instance.dockerRpcServerAdapter = new RMIServerHandler();
            instance.dockerRpcServerAdapter.setServerImpl(instance.getDockerRpcServer());
            instance.dockerRpcServerAdapter.setIpHolder(instance.getIpHolder());
            instance.dockerRpcServerAdapter.setRmiPort(Integer.valueOf(instance.getRpcPort()));
        }
        return instance.dockerRpcServerAdapter;
    }

    public synchronized RMIServerImplWrapper getDockerRpcServer() {
        if (instance.dockerRpcServer == null) {
            try {
                instance.dockerRpcServer = instance.getRpcServer();
//                dockerRpcServer = new com.docker.rpc.impl.RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
                instance.dockerRpcServer.setRmiServerHandler(instance.getDockerRpcServerAdapter());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return instance.dockerRpcServer;
    }

    public synchronized RMIServerImplWrapper getRpcServerSsl() {
        if (instance.rpcServerSsl == null) {
            try {
                instance.rpcServerSsl = new RMIServerImplWrapper(Integer.valueOf(getSslRpcPort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return instance.rpcServerSsl;
    }

    public synchronized AutoReloadProperties getOauth2ClientProperties() {
        if(instance.oauth2ClientProperties == null){
            instance.oauth2ClientProperties = new AutoReloadProperties();
            instance.oauth2ClientProperties.setPath("groovycloud.properties");
        }
        return instance.oauth2ClientProperties;
    }

    public synchronized RMIServerImplWrapper getRpcServer() {
        if (instance.rpcServer == null) {
            try {
                instance.rpcServer = new RMIServerImplWrapper(Integer.valueOf(getRpcPort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return instance.rpcServer;
    }

    public synchronized RPCClientAdapterMap getRpcClientAdapterMapSsl() {
        if (instance.rpcClientAdapterMapSsl == null) {
            instance.rpcClientAdapterMapSsl = new RPCClientAdapterMap();
            instance.rpcClientAdapterMapSsl.setEnableSsl(true);
            instance.rpcClientAdapterMapSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            instance.rpcClientAdapterMapSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            instance.rpcClientAdapterMapSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        }
        return instance.rpcClientAdapterMapSsl;
    }

    public synchronized RPCClientAdapterMap getRpcClientAdapterMap() {
        if (instance.rpcClientAdapterMap == null) {
            instance.rpcClientAdapterMap = new RPCClientAdapterMap();
        }
        return instance.rpcClientAdapterMap;
    }

    public synchronized OnlineServerWithStatus getOnlineServer() {
        if (instance.onlineServer == null) {
            instance.onlineServer = new OnlineServerWithStatus();
            instance.onlineServer.setDockerStatusService(instance.getDockerStatusService());
            instance.onlineServer.setServerType(instance.getServerType());
            instance.onlineServer.setDockerName(instance.getDockerName());
            instance.onlineServer.setHttpPort(Integer.valueOf(instance.getServerPort()));
            instance.onlineServer.setInternalKey(instance.getInternalKey());
            instance.onlineServer.setRpcPort(instance.getRpcPort());
            instance.onlineServer.setSslRpcPort(instance.getSslRpcPort());
            instance.onlineServer.setPublicDomain(instance.getPublicDomain());
            instance.onlineServer.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
            instance.onlineServer.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
            instance.onlineServer.setRpcSslJksPwd(instance.getRpcSslJksPwd());
            instance.onlineServer.setMaxUsers(Integer.valueOf(instance.getMaxUsers()));
            instance.onlineServer.setStatus(1);
            if(StringUtils.isNotBlank(instance.getScaleInstanceId())){
                instance.onlineServer.setScaleInstanceId(instance.getScaleInstanceId());
            }
            instance.onlineServer.setType(Integer.valueOf(instance.getType()));
            instance.onlineServer.setConfigPath("groovycloud.properties");
            instance.onlineServer.setIpHolder(instance.getIpHolder());
        }
        return instance.onlineServer;
    }

    public synchronized ScriptManager getScriptManager() {
        if (instance.scriptManager == null) {
            instance.scriptManager = new ScriptManager();
            instance.scriptManager.setLocalPath(instance.getLocalPath());
            instance.scriptManager.setRemotePath(instance.getRemotePath());
            instance.scriptManager.setBaseRuntimeClass(MyBaseRuntime.class);
            instance.scriptManager.setRuntimeBootClass(instance.getRuntimeBootClass());
            instance.scriptManager.setHotDeployment(Boolean.valueOf(instance.getHotDeployment()));
            instance.scriptManager.setKillProcess(Boolean.valueOf(instance.getKillProcess()));
            instance.scriptManager.setServerType(instance.getServerType());
        }
        return instance.scriptManager;
    }

    public synchronized RequestPermissionHandler getRequestPermissionHandler() {
        if (instance.requestPermissionHandler == null) {
            instance.requestPermissionHandler = new RequestPermissionHandler();
        }
        return instance.requestPermissionHandler;
    }

    public synchronized JsonFilterFactory getJsonFilterFactory() {
        if (instance.jsonFilterFactory == null) {
            instance.jsonFilterFactory = new JsonFilterFactory();
        }
        return instance.jsonFilterFactory;
    }

    public synchronized IPHolder getIpHolder() {
        if (instance.ipHolder == null) {
            instance.ipHolder = new IPHolder();
            instance.ipHolder.setEthPrefix(instance.getEthPrefix());
            instance.ipHolder.setIpPrefix(instance.getIpPrefix());
        }
        return instance.ipHolder;
    }


    public synchronized DockerStatusServiceImpl getDockerStatusService() {
        if (instance.dockerStatusService == null) {
            instance.dockerStatusService = new DockerStatusServiceImpl();
            instance.dockerStatusService.setDockerStatusDAO(instance.getDockerStatusDAO());
        }
        return instance.dockerStatusService;
    }

    public synchronized SpringContextUtil getSpringContextUtil() {
        if (instance.springContextUtil == null) {
            instance.springContextUtil = new SpringContextUtil();
        }
        return instance.springContextUtil;
    }


    public synchronized PlainSocketFactory getPlainSocketFactory() {
        if (instance.plainSocketFactory == null) {
            instance.plainSocketFactory = PlainSocketFactory.getSocketFactory();
        }
        return instance.plainSocketFactory;
    }

    public synchronized SSLSocketFactory getSslSocketFactory() {
        if (instance.sslSocketFactory == null) {
            instance.sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }
        return instance.sslSocketFactory;
    }

    public synchronized Scheme getHttpScheme() {
        if (instance.httpScheme == null) {
            instance.httpScheme = new Scheme("http", 80, getPlainSocketFactory());
        }
        return instance.httpScheme;
    }

    public synchronized Scheme getHttpsScheme() {
        if (instance.httpsScheme == null) {
            instance.httpsScheme = new Scheme("https", 443, getSslSocketFactory());
        }
        return instance.httpsScheme;
    }
    public synchronized DefaultHttpClient getHttpClient() {
        if(instance.httpClient == null){
            MyHttpParameters myHttpParameters = new MyHttpParameters();
            myHttpParameters.setCharset("utf8");
            myHttpParameters.setConnectionTimeout(30000);
            myHttpParameters.setSocketTimeout(30000);
            instance.httpClient = new DefaultHttpClient(getClientConnManager(), myHttpParameters);
        }
        return instance.httpClient;
    }
    public synchronized SchemeRegistry getSchemeRegistry() {
        if (instance.schemeRegistry == null) {
            instance.schemeRegistry = new SchemeRegistry();
            Map map = new HashMap();
            map.put("http", instance.getHttpScheme());
            map.put("https", instance.getHttpsScheme());
            instance.schemeRegistry.setItems(map);
        }
        return instance.schemeRegistry;
    }

    public synchronized ThreadSafeClientConnManager getClientConnManager() {
        if (instance.clientConnManager == null) {
            instance.clientConnManager = new ThreadSafeClientConnManager(getSchemeRegistry());
            instance.clientConnManager.setMaxTotal(20);
        }
        return instance.clientConnManager;
    }

    public synchronized MongoHelper getDockerStatusHelper() {
        if (instance.dockerStatusHelper == null) {
            instance.dockerStatusHelper = new MongoHelper();
            instance.dockerStatusHelper.setHost(instance.getMongoHost());
            instance.dockerStatusHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            instance.dockerStatusHelper.setDbName(instance.getDbName());
            instance.dockerStatusHelper.setUsername(instance.getMongoUsername());
            instance.dockerStatusHelper.setPassword(instance.getMongoPassword());
        }
        return instance.dockerStatusHelper;
    }

    public synchronized MongoHelper getScheduledTaskHelper() {
        if (instance.scheduledTaskHelper == null) {
            instance.scheduledTaskHelper = new MongoHelper();
            instance.scheduledTaskHelper.setHost(instance.getMongoHost());
            instance.scheduledTaskHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            instance.scheduledTaskHelper.setDbName("scheduled");
            instance.scheduledTaskHelper.setUsername(instance.getMongoUsername());
            instance.scheduledTaskHelper.setPassword(instance.getMongoPassword());
        }
        return instance.scheduledTaskHelper;
    }
    public synchronized MongoHelper getRepairHelper() {
        if (instance.repairHelper == null) {
            instance.repairHelper = new MongoHelper();
            instance.repairHelper.setHost(instance.getMongoHost());
            instance.repairHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            instance.repairHelper.setDbName("extras");
            instance.repairHelper.setUsername(instance.getMongoUsername());
            instance.repairHelper.setPassword(instance.getMongoPassword());
        }
        return instance.repairHelper;
    }

    public synchronized MongoHelper getLogsHelper() {
        if (instance.logsHelper == null) {
            instance.logsHelper = new MongoHelper();
            instance.logsHelper.setHost(instance.getMongoHost());
            instance.logsHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            instance.logsHelper.setDbName(instance.getLogsDBName());
            instance.logsHelper.setUsername(instance.getMongoUsername());
            instance.logsHelper.setPassword(instance.getMongoPassword());
        }
        return instance.logsHelper;
    }

    public synchronized MongoHelper getConfigHelper() {
        if (instance.configHelper == null) {
            instance.configHelper = new MongoHelper();
            instance.configHelper.setHost(instance.getMongoHost());
            instance.configHelper.setConnectionsPerHost(Integer.valueOf(instance.getMongoConnectionsPerHost()));
            instance.configHelper.setDbName(instance.getConfigDBName());
            instance.configHelper.setUsername(instance.getMongoUsername());
            instance.configHelper.setPassword(instance.getMongoPassword());
        }
        return instance.configHelper;
    }

    public synchronized ServersDAO getServersDAO() {
        if (instance.serversDAO == null) {
            instance.serversDAO = new ServersDAO();
            instance.serversDAO.setMongoHelper(instance.getConfigHelper());
        }
        return instance.serversDAO;
    }

    public synchronized LansDAO getLansDAO() {
        if (instance.lansDAO == null) {
            instance.lansDAO = new LansDAO();
            instance.lansDAO.setMongoHelper(instance.getConfigHelper());
        }
        return instance.lansDAO;
    }

    public synchronized SDockerDAO getSdockerDAO() {
        if (instance.sdockerDAO == null) {
            instance.sdockerDAO = new SDockerDAO();
            instance.sdockerDAO.setMongoHelper(instance.getConfigHelper());
        }
        return instance.sdockerDAO;
    }

    public synchronized MongoHelper getGridfsHelper() {
        if (instance.gridfsHelper == null) {
            instance.gridfsHelper = new MongoHelper();
            instance.gridfsHelper.setHost(instance.getGridHost());
            instance.gridfsHelper.setConnectionsPerHost(Integer.valueOf(instance.getGirdConnectionsPerHost()));
            instance.gridfsHelper.setDbName(instance.getGridDbName());
            instance.gridfsHelper.setUsername(instance.getGridUsername());
            instance.gridfsHelper.setPassword(instance.getGridPassword());
        }
        return instance.gridfsHelper;
    }

    public synchronized GridFSFileHandler getFileAdapter() {
        if (instance.fileAdapter == null) {
            instance.fileAdapter = new GridFSFileHandler();
            instance.fileAdapter.setResourceHelper(instance.getGridfsHelper());
            instance.fileAdapter.setBucketName(instance.getFileBucket());
        }
        return instance.fileAdapter;
    }

    public static BeanApp getInstance() {
        if (instance == null) {
            synchronized (BeanApp.class) {
                if (instance == null) {
                    instance = new BeanApp();
                }
            }
        }
        return instance;
    }
}
