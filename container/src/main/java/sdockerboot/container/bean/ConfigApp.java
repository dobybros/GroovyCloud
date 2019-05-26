package sdockerboot.container.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 19:23
 */
@Configuration
@PropertySource({"classpath:container.properties", "classpath:application.properties"})
public class ConfigApp {
    @Value("${database.host}")
    private String mongoHost;
    @Value("${connectionsPerHost}")
    private String mongoConnectionsPerHost;
    @Value("${dockerstatus.dbname}")
    private String dbName;
    @Value("${logs.dbname}")
    private String logsDBName;
    @Value("${config.dbname}")
    private String configDBName;
    @Value("${mongo.username}")
    private String mongoUsername;
    @Value("${mongo.password}")
    private String mongoPassword;

    @Value("${gridfs.host}")
    private String gridHost;
    @Value("${gridfs.connectionsPerHost}")
    private String girdConnectionsPerHost;
    @Value("${gridfs.files.dbname}")
    private String gridDbName;
    @Value("${gridfs.username}")
    private String gridUsername;
    @Value("${gridfs.password}")
    private String gridPassword;

    @Value("${upstream-port}")
    private String upstreamPort;
    @Value("${keystore.pwd}")
    private String keystorePwd;
    @Value("${keystore.path}")
    private String keystorePath;
    @Value("${keymanager.pwd}")
    private String keymanagerPwd;
    @Value("${upstream-ssl-port}")
    private String upstreamSslPort;
    @Value("${upstream-ws-port}")
    private String upstreamWsPort;

    @Value("${server.ip.prefix}")
    private String ipPrefix;
    @Value("${server.eth.prefix}")
    private String ethPrefix;
    @Value("${server.type}")
    private String serverType;
    @Value("${internal.key}")
    private String internalKey;
    @Value("${rpc.port}")
    private String rpcPort;
    @Value("${rpc.sslport}")
    private String sslRpcPort;
    @Value("${public.domain}")
    private String publicDomain;
    @Value("${rpc.ssl.clientTrust.jks.path}")
    private String rpcSslClientTrustJksPath;
    @Value("${rpc.ssl.server.jks.path}")
    private String rpcSslServerJksPath;
    @Value("${rpc.ssl.jks.pwd}")
    private String rpcSslJksPwd;
    @Value("${script.local.path}")
    private String localPath;
    @Value("${script.remote.path}")
    private String remotePath;
    @Value("${runtimeBootClass}")
    private String runtimeBootClass;
    @Value("${database.host}")
    private String serverPort;
    @Value("${server.max.users}")
    private String maxUsers;
    @Value("${docker.rpc.port}")
    private String dockerRpcPort;
    @Value("${docker.rpc.sslport}")
    private String dockerSslRpcPort;

    public String getMongoHost() {
        return mongoHost;
    }

    public String getMongoConnectionsPerHost() {
        return mongoConnectionsPerHost;
    }

    public String getDbName() {
        return dbName;
    }

    public String getLogsDBName() {
        return logsDBName;
    }

    public String getConfigDBName() {
        return configDBName;
    }

    public String getMongoUsername() {
        return mongoUsername;
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public String getGridHost() {
        return gridHost;
    }

    public String getGirdConnectionsPerHost() {
        return girdConnectionsPerHost;
    }

    public String getGridDbName() {
        return gridDbName;
    }

    public String getGridUsername() {
        return gridUsername;
    }

    public String getGridPassword() {
        return gridPassword;
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

    public String getIpPrefix() {
        return ipPrefix;
    }

    public String getEthPrefix() {
        return ethPrefix;
    }

    public String getServerType() {
        return serverType;
    }

    public String getInternalKey() {
        return internalKey;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public String getSslRpcPort() {
        return sslRpcPort;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public String getRpcSslClientTrustJksPath() {
        return rpcSslClientTrustJksPath;
    }

    public String getRpcSslServerJksPath() {
        return rpcSslServerJksPath;
    }

    public String getRpcSslJksPwd() {
        return rpcSslJksPwd;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public String getRuntimeBootClass() {
        return runtimeBootClass;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getMaxUsers() {
        return maxUsers;
    }

    public String getDockerRpcPort() {
        return dockerRpcPort;
    }

    public String getDockerSslRpcPort() {
        return dockerSslRpcPort;
    }
}
