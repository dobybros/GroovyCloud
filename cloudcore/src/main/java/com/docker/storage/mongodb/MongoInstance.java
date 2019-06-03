package com.docker.storage.mongodb;

import chat.logs.LoggerEx;
import com.docker.storage.DBException;
import com.mongodb.*;
import org.apache.commons.lang.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import script.utils.ShutdownListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoInstance implements ShutdownListener {
	private static final String TAG = LoggerEx.getClassTag(MongoInstance.class);

	private MongoClient mongo;
	private String host;
    private MongoClientOptions.Builder optionsBuilder;
	private Integer connectionsPerHost;
	private Integer threadsAllowedToBlockForConnectionMultiplier;
	private Integer maxWaitTime;
	private Integer connectTimeout;
	private Integer socketTimeout;
	private Boolean socketKeepAlive;
	
	private String username;
	private String password;
	
	
	public synchronized void disconnect() {
		mongo.close();
	}
	
	
	public synchronized void connect(String dbName) throws DBException {
		if(host == null){
			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "Initiate database miss arguments, host = " + host);
		}
		if(mongo == null){
			try {
				initMongoOptions();
				
				final String MONGODB_PROTOCOL = "mongodb://";
				if(host.startsWith(MONGODB_PROTOCOL)) {
					MongoClientURI connectionString = new MongoClientURI(host + "/" + dbName, optionsBuilder);
					mongo = new MongoClient(connectionString);
					return;
				}
				MongoClientOptions options = optionsBuilder.build();
//					host = host.substring(MONGODB_PROTOCOL.length());

				List<ServerAddress> servers = new ArrayList<>();
				String[] hostArray = host.split(",");
				for(String hostString : hostArray) {
                    ServerAddress address = new ServerAddress(hostString);
                    servers.add(address);
				}
				
				String key = host + ";" + options.toString();
				if(!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
					LoggerEx.info(TAG, "New Mongo instance created, " + key + " with username " + username + " password " + password);
					List<MongoCredential> lstCredentials =
                            Arrays.asList(MongoCredential.createCredential(
                                    username, dbName, password.toCharArray()));
					
					mongo = new MongoClient(servers, lstCredentials, options);
				} else {
					mongo = new MongoClient(servers, options);
					LoggerEx.info(TAG, "New Mongo instance created, " + key);
				}
				
			} catch (MongoException e) {
				e.printStackTrace();
				throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "MongoException 创建数据库对象失败", e.getMessage());
			}
		} else {
//			throw new DBException("数据库已经连接上， 不能重复连接");
		}
//		initDAOMap();
	}
	
	private void initMongoOptions(){
		optionsBuilder = MongoClientOptions.builder();
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
		CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new CleanDocumentCodec()));
		optionsBuilder.codecRegistry(registry);
	}

    public MongoClient getMongo() {
        return mongo;
	}
	public void setMongo(MongoClient mongo) {
		this.mongo = mongo;
	}
//	public MongoClientOptions getOptions() {
//		return options;
//	}
//	public void setOptions(MongoClientOptions options) {
//		this.options = options;
//	}
	
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
	public String getHost() {
		return host;
	}
	
	public void setHost(String hosts) {
		this.host = hosts;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void shutdown() {
		disconnect();
	}
}
