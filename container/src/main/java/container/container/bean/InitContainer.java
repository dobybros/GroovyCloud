package container.container.bean;

import chat.utils.IPHolder;
import com.dobybros.chat.props.GlobalLansProperties;
import com.dobybros.chat.utils.AutoReloadProperties;
import com.dobybros.file.adapters.GridFSFileHandler;
import com.dobybros.gateway.onlineusers.impl.OnlineUserManagerImpl;
import com.docker.onlineserver.OnlineServerWithStatus;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.script.ScriptManager;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.storage.mongodb.daos.DockerStatusDAO;
import com.docker.storage.mongodb.daos.LansDAO;
import com.docker.storage.mongodb.daos.SDockerDAO;
import com.docker.storage.mongodb.daos.ServersDAO;
import org.apache.mina.transport.socket.nio.NioSocketAcceptorEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
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
    DockerStatusDAO dockerStatusDAO;
    @Autowired
    ServersDAO serversDAO;
    @Autowired
    LansDAO lansDAO;
    @Autowired
    SDockerDAO sDockerDAO;
    @Autowired
    MongoHelper logsHelper;
    @Autowired
    MongoHelper gridfsHelper;
    @Autowired
    GridFSFileHandler fileAdapter;
    @Autowired
    NioSocketAcceptorEx tcpIoAcceptor;
    @Autowired
    IPHolder ipHolder;
    @Autowired
    AutoReloadProperties oauth2ClientProperties;
    @Autowired
    ScriptManager scriptManager;
//    @Autowired
//    RMIHandler rpcServerAdapter;
    @Autowired
    RMIServerHandler dockerRpcServerAdapter;
    @Autowired
    OnlineServerWithStatus onlineServer;
    @Autowired
    OnlineUserManagerImpl onlineUserManager;

    @Override
    public void run(String... args) throws Exception {
        globalLansProperties.init();
        dockerStatusHelper.init();
        configHelper.init();
        dockerStatusDAO.init();
        serversDAO.init();
        lansDAO.init();
        sDockerDAO.init();
        logsHelper.init();
        gridfsHelper.init();
        fileAdapter.init();
        tcpIoAcceptor.bind();
        ipHolder.init();
        oauth2ClientProperties.init();
        onlineServer.start();
        scriptManager.init();
        onlineUserManager.init();
//        rpcServerAdapter.serverStart();
        dockerRpcServerAdapter.serverStart();
    }
}
