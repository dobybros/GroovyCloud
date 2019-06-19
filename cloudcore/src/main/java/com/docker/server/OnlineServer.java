package com.docker.server;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import chat.utils.IPHolder;
import com.docker.data.DockerStatus;
import com.docker.errors.CoreErrorCodes;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.storage.adapters.SDockersService;
import com.docker.tasks.Task;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class OnlineServer {
    private static final String TAG = OnlineServer.class.getSimpleName();
    private String server;

    private List<Task> tasks;
//    private List<ShutdownListener> shutdownList;

    private String internalKey;

    private Integer httpPort;
    private String serverType;

    private IPHolder ipHolder;
    private SDockersService sdockersService;

    private DockerStatusService dockerStatusService;

    private static OnlineServer instance;

    private DockerStatus dockerStatus;

    private OnlineServerStartHandler startHandler;

    private String sslRpcPort;
    private String rpcPort;
    private String wsPort;
    private String tcpPort;
    private String sslTcpPort;
    private Integer status;
    private String configPath;

    private String lanId;

    /**
     * rpc ssl certificate
     */
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;

    private Properties config;

    public static interface OnlineServerStartHandler {
        public void serverWillStart(OnlineServer onlineServer) throws CoreException;

        public void serverWillShutdown(OnlineServer onlineServer);
    }

    protected OnlineServer() {
        instance = this;
    }

    public static OnlineServer getInstance() {
        return instance;
    }

    public void prepare() {
    }

    public String getIp() {
        if(ipHolder != null)
            return ipHolder.getIp();
        return null;
    }

    protected DockerStatus generateDockerStatus(Integer port) {
        DockerStatus dockerStatus = new DockerStatus();
        dockerStatus.setServer(server);
        dockerStatus.setServerType(serverType);
        dockerStatus.setIp(ipHolder.getIp());
        if (rpcPort != null) {
            try {
                dockerStatus.setRpcPort(Integer.parseInt(rpcPort));
            } catch (Throwable t) {
            }
        }
        if (sslRpcPort != null) {
            try {
                dockerStatus.setSslRpcPort(Integer.parseInt(sslRpcPort));
            } catch (Throwable t) {
            }
        }
        dockerStatus.setHttpPort(port);
        dockerStatus.setLanId(lanId);
        dockerStatus.setHealth(0);
        dockerStatus.setSslRpcPort(Integer.valueOf(sslRpcPort));
        dockerStatus.setTcpPort(Integer.valueOf(tcpPort));
        dockerStatus.setWsPort(Integer.valueOf(wsPort));
        dockerStatus.setRpcPort(Integer.valueOf(rpcPort));
        if (status == null)
            status = DockerStatus.STATUS_OK;
        dockerStatus.setStatus(status);
        Map<String, Object> info = new HashMap<String, Object>();
        info.put(DockerStatus.FIELD_SERVERSTATUS_SSLRPCPORT, sslRpcPort);
        info.put(DockerStatus.FIELD_SERVERSTATUS_TCPPORT, tcpPort);
        info.put(DockerStatus.FIELD_SERVERSTATUS_WEBSOCKETPORT, wsPort);
        info.put(DockerStatus.FIELD_SERVERSTATUS_RPCPORT, rpcPort);
        dockerStatus.setInfo(info);
        return dockerStatus;
    }

    public void start() {
        try {
            ClassPathResource resource = new ClassPathResource(configPath);

            Properties pro = new Properties();
            try {
                pro.load(resource.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Prepare lan.properties is failed, " + e.getMessage());
            }
            lanId = pro.getProperty("lan.id");
            if (StringUtils.isBlank(lanId)) {
                throw new CoreException(CoreErrorCodes.ERROR_LANID_ILLEGAL, "LanId is illegal, " + lanId);
            }

            if (httpPort == null) {
                throw new CoreException(ChatErrorCodes.ERROR_CORE_SERVERPORT_ILLEGAL, "Server port is null");
            }

            if (server == null) {
                server = ChatUtils.generateFixedRandomString();
            }
            prepare();

            fetchConfig();
            try {
                String serverType = (String) this.config.get("server.type");
                if (serverType != null) {
                    this.serverType = serverType;
                }
            } catch (Exception e) {
                LoggerEx.info(TAG, "'serverType' not font in config.");
            }

            if (tcpPort != null) {
                try {
                    dockerStatus.setTcpPort(Integer.parseInt(tcpPort));
                } catch (Throwable t) {
                }
            }
            if (sslTcpPort != null) {
                try {
                    dockerStatus.setSslTcpPort(Integer.parseInt(sslTcpPort));
                } catch (Throwable t) {
                }
            }
            if (wsPort != null) {
                try {
                    dockerStatus.setWsPort(Integer.parseInt(wsPort));
                } catch (Throwable t) {
                }
            }

            LoggerEx.info(TAG, "Get serverType: " + serverType);
            if (dockerStatusService != null) {
                dockerStatus = generateDockerStatus(httpPort);
                dockerStatusService.addDockerStatus(dockerStatus);
            }

//            QueuedThreadPool threadPool = ServerStart.getInstance().getThreadPool();
//            if (tasks != null) {
//                for (Task task : tasks) {
//                    task.setOnlineServer(this);
//                    task.init();
//                    LoggerEx.info(TAG, "Task " + task + " initialized!");
//                    int numOfThreads = task.getNumOfThreads();
//                    for (int i = 0; i < numOfThreads; i++) {
//                        threadPool.execute(task);
//                    }
//                }
//            }

            //Will call below only when server enter OK status from standby status.
//			if(startHandler != null) {
//				startHandler.serverWillStart(this);
//			}
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Start online server " + server + " failed, " + e.getMessage());
            if (dockerStatusService != null) {
                try {
                    dockerStatusService.deleteDockerStatus(server);
                    LoggerEx.info(TAG, "Deleted OnlineServer " + server + " because of error " + e.getMessage());
                } catch (CoreException e1) {
                    e.printStackTrace();
                    LoggerEx.info(TAG, "Remove online server " + server + " failed, " + e1.getMessage());
                }
            }
            OnlineServer.shutdownNow();
            System.exit(0);
        }
    }

    public Properties fetchConfig() {
        if (this.config != null) {
            return this.config;
        }
        ClassPathResource configResource = new ClassPathResource(configPath);
        Properties config = new Properties();
        try {
            config.load(configResource.getInputStream());
        } catch (IOException e) {
            LoggerEx.error(TAG, "Prepare config.properties failed, can't do anything. " + e.getMessage());
        }

        if (sdockersService != null) {
            try {
                Document sDocker = sdockersService.getSDockerConf(ipHolder.getIp(), httpPort);
                for (String field : sDocker.keySet()) {
                    if (field.equals("_id")) continue;
                    config.put(field.toString().replaceAll("_", "."), sDocker.get(field));
                    LoggerEx.info(TAG, field.toString());
                }
            } catch (Throwable e) {
                LoggerEx.warn(TAG, "Can not get SDocker configuration in DB, will use local config.");
            }
        }
        this.config = config;
        return config;
    }

    public static void shutdownNow() {
        OnlineServer onlineServer = OnlineServer.getInstance();
        if (onlineServer != null)
            onlineServer.shutdown();
    }

    public void shutdown() {
        LoggerEx.info(TAG, "OnlineServer " + server + " is shutting down");
        if (startHandler != null) {
            try {
                startHandler.serverWillShutdown(this);
            } catch (Exception e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "StartHandler " + startHandler + " shutdown failed, " + e.getMessage());
            }
        }
        if (dockerStatusService != null) {
            try {
                dockerStatusService.deleteDockerStatus(server);
                LoggerEx.info(TAG, "Deleted OnlineServer " + server);
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "Remove online server " + server + " failed, " + e.getMessage());
            }
        }
        if (tasks != null) {
            LoggerEx.info(TAG, "Deleted tasks " + tasks + " size " + tasks.size());
            for (Task task : tasks) {
                try {
                    LoggerEx.info(TAG, "Task " + task + " is shutting down");
                    task.shutdown();
                    LoggerEx.info(TAG, "Task " + task + " has been shutdown");
                } catch (Exception e) {
                    e.printStackTrace();
                    LoggerEx.fatal(TAG, "Task " + task + " shutdown failed, " + e.getMessage());
                }
            }
        }
//        if (shutdownList != null) {
//            LoggerEx.info(TAG, "Deleted shutdownListener " + shutdownList + " size " + shutdownList.size());
//            for (ShutdownListener shutdownListener : shutdownList) {
//                try {
//                    LoggerEx.info(TAG, "shutdownListener " + shutdownListener + " is shutting down");
//                    shutdownListener.shutdown();
//                    LoggerEx.info(TAG, "shutdownListener " + shutdownListener + " has been shutdown");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LoggerEx.fatal(TAG, "shutdownListener " + shutdownListener + " shutdown failed, " + e.getMessage());
//                }
//            }
//        }
    }

    public String getServer() {
        return server;
    }

    public void setServer(String serverName) {
        this.server = serverName;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public OnlineServerStartHandler getStartHandler() {
        return startHandler;
    }

    public void setStartHandler(OnlineServerStartHandler startHandler) {
        this.startHandler = startHandler;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public DockerStatus getDockerStatus() {
        return dockerStatus;
    }

    public void setDockerStatus(DockerStatus dockerStatus) {
        this.dockerStatus = dockerStatus;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(String rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(String tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getLanId() {
        return lanId;
    }

    public void setLanId(String lanId) {
        this.lanId = lanId;
    }

    public String getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public String getSslRpcPort() {
        return sslRpcPort;
    }

    public void setSslRpcPort(String sslRpcPort) {
        this.sslRpcPort = sslRpcPort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getWsPort() {
        return wsPort;
    }

    public void setWsPort(String wsPort) {
        this.wsPort = wsPort;
    }

    public DockerStatusService getDockerStatusService() {
        return dockerStatusService;
    }

    public void setDockerStatusService(DockerStatusService dockerStatusService) {
        this.dockerStatusService = dockerStatusService;
    }

    public String getRpcSslClientTrustJksPath() {
        return rpcSslClientTrustJksPath;
    }

    public void setRpcSslClientTrustJksPath(String rpcSslClientTrustJksPath) {
        this.rpcSslClientTrustJksPath = rpcSslClientTrustJksPath;
    }

    public String getRpcSslServerJksPath() {
        return rpcSslServerJksPath;
    }

    public void setRpcSslServerJksPath(String rpcSslServerJksPath) {
        this.rpcSslServerJksPath = rpcSslServerJksPath;
    }

    public String getRpcSslJksPwd() {
        return rpcSslJksPwd;
    }

    public void setRpcSslJksPwd(String rpcSslJksPwd) {
        this.rpcSslJksPwd = rpcSslJksPwd;
    }

    public IPHolder getIpHolder() {
        return ipHolder;
    }

    public void setIpHolder(IPHolder ipHolder) {
        this.ipHolder = ipHolder;
    }

    public SDockersService getSdockersService() {
        return sdockersService;
    }

    public void setSdockersService(SDockersService sdockersService) {
        this.sdockersService = sdockersService;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public String getSslTcpPort() {
        return sslTcpPort;
    }

    public void setSslTcpPort(String sslTcpPort) {
        this.sslTcpPort = sslTcpPort;
    }

}