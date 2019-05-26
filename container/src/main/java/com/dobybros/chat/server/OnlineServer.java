package com.dobybros.chat.server;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.utils.ChatUtils;
import chat.utils.IPHolder;
import com.dobybros.chat.data.serverstatus.ServerStatus;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.data.DockerStatus;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.tasks.Task;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;


public class OnlineServer extends com.docker.server.OnlineServer{
    private static final String TAG = OnlineServer.class.getSimpleName();
    private String server;

    private List<Task> tasks;

    private String internalKey;

    private String serverType;

    private DockerStatusService dockerStatusService;

//    @Resource
    private IPHolder ipHolder = (IPHolder) SpringContextUtil.getBean("ipHolder");

    public static String PREFIX_ROOT;
    public static String PREFIX_ROOT_LOCKS;
    public static String PREFIX_ROOT_PENDING;
    public static String PREFIX_ROOT_SERVERS_ONLINE;
    public static String PREFIX_ROOT_SERVERS;

    private static OnlineServer instance;

    private ServerStatus serverStatus;

    private OnlineServerStartHandler startHandler;

    private String sslRpcPort;
    private String rpcPort;
    private String wsPort;
    private String tcpPort;
    private String sslTcpPort;
    private Integer status;
    private String configPath;
    private String dockerRpcPort;
    private String dockerSslRpcPort;

    private String lanId;

    protected OnlineServer() {
        instance = this;
    }

    public static OnlineServer getInstance() {
        return instance;
    }

    @Override
    public void prepare() {
    }

    protected DockerStatus generateDockerStatus() {
        DockerStatus dockerStatus = new DockerStatus();
        dockerStatus.setServerType(serverType);
        dockerStatus.setIp(serverStatus.getIp());
        dockerStatus.setHttpPort(serverStatus.getHttpPort());
        dockerStatus.setLanId(serverStatus.getLanId());

        if (dockerRpcPort != null) {
            try {
                dockerStatus.setRpcPort(Integer.parseInt(dockerRpcPort));
            } catch (Throwable t) {
            }
        }
        if (dockerSslRpcPort != null) {
            try {
                dockerStatus.setSslRpcPort(Integer.parseInt(dockerSslRpcPort));
            } catch (Throwable t) {
            }
        }
        dockerStatus.setServer(serverStatus.getServer());
        if(status == null)
            status = DockerStatus.STATUS_OK;
        dockerStatus.setStatus(status);
        Map<String, Object> info = new HashMap<String, Object>();
        info.put(ServerStatus.FIELD_SERVERSTATUS_SSLRPCPORT, sslRpcPort);
        info.put(ServerStatus.FIELD_SERVERSTATUS_TCPPORT, tcpPort);
        info.put(ServerStatus.FIELD_SERVERSTATUS_WEBSOCKETPORT, wsPort);
        info.put(ServerStatus.FIELD_SERVERSTATUS_RPCPORT, rpcPort);
        dockerStatus.setInfo(info);
        return dockerStatus;
    }

    @Override
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

            String serverPort = System.getProperty("server.port");
            if (serverPort == null || StringUtils.isBlank(serverPort)) {
                throw new CoreException(ChatErrorCodes.ERROR_CORE_SERVERPORT_ILLEGAL, "Server port is null");
            }
            Integer port = Integer.parseInt(serverPort);

            if (server == null)
                server = ChatUtils.generateFixedRandomString();
            prepare();
            serverStatus = new ServerStatus();
            serverStatus.setServer(server);
            serverStatus.setServerType(serverType);
            serverStatus.setIp(ipHolder.getIp());
            if (rpcPort != null) {
                try {
                    serverStatus.setRpcPort(Integer.parseInt(rpcPort));
                } catch (Throwable t) {
                }
            }
            if (sslRpcPort != null) {
                try {
                    serverStatus.setSslRpcPort(Integer.parseInt(sslRpcPort));
                } catch (Throwable t) {
                }
            }
            if (tcpPort != null) {
                try {
                    serverStatus.setTcpPort(Integer.parseInt(tcpPort));
                } catch (Throwable t) {
                }
            }
            if (sslTcpPort != null) {
                try {
                    serverStatus.setSslTcpPort(Integer.parseInt(sslTcpPort));
                } catch (Throwable t) {
                }
            }
            if (wsPort != null) {
                try {
                    serverStatus.setWsPort(Integer.parseInt(wsPort));
                } catch (Throwable t) {
                }
            }
            serverStatus.setHttpPort(port);
            serverStatus.setLanId(lanId);
            serverStatus.setHealth(0);
            if(status == null)
                status = ServerStatus.STATUS_OK;
            serverStatus.setStatus(status);

