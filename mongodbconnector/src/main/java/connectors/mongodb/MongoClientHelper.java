package connectors.mongodb;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


public class MongoClientHelper {
	private static final String TAG = MongoClientHelper.class.getSimpleName();

	private String hosts;//"mongodb://localhost:27017,localhost:27018,localhost:27019"
	
//	private static MongoClientHelper instance;
	
	private static int[] lock = new int[0];
	
//	private MongoClient mongoClient;
	private Integer connectionsPerHost;
	private Integer threadsAllowedToBlockForConnectionMultiplier;
	private Integer maxWaitTime;
	private Integer connectTimeout;
	private Integer socketTimeout;
	private Boolean socketKeepAlive;
	private ConcurrentHashMap<String, MongoClient> clientMap = new ConcurrentHashMap<>();

	public MongoClientHelper() {
//		instance = this;
	}
	
//	public static MongoClientHelper getInstance() {
//		return instance;
//	}
	
//	public void connect() throws CoreException {
//		connect(null);
//	}
	
	private MongoClient connect(String dbName) throws CoreException {
//			if(toHosts == null)
//				toHosts = hosts;
		MongoClient mongoClient = clientMap.get(dbName);
		if(mongoClient == null) {
			synchronized (clientMap) {
				mongoClient = clientMap.get(dbName);
				if(mongoClient == null) {
					LoggerEx.info(TAG, "Connecting hosts " + hosts);
					try {
						MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
						if(connectionsPerHost != null)
							optionsBuilder.connectionsPerHost(connectionsPerHost);
						if(threadsAllowedToBlockForConnectionMultiplier != null)
							optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
						if(maxWaitTime != null)
							optionsBuilder.maxWaitTime(maxWaitTime);
						if(connectTimeout != null)
							optionsBuilder.connectTimeout(connectTimeout);
						if(socketTimeout != null)
							optionsBuilder.socketTimeout(socketTimeout);
						if(socketKeepAlive != null)
							optionsBuilder.socketKeepAlive(socketKeepAlive);
//					CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new CleanDocumentCodec()));
//					optionsBuilder.codecRegistry(registry);

//							if(mongoClient != null) {
//								mongoClient.close();
//								LoggerEx.info(TAG, "Connected hosts " + hosts + " closing old hosts client " + hosts + " now.");
//							}

						MongoClientURI connectionString = new MongoClientURI(hosts + "/" + dbName, optionsBuilder);
						mongoClient = new MongoClient(connectionString);
						clientMap.put(dbName, mongoClient);

						LoggerEx.info(TAG, "Connected hosts " + hosts + " db " + dbName);
					} catch (Throwable t) {
						t.printStackTrace();
						LoggerEx.fatal(TAG, "Build mongo uri for hosts " + hosts + " failed, " + ExceptionUtils.getFullStackTrace(t));
					}
				}
			}
		}
		return mongoClient;
	}
	
	public void disconnect() {
		synchronized (lock) {
			if(hosts != null) {
				Collection<MongoClient> clients = clientMap.values();
				for(MongoClient mongoClient : clients) {
					if(mongoClient != null) {
						mongoClient.close();
					}
				}
				hosts = null;
			}
		}
	}

	public MongoDatabase getMongoDatabase(String databaseName) {
		MongoClient client = clientMap.get(databaseName);
		if(client == null) {
			try {
				connect(databaseName);
			} catch (CoreException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "connect database " + databaseName + " failed, " + ExceptionUtils.getFullStackTrace(e));
			}
		}
		client = clientMap.get(databaseName);
		if(client != null) {
			return client.getDatabase(databaseName);
		}
		return null;
	}

	public ClientSession startSession(String databaseName) {
		MongoClient client = clientMap.get(databaseName);
		if(client != null) {
			return client.startSession();
		}
		return null;
	}

	public ClientSession startSession(String databaseName, ClientSessionOptions options) {
		MongoClient client = clientMap.get(databaseName);
		if(client != null) {
			return client.startSession(options);
		}
		return null;
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
