package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.props.GlobalLansProperties;
import com.docker.data.Lan;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.LansService;
import com.docker.utils.SpringContextUtil;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
	private static final String TAG = StorageManager.class.getSimpleName();
    private GlobalLansProperties globalLansProperties;
	private Properties storageProperties;
    private Properties sdockerProperties;
	private static StorageManager instance;
	private OnlineServer onlineServer = (OnlineServer) SpringContextUtil.getBean("onlineServer");
    private LansService lansService = (LansService) SpringContextUtil.getBean("lansService");
//	private RMIServerImpl rpcServer = (RMIServerImpl) SpringContextUtil.getBean("rpcServer");

    private ConcurrentHashMap<String, ServiceStubManager> stubManagerForLanIdMap = new ConcurrentHashMap<>();

    // 跨区的adapter，比较特殊
    private ConcurrentHashMap<Class, StorageAdapter> acrossAdaptorMap = new ConcurrentHashMap<>();

	public static StorageManager getInstance() {
		if(instance == null) {
			synchronized (StorageManager.class) {
				if(instance == null)
					instance = new StorageManager();
			}
		}
		return instance;
	}
	
	public StorageManager() {
		ClassPathResource resource = new ClassPathResource(onlineServer.getConfigPath());
		storageProperties = new Properties();
		try {
			storageProperties.load(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Prepare storage.properties is failed, " + e.getMessage());
		}
		ClassPathResource sdockerResource = new ClassPathResource("container.properties");
        sdockerProperties = new Properties();
        try {
            sdockerProperties.load(sdockerResource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Prepare properties is failed, " + e.getMessage());
        }

        globalLansProperties = (GlobalLansProperties) SpringContextUtil.getBean("globalLansProperties");
    }
	
	public <T extends StorageAdapter> T getStorageAdapter(Class<T> adapterClass) {
		return getStorageAdapter(adapterClass, onlineServer != null ? onlineServer.getLanId() : null);
	}
	@SuppressWarnings("unchecked")
	public <T extends StorageAdapter> T getStorageAdapter(Class<T> adapterClass, String lanId) {

        String currentLanId = OnlineServer.getInstance().getLanId();
        String className = adapterClass.getName();
        String serviceName = sdockerProperties.getProperty(className);
        if (serviceName == null) {
            LoggerEx.warn(TAG, "service not exist, class:" + adapterClass);
            return null;
        } else {
            String globalLanId = storageProperties.getProperty(className);

            if (globalLanId != null) {
                lanId = globalLanId;
            }
            if (lanId == null) {
                lanId = currentLanId;
            }
            ServiceStubManager manager = stubManagerForLanIdMap.get(lanId);
            if (manager == null) {
                synchronized (stubManagerForLanIdMap) {
                    manager = stubManagerForLanIdMap.get(lanId);
                    if(manager == null) {
                        manager = new ServiceStubManager();
                        if (!lanId.equals(currentLanId)) {
                            manager.setUsePublicDomain(true);
                            manager.setClientTrustJksPath(onlineServer.getRpcSslClientTrustJksPath());
                            manager.setServerJksPath(onlineServer.getRpcSslServerJksPath());
                            manager.setJksPwd(onlineServer.getRpcSslJksPwd());
                        }
                        Lan dblan = null;
                        try {
                            dblan = lansService.getLan(lanId);
                        } catch (CoreException e) {
                            e.printStackTrace();
                            LoggerEx.error(TAG, "Read lan " + lanId + " information failed, " + e.getMessage() + " try to read globallan.properties instead.");
                        }
                        if(dblan == null || dblan.getDomain() == null || dblan.getPort() == null || dblan.getProtocol() == null) {
                            Map<String, GlobalLansProperties.Lan> lanMap = globalLansProperties.getLanMap();
                            if (lanMap != null) {
                                GlobalLansProperties.Lan lan = lanMap.get(lanId);
                                if (lan != null) {
                                    manager.setHost(lan.getHost());
                                } else {
                                    LoggerEx.warn(TAG, "lan not exist for lanId: " + lanId);
                                    return null;
                                }
                            } else {
                                LoggerEx.warn(TAG, "lanMap is null");
                                return null;
                            }
                        } else {
                            manager.setHost(dblan.getProtocol() + "://" + dblan.getDomain() + ":" + dblan.getPort());
                        }
                        manager.init();
                        stubManagerForLanIdMap.putIfAbsent(lanId, manager);
                        manager = stubManagerForLanIdMap.get(lanId);
                    }
                }
            }
            return manager.getService(serviceName, adapterClass);
        }
	}
	
	Properties getStorageProperties() {
		return storageProperties;
	}
}
