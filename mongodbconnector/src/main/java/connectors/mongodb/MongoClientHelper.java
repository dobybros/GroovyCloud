package connectors.mongodb;

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;


public class MongoClientHelper {
    private static final String TAG = MongoClientHelper.class.getSimpleName();

    private String hosts;//"mongodb://192.168.80.101:27017,192.168.80.101:27018,192.168.80.101:27019,localhost:27018,localhost:27019"

//	private static MongoClientHelper instance;

    private static int[] lock = new int[0];

    //	private MongoClient mongoClient;
    private MongoClientOptions.Builder optionsBuilder;
    private Integer connectionsPerHost;
    private Integer threadsAllowedToBlockForConnectionMultiplier;
    private Integer maxWaitTime;
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Boolean socketKeepAlive;

    public MongoClientHelper() {
//		instance = this;
    }

//	public static MongoClientHelper getInstance() {
//		return instance;
//	}

//	public void connect() throws CoreException {
//		connect(null);
//	}

    //    private MongoClient connect(String dbName) throws CoreException {
////			if(toHosts == null)
////				toHosts = hosts;
//        MongoClient mongoClient = clientMap.get(dbName);
//        if (mongoClient == null) {
//            synchronized (clientMap) {
//                mongoClient = clientMap.get(dbName);
//                if (mongoClient == null) {
//                    LoggerEx.info(TAG, "Connecting hosts " + hosts);
//                    try {
//                        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
//                        if (connectionsPerHost != null)
//                            optionsBuilder.connectionsPerHost(connectionsPerHost);
//                        if (threadsAllowedToBlockForConnectionMultiplier != null)
//                            optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
//                        if (maxWaitTime != null)
//                            optionsBuilder.maxWaitTime(maxWaitTime);
//                        if (connectTimeout != null)
//                            optionsBuilder.connectTimeout(connectTimeout);
//                        if (socketTimeout != null)
//                            optionsBuilder.socketTimeout(socketTimeout);
//                        if (socketKeepAlive != null)
//                            optionsBuilder.socketKeepAlive(socketKeepAlive);
////					CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new CleanDocumentCodec()));
////					optionsBuilder.codecRegistry(registry);
//
////							if(mongoClient != null) {
////								mongoClient.close();
////								LoggerEx.info(TAG, "Connected hosts " + hosts + " closing old hosts client " + hosts + " now.");
////							}
//                        String[] accountInfo = hosts.split("@");
//                        String hostsStr = accountInfo[0];
//                        String account = null;
//                        String passwd = null;
//                        if (accountInfo.length > 1) {
//                            String[] accountInfoStrArray = accountInfo[1].replaceAll(" ", "").split(":");
//                            account = accountInfoStrArray[0];
//                            passwd = accountInfoStrArray[1];
//                        }
//                        List<ServerAddress> serverAddresses = getServerAddresses(hostsStr);
//                        if (!serverAddresses.isEmpty()) {
//                            if (StringUtils.isNotBlank(account) && StringUtils.isNotBlank(passwd)) {
//                                mongoClient = new MongoClient(serverAddresses, MongoCredential.createCredential(account, "admin", passwd.toCharArray()), optionsBuilder.build());
//                            } else {
//								mongoClient = new MongoClient(serverAddresses, optionsBuilder.build());
//                            }
////                            MongoClientURI connectionString = new MongoClientURI(hosts + "/" + dbName, optionsBuilder);
////                            mongoClient = new MongoClient(connectionString);
//                            clientMap.put(dbName, mongoClient);
//
//                            LoggerEx.info(TAG, "Connected hosts " + hosts + " db " + dbName);
//                        }
//                    } catch (Throwable t) {
//                        t.printStackTrace();
//                        LoggerEx.fatal(TAG, "Build mongo uri for hosts " + hosts + " failed, " + ExceptionUtils.getFullStackTrace(t));
//                    }
//                }
//            }
//        }
//        return mongoClient;
//    }
//
//    public void disconnect() {
//        synchronized (lock) {
//            if (hosts != null) {
//                Collection<MongoClient> clients = clientMap.values();
//                for (MongoClient mongoClient : clients) {
//                    if (mongoClient != null) {
//                        mongoClient.close();
//                    }
//                }
//                hosts = null;
//            }
//        }
//    }
    private void initMongoOptions() {
        if (optionsBuilder == null) {
            optionsBuilder = MongoClientOptions.builder();
            if (connectionsPerHost != null)
                optionsBuilder.connectionsPerHost(connectionsPerHost);
            if (threadsAllowedToBlockForConnectionMultiplier != null)
                optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
            if (maxWaitTime != null)
                optionsBuilder.maxWaitTime(maxWaitTime);
            if (connectTimeout != null)
                optionsBuilder.connectTimeout(connectTimeout);
            if (socketTimeout != null)
                optionsBuilder.socketTimeout(socketTimeout);
            if (socketKeepAlive != null)
                optionsBuilder.socketKeepAlive(socketKeepAlive);
            try {
                Class<?> cleanDocumentCodecClass = Class.forName("com.docker.storage.mongodb.CleanDocumentCodec");
                CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs((Codec<?>) cleanDocumentCodecClass.getDeclaredConstructor().newInstance()));
                optionsBuilder.codecRegistry(registry);
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
    }

    public MongoDatabase getMongoDatabase(String databaseName) {
        return getMongo().getDatabase(databaseName);
    }

    public ClientSession startSession() {
        return getMongo().startSession();
    }

    public ClientSession startSession(ClientSessionOptions options) {
        return getMongo().startSession(options);
    }
    private MongoClient getMongo(){
        initMongoOptions();
        return MongoClientFactory.getInstance().getClient(hosts, optionsBuilder);
    }
    public String getHosts() {
        return hosts;
    }

    public Integer getConnectionsPerHost() {
        return connectionsPerHost;
    }

    public void setConnectionsPerHost(Integer connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
        return threadsAllowedToBlockForConnectionMultiplier;
    }

    public void setThreadsAllowedToBlockForConnectionMultiplier(
            Integer threadsAllowedToBlockForConnectionMultiplier) {
        this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    public Integer getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Integer maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Boolean getSocketKeepAlive() {
        return socketKeepAlive;
    }

    public void setSocketKeepAlive(Boolean socketKeepAlive) {
        this.socketKeepAlive = socketKeepAlive;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }
}
