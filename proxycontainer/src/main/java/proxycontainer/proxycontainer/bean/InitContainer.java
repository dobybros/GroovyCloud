package proxycontainer.proxycontainer.bean;

import chat.utils.IPHolder;
import com.alibaba.fastjson.util.TypeUtils;
import com.dobybros.chat.handlers.ProxyContainerDuplexSender;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.gateway.channels.websocket.netty.WebSocketManager;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.script.ScriptManager;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.*;
import com.docker.utils.AutoReloadProperties;
import com.docker.utils.GroovyCloudBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by lick on 2019/5/27.
 * Description：
 */
@Component
public class InitContainer implements CommandLineRunner{
    @Autowired
    MongoHelper dockerStatusHelper;
    @Autowired
    GlobalLansProperties globalLansProperties;
    @Autowired
    MongoHelper configHelper;
    @Autowired
    MongoHelper scheduledTaskHelper;
    @Autowired
    DockerStatusDAO dockerStatusDAO;
    @Autowired
    ServiceVersionDAO serviceVersionDAO;
    @Autowired
    DeployServiceVersionDAO deployServiceVersionDAO;
    @Autowired
    ServersDAO serversDAO;
    @Autowired
    ScheduledTaskDAO scheduledTaskDAO;
    @Autowired
    LansDAO lansDAO;
    @Autowired
    SDockerDAO sDockerDAO;
    @Autowired
    MongoHelper repairHelper;
    @Autowired
    RepairDAO repairDAO;
    @Autowired
    MongoHelper logsHelper;
    @Autowired
    MongoHelper gridfsHelper;
    @Autowired
    GridFSFileHandler fileAdapter;
    @Autowired
    IPHolder ipHolder;
    @Autowired
    AutoReloadProperties oauth2ClientProperties;
    @Autowired
    ScriptManager scriptManager;
    @Autowired
    RMIServerHandler dockerRpcServerAdapter;
    @Autowired
    RMIServerHandler dockerRpcServerAdapterSsl;
    @Autowired
    OnlineServerWithStatus onlineServer;
    @Autowired
    OnlineUserManagerImpl onlineUserManager;
    @Autowired
    ProxyContainerDuplexSender proxyContainerDuplexSender;
    @Autowired
    WebSocketManager webSocketManager;

    @Override
    public void run(String... args) throws Exception {
        GroovyCloudBean.map();
        TypeUtils.compatibleWithJavaBean = true;
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        globalLansProperties.init();
        dockerStatusHelper.init();
        configHelper.init();
        scheduledTaskHelper.init();
        dockerStatusDAO.init();
        serviceVersionDAO.init();
        deployServiceVersionDAO.init();
        serversDAO.init();
        lansDAO.init();
        sDockerDAO.init();
        repairHelper.init();
        repairDAO.init();
        scheduledTaskDAO.init();
        logsHelper.init();
        gridfsHelper.init();
        fileAdapter.init();
        ipHolder.init();
        oauth2ClientProperties.init();
        onlineServer.start();
        onlineUserManager.init();
        scriptManager.init();
        dockerRpcServerAdapter.serverStart();
        dockerRpcServerAdapterSsl.serverStart();
        proxyContainerDuplexSender.init();
        webSocketManager.start();
    }
}
