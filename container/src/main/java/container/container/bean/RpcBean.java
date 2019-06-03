package container.container.bean;

import com.dobybros.chat.tasks.RPCClientAdapterMapTask;
import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.impl.RMIServerImplWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 16:50
 */
@Configuration
public class RpcBean{
    private BeanApp instance;
    RpcBean(){
        instance = BeanApp.getInstance();
    }
    @Bean
    public RPCClientAdapterMapTask rpcClientAdapterMapTask(){
        return instance.getRpcClientAdapterMapTask();
    }
    @Bean
    public RPCClientAdapterMapTask rpcClientAdapterMapTaskSsl(){
        RPCClientAdapterMapTask rpcClientAdapterMapTaskSsl = instance.getRpcClientAdapterMapTaskSsl();
        rpcClientAdapterMapTaskSsl.setEnableSsl(true);
        rpcClientAdapterMapTaskSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
        rpcClientAdapterMapTaskSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
        rpcClientAdapterMapTaskSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        return rpcClientAdapterMapTaskSsl;
    }
    @Bean
    public RMIServerImplWrapper rpcServer(){
        RMIServerImplWrapper rpcServer = instance.getRpcServer();
        rpcServer.setRmiServerHandler(instance.getDockerRpcServerAdapter());
        return rpcServer;
    }
//    @Bean(initMethod = "serverStart")
    @Bean
    public RMIServerHandler rpcServerAdapter(){
        RMIServerHandler rpcServerAdapter = instance.getDockerRpcServerAdapter();
        rpcServerAdapter.setServerImpl(instance.getRpcServer());
        rpcServerAdapter.setIpHolder(instance.getIpHolder());
        rpcServerAdapter.setRmiPort(Integer.valueOf(instance.getRpcPort()));
        return rpcServerAdapter;
    }
    @Bean
    public RMIServerImplWrapper rpcServerSsl(){
        RMIServerImplWrapper rpcServerSsl = instance.getRpcServerSsl();
        rpcServerSsl.setRmiServerHandler(instance.getDockerRpcServerAdapterSsl());
        return rpcServerSsl;
    }
    @Bean
    public RMIServerHandler rpcServerAdapterSsl(){
        RMIServerHandler rpcServerAdapterSsl = instance.getDockerRpcServerAdapterSsl();
        rpcServerAdapterSsl.setServerImpl(instance.getRpcServerSsl());
        rpcServerAdapterSsl.setRmiPort(Integer.valueOf(instance.getSslRpcPort()));
        rpcServerAdapterSsl.setEnableSsl(true);
        rpcServerAdapterSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
        rpcServerAdapterSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
        rpcServerAdapterSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
        return rpcServerAdapterSsl;
    }
    @Bean
    public MessageEventHandler messageEventHandler(){
        return instance.getMessageEventHandler();
    }
    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServer(){
        com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServer = instance.getDockerRpcServer();
        dockerRpcServer.setRmiServerHandler(instance.getDockerRpcServerAdapter());
        return dockerRpcServer;
    }
//    @Bean(initMethod = "serverStart")
    @Bean
    public RMIServerHandler dockerRpcServerAdapter(){
        RMIServerHandler dockerRpcServerAdapter = instance.getDockerRpcServerAdapter();
        dockerRpcServerAdapter.setServerImpl(instance.getDockerRpcServer());
        dockerRpcServerAdapter.setIpHolder(instance.getIpHolder());
        dockerRpcServerAdapter.setRmiPort(Integer.valueOf(instance.getDockerRpcPort()));
        return dockerRpcServerAdapter;
    }
    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServerSsl(){
        com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServerSsl = instance.getDockerRpcServerSsl();
        dockerRpcServerSsl.setRmiServerHandler(instance.getDockerRpcServerAdapterSsl());
        return dockerRpcServerSsl;
    }
    @Bean
    public RMIServerHandler dockerRpcServerAdapterSsl(){
        RMIServerHandler dockerRpcServerAdapterSsl = instance.getDockerRpcServerAdapterSsl();
        dockerRpcServerAdapterSsl.setServerImpl(instance.getDockerRpcServerSsl());
        return dockerRpcServerAdapterSsl;
    }
}
