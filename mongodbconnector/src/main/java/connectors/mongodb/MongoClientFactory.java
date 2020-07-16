package connectors.mongodb;

import chat.logs.LoggerEx;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2020/7/16.
 * Description：
 */
public class MongoClientFactory {
    private final String TAG = MongoClientFactory.class.getSimpleName();
    public static final String MONGO_PREFIX = "mongodb://";
    private Map<String, MongoClient> clientMap = new ConcurrentHashMap<>();

    public MongoClient getClient(String host, MongoClientOptions.Builder builder) {
        String[] accountInfo = host.split("@");
        String hostsStr = accountInfo[0].replaceAll(" ", "");
        MongoClient mongoClient = clientMap.get(hostsStr);
        if (mongoClient == null) {
            String account = null;
            String passwd = null;
            if (accountInfo.length > 1) {
                String[] accountInfoStrArray = accountInfo[1].replaceAll(" ", "").split(":");
                account = accountInfoStrArray[0];
                passwd = accountInfoStrArray[1];
            }
            List<ServerAddress> serverAddresses = getServerAddresses(hostsStr);
            if (!serverAddresses.isEmpty()) {
                if (StringUtils.isNotBlank(account) && StringUtils.isNotBlank(passwd)) {
                    mongoClient = new MongoClient(serverAddresses, MongoCredential.createCredential(account, "admin", passwd.toCharArray()), builder.build());
                } else {
                    mongoClient = new MongoClient(serverAddresses, builder.build());
                }
                clientMap.putIfAbsent(hostsStr, mongoClient);
                LoggerEx.info(TAG, "Connected mongodb, hosts " + host + "");
            }
        }
        return clientMap.get(hostsStr);
    }
    public void releaseAllMongoClient(){
        for (MongoClient mongoClient : clientMap.values()){
            try {
                mongoClient.close();
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
        clientMap.clear();
    }
    private static volatile MongoClientFactory instance;

    public static MongoClientFactory getInstance() {
        if (instance == null) {
            synchronized (MongoClientFactory.class) {
                if (instance == null) {
                    instance = new MongoClientFactory();
                }
            }
        }
        return instance;
    }

    private List<ServerAddress> getServerAddresses(String hostsStr) {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        if (StringUtils.isNotBlank(hostsStr) && hostsStr.startsWith(MONGO_PREFIX)) {
            String[] hostsArray = hostsStr.split(MONGO_PREFIX)[1].split(",");
            for (int i = 0; i < hostsArray.length; i++) {
                String address = hostsArray[i];
                String[] ipPort = address.split(":");
                ServerAddress serverAddress = new ServerAddress(ipPort[0], Integer.parseInt(ipPort[1]));
                serverAddresses.add(serverAddress);
            }
        }
        if (serverAddresses.isEmpty()) {
            LoggerEx.fatal(TAG, "Connect mongo err, serverAddresses is empty, hosts: " + hostsStr);
        }
        return serverAddresses;
    }
}
