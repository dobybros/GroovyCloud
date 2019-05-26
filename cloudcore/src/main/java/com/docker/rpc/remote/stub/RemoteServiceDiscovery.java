package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RemoteServiceDiscovery implements Runnable {
	private static final String TAG = RemoteServiceDiscovery.class.getSimpleName();
	private CloseableHttpClient httpClient = HttpClients.createDefault();;
    private RPCClientAdapterMap rpcClientAdapterMap;
    private Random random = new Random();
	/**
	 * 获取的时间
	 */
	private Long time;
	/**
	 * 重新获取Lan列表的超时时间。
	 */
	private long expireTime = TimeUnit.SECONDS.toMillis(10);

	private boolean isStarted = true;

	private Object lock = new Object();

	public String toString() {
		return super.toString();
	}

    private RemoteServers remoteServers;

	private String host;

	private String service;

	private String serviceName;
	private Integer version;

	private long touch;

	private long expireShutdownTime = TimeUnit.MINUTES.toMillis(15);

	private ShutdownListener shutdownListener;

    private Boolean usePublicDomain = false;

	public RemoteServiceDiscovery() {
	}

	public void touch() {
	    touch = System.currentTimeMillis();
    }

    public static interface ShutdownListener {
	    int TYPE_EXPIRED = 1;
	    int TYPE_MANUAL = 2;
	    int TYPE_SERVER_SHUTDOWN = 3;
	    public void shutdownNow();
    }

    public void shutdown() {
	    shutdown(ShutdownListener.TYPE_MANUAL);
    }

	public void shutdown(int type) {
		LoggerEx.info(TAG, RemoteServiceDiscovery.class.getSimpleName() + " is shutting down");
		if(isStarted)
			isStarted = false;
		synchronized (lock) {
			lock.notify();
		}
        try {
		    if(shutdownListener != null)
		        shutdownListener.shutdownNow();
        } catch(Throwable t) {
		    t.printStackTrace();
            LoggerEx.error(TAG, "Shutdown callback " + shutdownListener + " failed, " + t.getMessage());
        }
	}

	public void updateNow() {
        synchronized (lock) {
            lock.notify();
        }
	}

    public void setHost(String host) {
        if(host.startsWith("http")) {
            this.host = host;
        } else {
            this.host = "http://" + host;
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setRpcClientAdapterMap(RPCClientAdapterMap rpcClientAdapterMap) {
        this.rpcClientAdapterMap = rpcClientAdapterMap;
    }

	public static class ServersResult extends Result<List<Server>> {

    }

    public RemoteServers getRemoteServers() {
	    return remoteServers;
    }

	public void update() {
        try {
            if(remoteServers == null) {
                remoteServers = new RemoteServers();
            }

            if(version == null)
                version = 1;

            ServersResult result = (ServersResult) post(host + "/rest/discovery/service/" + serviceName + "/version/" + version, ServersResult.class);
            if(result != null) {
                List<Server> theServers = result.getData();
                if(theServers != null) {
                    ConcurrentHashMap<String, Server> servers = remoteServers.getServers();

                    HashSet<String> activeServers = new HashSet<>();
                    for(Server serverElement : theServers) {
//                        JsonObjectEx serverObj = new JsonObjectEx(serverElement.getAsJsonObject());

                        String serverName = serverElement.getServer();
                        if(serverName == null)
                            continue;
                        activeServers.add(serverName);

                        Server server = servers.get(serverName);
                        boolean newOnline = false;
                        if(server == null) {
                            newOnline = true;
                            server = serverElement;
                            servers.putIfAbsent(serverName, server);
                            if(newOnline)
                                LoggerEx.info(TAG, "New server " + server + " is online ");
                        } else {
                            server.setHttpPort(serverElement.getHttpPort());
                            server.setHealth(serverElement.getHealth());
                            server.setIp(serverElement.getIp());
                            server.setLanId(serverElement.getLanId());
                            server.setRpcPort(serverElement.getRpcPort());
                            server.setSslRpcPort(serverElement.getSslRpcPort());
                            server.setPublicDomain(serverElement.getPublicDomain());
                            server.setVersion(serverElement.getVersion());
                            server.setMinVersion(serverElement.getMinVersion());
                        }
//                                server.setServer(serverObj.getString("server"));
                    }
//                    LoggerEx.info(TAG, "The service: " + service + ", get all new servers: " + JSON.toJSONString(servers) + ", now time: " + System.currentTimeMillis());
                    Collection<String> keys = servers.keySet();
                    for (String key : keys) {
                        if(!activeServers.contains(key)) {
                            Server deletedServer = servers.remove(key);
                            rpcClientAdapterMap.unregisterServer(key);
//                            RPCClientAdapter clientAdapter = rpcClientAdapterMap.getClientAdapter(key);
//                            if(clientAdapter != null) {
//                                clientAdapter.clientDestroy();
//                            }
                            LoggerEx.info(TAG, "Server " + deletedServer + " is offline");
                        }
                    }
                }
            }
        } catch (CoreException e) {
//            e.printStackTrace();
            LoggerEx.error(TAG, "Get remote servers for service " + service + " on host " + host + " failed, " + e.getMessage());
        } finally {
            if(remoteServers != null) {
                remoteServers.calculate();
            }
        }
	}

    public class RemoteServers {
//        private Comparator comparator = new Comparator<Server>() {
//            @Override
//            public int compare(Server o1, Server o2) {
//                int o2Scrore = o2.getScore();
//                int o1Scrore = o1.getScore();
//
//                if (o2Scrore == o1Scrore)
//                    return 0;
//                else if (o2Scrore > o1Scrore)
//                    return 1;
//                else
//                    return -1;
//            }
//        };

        private ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
        private List<Server> sortedServers = new ArrayList<>();

        public RemoteServers() {
        }
        private final long PERIOD = 30000;

        public void calculate() {
            List<Server> newSortedServers = new ArrayList<>();
            Collection<Server> theServers = servers.values();
            for(Server server : theServers) {
                int score = 100;
                RPCClientAdapter clientAdapter = rpcClientAdapterMap.getClientAdapter(server.getServer());
                if(clientAdapter != null) {
//                    score += 10;
                    if(clientAdapter.isConnected()) {
//                        Integer average = clientAdapter.getAverageLatency();
//                        if(average != null)
//                            score += (500 - average);
//                        if(score < 0)
//                            score = 0;
                    } else {
                        score = -1;
                        continue;
                    }
                }
                server.setScore(score);
                newSortedServers.add(server);
            }
            sortedServers = newSortedServers;
        }

        public void stop() {

        }

        public ConcurrentHashMap<String, Server> getServers() {
            return servers;
        }

        public Server getServer(String server) {
            return servers.get(server);
        }

        public List<Server> getSortedServers() {
            return sortedServers;
        }

        public class RandomDraw {
            private int[] array;
            public RandomDraw(int count) {
                array = new int[count];
                for(int i = 0; i < count; i++)
                    array[i] = i;
            }

            public int next() {
                if(array.length <= 0)
                    return -1;
                int index = random.nextInt(array.length);
                int value = array[index];
                int[] newArray = new int[array.length - 1];
                if(index == 0) {
                    System.arraycopy(array, 1, newArray, 0, newArray.length);
                } else if(index == array.length - 1) {
                    System.arraycopy(array, 0, newArray, 0, newArray.length);
                } else {
                    System.arraycopy(array, 0, newArray, 0, index);
                    System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
                }
                array = newArray;
                return value;
            }
        }

        public MethodResponse call(MethodRequest request) throws CoreException {
            touch();
            if(sortedServers.isEmpty())
                throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "No server is found for service " + service + " fromService " + request.getFromService() + " crc " + request.getCrc());

            List<Server> keptSortedServers = sortedServers;
            int count = 0;
            int maxCount = 5;
            int size = keptSortedServers.size();
            maxCount = size < 5 ? size : 5;
            RandomDraw randomDraw = new RandomDraw(size);
            for(int i = 0; i < maxCount; i++) {
                int index = randomDraw.next();
                if(index == -1)
                    continue;
                Server server = keptSortedServers.get(index);
                if(server == null)
                    continue;
                if(count++ > maxCount)
                    break;
                try {
                    String ip = null;
                    if(usePublicDomain) {
                        ip = server.getPublicDomain();
                    } else {
                        ip = server.getIp();
                    }
                    Integer port = null;
                    if(rpcClientAdapterMap.isEnableSsl()) {
                        port = server.getSslRpcPort();
                    } else {
                        port = server.getRpcPort();
                    }
                    request.setService(serviceName + "_v" + server.version);
                    if(ip != null && port != null) {
                        RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(ip, port, server.getServer());
                        MethodResponse response = (MethodResponse) clientAdapter.call(request);
                        if(response.getException() != null) {
                            LoggerEx.info(TAG, "Failed to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                             throw response.getException();
                        }
                        LoggerEx.info(TAG, "Successfully call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                        return response;
                    } else {
                        LoggerEx.info(TAG, "No ip " + ip + " or port " + port + ", fail to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
                    }
                } catch(Throwable t) {
                    if(t instanceof CoreException) {
                        CoreException ce = (CoreException) t;
                        switch (ce.getCode()) {
                            case ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED:
                            case ChatErrorCodes.ERROR_RPC_DISCONNECTED:
                                break;
                            default:
                                throw t;
                        }
                    }
                    LoggerEx.error(TAG, "Fail to Call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount + " available size " + keptSortedServers.size() + " error " + t.getMessage() + " exception " + t);
                }
            }
            throw new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "Call request " + request + " outside failed with several retries.");
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(RemoteServers.class.getSimpleName() + ": ");
            builder.append("Servers " + Arrays.toString(servers.values().toArray())).append("; sorted servers " + Arrays.toString(sortedServers.toArray()));
            return builder.toString();
        }

    }

	public static class Server {
        /**
         * 6位服务器名称， 随机生成的， 在数据库中是唯一字段
         */
        private String server;

        /**
         * 这是网卡上能取到的IP， 一般是内网IP， 可以用于相同局域网的服务器间通信。
         */
        private String ip;
        /**
         * 通过msg.jar包装jetty提供http服务，
         * 这样的设计需要提供两个jar， 一个msg.jar提供元数据和接口， 以及使用classloader启动msgimpl.jar， 在msgimpl.jar里包含所有的依赖包， 例如jetty的。
         */
        private Integer httpPort;
        /**
         * 通过msg.jar提供的rpc端口
         */
        private Integer rpcPort;
        /*
         * ssl rpc port
         */
        private Integer sslRpcPort;
        /**
         * ip有可能对应的是内网IP， 如果这台服务器需要对外， 就需要通过IM的管理后台配置外网IP。
         */
        private String publicDomain;
        /**
         * 相同lanId的服务器可以通过ip直接通信， 说明他们在同一内网。
         * 不同lanId的服务器只能通过publicDomain访问， 说明他们不在同一内网， 可以是跨越国家或者大洲的不同部署。
         */
        private String lanId;

        private Integer version;
        private Integer minVersion;

        /**
         * 一台服务器的健康值， 0分是最健康的， 100分是最不健康的。 100分封顶。
         *
         * 这个值的计算需要综合各方面因素， 定期刷新到服务器中， 例如每10秒刷一次。
         */
        private Integer health;
        public static final int HEALTH_MAX = 100;

        private int score = 100;

        public static class Service {
            private String service;
            private Integer version;
            private Integer minVersion;

            public String getService() {
                return service;
            }

            public void setService(String service) {
                this.service = service;
            }

            public Integer getVersion() {
                return version;
            }

            public void setVersion(Integer version) {
                this.version = version;
            }

            public Integer getMinVersion() {
                return minVersion;
            }

            public void setMinVersion(Integer minVersion) {
                this.minVersion = minVersion;
            }
        }

        private List<Service> services;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(Integer httpPort) {
            this.httpPort = httpPort;
        }

        public Integer getRpcPort() {
            return rpcPort;
        }

        public void setRpcPort(Integer rpcPort) {
            this.rpcPort = rpcPort;
        }

        public Integer getSslRpcPort() {
            return sslRpcPort;
        }

        public void setSslRpcPort(Integer sslRpcPort) {
            this.sslRpcPort = sslRpcPort;
        }

        public String getPublicDomain() {
            return publicDomain;
        }

        public void setPublicDomain(String publicDomain) {
            this.publicDomain = publicDomain;
        }

        public String getLanId() {
            return lanId;
        }

        public void setLanId(String lanId) {
            this.lanId = lanId;
        }

        public Integer getHealth() {
            return health;
        }

        public void setHealth(Integer health) {
            this.health = health;
        }

        public List<Service> getServices() {
            return services;
        }

        public void setServices(List<Service> services) {
            this.services = services;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(Integer minVersion) {
            this.minVersion = minVersion;
        }
    }

	private Result post(String uri, Class<? extends Result> resultClass) throws CoreException {
		HttpPost post = new HttpPost(uri);
//		post.setHeader("key", OnlineServer.getInstance().getInternalKey());
//		if(data != null)
//			post.setEntity(new StringEntity(data.toString(), "utf8"));
		try {
//			post.setHeader(InternalServerKeyHolder.KEY_HEADER, OnlineServer.getInstance().getKey());
			HttpResponse response = httpClient.execute(post);
			int code = response.getStatusLine().getStatusCode();
			if(code == 200) {
				HttpEntity responseEntity = response.getEntity();
				String str = IOUtils.toString(responseEntity.getContent());
                ServersResult server = JSON.parseObject(str, ServersResult.class);
				if(server != null && server.getCode() == 1) {
					return server;
				} else {
					String description = server.getMsg();
					throw new CoreException(ChatErrorCodes.ERROR_POST_FAILED, "Connect to server failed, " + description);
				}
			} else {
				throw new CoreException(ChatErrorCodes.ERROR_POST_FAILED, "Connect to server http failed, " + code);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_POST_FAILED, "Connect to server occur error, " + e.getMessage());
		} finally {
			post.abort();
		}
	}

	@Override
	public void run() {
	    touch();
		while(isStarted) {
			try {
			    if(touch + expireShutdownTime < System.currentTimeMillis()) {
			        shutdown(ShutdownListener.TYPE_EXPIRED);
			        continue;
                }
                synchronized (lock) {
                    try {
                        lock.wait(expireTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                update();
            }catch(Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Update servers for service " + service + " failed, " + t.getMessage() + " will sleep a few seconds");
                synchronized (lock) {
                    try {
                        lock.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
			}
		}
		LoggerEx.info(TAG, RemoteServiceDiscovery.class.getSimpleName() + " " + service + " is shutted down");
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

    public ShutdownListener getShutdownListener() {
        return shutdownListener;
    }

    public void setShutdownListener(ShutdownListener shutdownListener) {
        this.shutdownListener = shutdownListener;
    }

    public Boolean getUsePublicDomain() {
        return usePublicDomain;
    }

    public void setUsePublicDomain(Boolean usePublicDomain) {
        this.usePublicDomain = usePublicDomain;
    }
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
