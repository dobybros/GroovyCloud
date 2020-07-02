package container.container.bean;

import com.docker.storage.kafka.BaseKafkaConfCenter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 19:23
 */
public class ConfigApp {
    private static final String TAG = ConfigApp.class.getSimpleName();
    private String mongoHost;
    private String mongoConnectionsPerHost;
    private String dbName;
    private String logsDBName;
    private String configDBName;
    private String mongoUsername;
    private String mongoPassword;
    private String redisHost;

    private String gridHost;
    private String girdConnectionsPerHost;
    private String gridDbName;
    private String gridUsername;
    private String gridPassword;

    private String ipPrefix;
    private String ethPrefix;
    private String type;
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
    private String hotDeployment;
    private String killProcess;
    private String fileBucket;
    private String dockerName;
    private String scaleInstanceId;

    public ConfigApp(){
        InputStream inStream = ConfigApp.class.getClassLoader().getResourceAsStream("groovycloud.properties");
        InputStream appInStream = ConfigApp.class.getClassLoader().getResourceAsStream("application.properties");
        InputStream kafkaProducerInStream = ConfigApp.class.getClassLoader().getResourceAsStream("config/kafka/producer.properties");
        Properties prop = new Properties();
        Properties appProp = new Properties();
        Properties kafkaProducerProp = new Properties();
        try {
            prop.load(inStream);
            mongoHost = prop.getProperty("database.host");
            mongoConnectionsPerHost = prop.getProperty("connectionsPerHost");
            dbName = prop.getProperty("dockerstatus.dbname");
            logsDBName = prop.getProperty("logs.dbname");
            configDBName = prop.getProperty("config.dbname");
            mongoUsername = prop.getProperty("mongo.username");
            mongoPassword = prop.getProperty("mongo.password");
            gridHost = prop.getProperty("gridfs.host");
            girdConnectionsPerHost = prop.getProperty("gridfs.connectionsPerHost");
            gridDbName = prop.getProperty("gridfs.files.dbname");
            gridUsername = prop.getProperty("gridfs.username");
            gridPassword = prop.getProperty("gridfs.password");

            ipPrefix = prop.getProperty("server.ip.prefix");
            ethPrefix = prop.getProperty("server.eth.prefix");
            type = prop.getProperty("type");
            serverType = prop.getProperty("server.type");
            internalKey = prop.getProperty("internal.key");
            rpcPort = prop.getProperty("rpc.port");
            sslRpcPort = prop.getProperty("rpc.sslport");
            fileBucket = prop.getProperty("gridfs.bucket");
            publicDomain = prop.getProperty("public.domain");
            rpcSslClientTrustJksPath = prop.getProperty("rpc.ssl.clientTrust.jks.path");
            rpcSslServerJksPath = prop.getProperty("rpc.ssl.server.jks.path");
            rpcSslJksPwd = prop.getProperty("rpc.ssl.jks.pwd");
            localPath = prop.getProperty("script.local.path");
            remotePath = prop.getProperty("script.remote.path");
            runtimeBootClass = prop.getProperty("runtimeBootClass");
            maxUsers = prop.getProperty("server.max.users");
            hotDeployment = prop.getProperty("hotDeployment");
            killProcess = prop.getProperty("killProcess");
            dockerName = prop.getProperty("docker.name");
            scaleInstanceId = prop.getProperty("scale.instanceId");
            redisHost = prop.getProperty("db.redis.uri");
            appProp.load(appInStream);
            serverPort = appProp.getProperty("server.port");
            if(kafkaProducerInStream != null){
                kafkaProducerProp.load(kafkaProducerInStream);
                BaseKafkaConfCenter.getInstance().setKafkaConfCenter(kafkaProducerProp, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                appInStream.close();
                if(kafkaProducerInStream != null){
                    kafkaProducerInStream.close();
                }
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


    public String getHotDeployment() {
        return hotDeployment;
    }

    public String getFileBucket() {
        return fileBucket;
    }

    public String getDockerName() {
        return dockerName;
    }

    public String getScaleInstanceId() {
        return scaleInstanceId;
    }

    public String getKillProcess() {
        return killProcess;
    }

    public String getType() {
        return type;
    }

    public String getRedisHost() {
        return redisHost;
    }
}

