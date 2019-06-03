package container.container.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 19:23
 */
public class ConfigApp {
    private String mongoHost;
    private String mongoConnectionsPerHost;
    private String dbName;
    private String logsDBName;
    private String configDBName;
    private String mongoUsername;
    private String mongoPassword;

    private String gridHost;
    private String girdConnectionsPerHost;
    private String gridDbName;
    private String gridUsername;
    private String gridPassword;

    private String upstreamPort;
    private String keystorePwd;
    private String keystorePath;
    private String keymanagerPwd;
    private String upstreamSslPort;
    private String upstreamWsPort;

    private String ipPrefix;
    private String ethPrefix;
    private String serverType;
    private String internalKey;
    private String rpcPort;
    private String sslRpcPort;
    private String publicDomain;
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;
    private String localPath;
    private String remotePath;
    private String runtimeBootClass;
    private String serverPort;
    private String maxUsers;
    private String dockerRpcPort;
    private String dockerSslRpcPort;
    private String hotDeployment;

    ConfigApp(){
        InputStream inStream = ConfigApp.class.getClassLoader().getResourceAsStream("container.properties");
        InputStream appInStream = ConfigApp.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        Properties apppProp = new Properties();
        try {
            prop.load(inStream);
            mongoHost = prop.getProperty("database.host");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            dbName = prop.getProperty("dockerstatus.dbname");
            logsDBName = prop.getProperty("logs.dbname");
            configDBName = prop.getProperty("config.dbname");
            mongoUsername = prop.getProperty("mongo.username");
            mongoPassword = prop.getProperty("mongo.password");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            gridHost = prop.getProperty("gridfs.host");
            girdConnectionsPerHost = prop.getProperty("gridfs.connectionsPerHost");
            gridDbName = prop.getProperty("gridfs.files.dbname");
            gridUsername = prop.getProperty("gridfs.username");
            gridPassword = prop.getProperty("gridfs.password");
            ipPrefix = prop.getProperty("server.ip.prefix");
            ethPrefix = prop.getProperty("server.eth.prefix");
            serverType = prop.getProperty("server.type");
            internalKey = prop.getProperty("internal.key");
            rpcPort = prop.getProperty("rpc.port");
            sslRpcPort = prop.getProperty("rpc.sslport");
            publicDomain = prop.getProperty("public.domain");
            rpcSslClientTrustJksPath = prop.getProperty("rpc.ssl.clientTrust.jks.path");
            rpcSslServerJksPath = prop.getProperty("rpc.ssl.server.jks.path");
            rpcSslJksPwd = prop.getProperty("rpc.ssl.jks.pwd");
            localPath = prop.getProperty("script.local.path");
            remotePath = prop.getProperty("script.remote.path");
            runtimeBootClass = prop.getProperty("runtimeBootClass");
            upstreamPort = prop.getProperty("upstream-port");
            keystorePwd = prop.getProperty("keystore.pwd");
            keystorePath = prop.getProperty("keystore.path");
            keymanagerPwd = prop.getProperty("keymanager.pwd");
            upstreamSslPort = prop.getProperty("upstream-ssl-port");
            upstreamWsPort = prop.getProperty("upstream-ws-port");
            maxUsers = prop.getProperty("server.max.users");
            dockerRpcPort = prop.getProperty("docker.rpc.port");
            hotDeployment = prop.getProperty("hotDeployment");
            dockerSslRpcPort = prop.getProperty("docker.rpc.sslport");
            apppProp.load(appInStream);
            serverPort = apppProp.getProperty("server.port");
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

    public String getHotDeployment() {
        return hotDeployment;
    }
}