            if(dockerStatusService != null) {
                DockerStatus dockerStatus = generateDockerStatus();
                dockerStatusService.addDockerStatus(dockerStatus);
                serverStatus.setServer(dockerStatus.getServer());
            } else {

            }
            ThreadPoolExecutor threadPool = ServerStart.getInstance().getThreadPool();
            if (tasks != null) {
                for (Task task : tasks) {
                    task.setOnlineServer(this);
                    task.init();
                    LoggerEx.info(TAG, "Task " + task + " initialized!");
                    int numOfThreads = task.getNumOfThreads();
                    for (int i = 0; i < numOfThreads; i++) {
                        threadPool.execute(task);
                    }
                }
            }

            //Will call below only when server enter OK status from standby status.
//			if(startHandler != null) {
//				startHandler.serverWillStart(this);
//			}
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Start online server " + server + " failed, " + e.getMessage());
            // todo 改造
            /*ServerStatusAdapter serverStatusAdapter = StorageManager.getInstance().getStorageAdapter(ServerStatusAdapter.class);
            if (serverStatusAdapter != null) {
                try {
                    serverStatusAdapter.deleteServerStatus(server);
                    LoggerEx.info(TAG, "Deleted OnlineServer " + server + " because of error " + e.getMessage());
                } catch (CoreException e1) {
                    e.printStackTrace();
                    LoggerEx.info(TAG, "Remove online server " + server + " failed, " + e1.getMessage());
                }
            }*/
            // todo 改造 添加下面代码
            if(dockerStatusService != null) {
                try {
                    dockerStatusService.deleteDockerStatus(server);
                    LoggerEx.info(TAG, "Deleted docker " + server + " because of error " + e.getMessage());
                } catch (CoreException e1) {
                    e1.printStackTrace();
                    LoggerEx.fatal(TAG, "Remove docker server " + server + " failed, " + e1.getMessage());
                }
            }
            System.exit(0);
        }
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
        // todo 改造
        /*ServerStatusAdapter serverStatusAdapter = StorageManager.getInstance().getStorageAdapter(ServerStatusAdapter.class);
        if (serverStatusAdapter != null) {
            try {
                serverStatusAdapter.deleteServerStatus(server);
                LoggerEx.info(TAG, "Deleted OnlineServer " + server);
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "Remove online server " + server + " failed, " + e.getMessage());
            }
        }*/
        if(dockerStatusService != null) {
            try {
                dockerStatusService.deleteDockerStatus(server);
                LoggerEx.info(TAG, "Deleted docker " + server);
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "Remove docker server " + server + " failed, " + e.getMessage());
            }
        }
        if (tasks != null) {
            for (Task task : tasks) {
                try {
                    LoggerEx.info(TAG, "Task " + task + " is shutting down");
                    task.shutdown();
                    LoggerEx.info(TAG, "Task " + task + " has been shutdown");
                } catch (Exception e) {
                    e.printStackTrace();
                    LoggerEx.fatal(TAG, "Task shutdown failed, " + e.getMessage());
                }
            }
        }
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

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
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

    @Override
    public DockerStatusService getDockerStatusService() {
        return dockerStatusService;
    }

    @Override
    public void setDockerStatusService(DockerStatusService dockerStatusService) {
        this.dockerStatusService = dockerStatusService;
    }

    public String getDockerRpcPort() {
        return dockerRpcPort;
    }

    public void setDockerRpcPort(String dockerRpcPort) {
        this.dockerRpcPort = dockerRpcPort;
    }

    public String getDockerSslRpcPort() {
        return dockerSslRpcPort;
    }
    public String getSslTcpPort() {
        return sslTcpPort;
    }

    public void setDockerSslRpcPort(String dockerSslRpcPort) {
        this.dockerSslRpcPort = dockerSslRpcPort;
    }
    public void setSslTcpPort(String sslTcpPort) {
        this.sslTcpPort = sslTcpPort;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}