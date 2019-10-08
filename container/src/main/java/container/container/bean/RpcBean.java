package container.container.bean;

import com.dobybros.gateway.eventhandler.MessageEventHandler;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.impl.RMIServerHandler;
import com.docker.rpc.remote.stub.RPCInterceptorFactory;
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
    public RPCClientAdapterMap rpcClientAdapterMap(){
        return instance.getRpcClientAdapterMap();
    }
    @Bean
    public RPCClientAdapterMap rpcClientAdapterMapSsl(){
        return instance.getRpcClientAdapterMapSsl();
    }
//    @Bean
//    public RMIServerImplWrapper rpcServer(){
//        RMIServerImplWrapper rpcServer = instance.getRpcServer();
//        rpcServer.setRmiServerHandler(instance.getDockerRpcServerAdapter());
//        return rpcServer;
//    }
//    @Bean
//    public RMIServerHandler rpcServerAdapter(){
//        RMIServerHandler rpcServerAdapter = instance.getDockerRpcServerAdapter();
//        rpcServerAdapter.setServerImpl(instance.getRpcServer());
//        rpcServerAdapter.setIpHolder(instance.getIpHolder());
//        rpcServerAdapter.setRmiPort(Integer.valueOf(instance.getRpcPort()));
//        return rpcServerAdapter;
//    }
//    @Bean
//    public RMIServerImplWrapper rpcServerSsl(){
//        RMIServerImplWrapper rpcServerSsl = instance.getRpcServerSsl();
//        rpcServerSsl.setRmiServerHandler(instance.getDockerRpcServerAdapterSsl());
//        return rpcServerSsl;
//    }
//    @Bean
//    public RMIServerHandler rpcServerAdapterSsl(){
//        RMIServerHandler rpcServerAdapterSsl = instance.getDockerRpcServerAdapterSsl();
//        rpcServerAdapterSsl.setServerImpl(instance.getRpcServerSsl());
//        rpcServerAdapterSsl.setRmiPort(Integer.valueOf(instance.getSslRpcPort()));
//        rpcServerAdapterSsl.setEnableSsl(true);
//        rpcServerAdapterSsl.setRpcSslClientTrustJksPath(instance.getRpcSslClientTrustJksPath());
//        rpcServerAdapterSsl.setRpcSslServerJksPath(instance.getRpcSslServerJksPath());
//        rpcServerAdapterSsl.setRpcSslJksPwd(instance.getRpcSslJksPwd());
//        return rpcServerAdapterSsl;
//    }
    @Bean
    public MessageEventHandler messageEventHandler(){
        return instance.getMessageEventHandler();
    }
    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServer(){
        return instance.getDockerRpcServer();
    }
    @Bean
    public RMIServerHandler dockerRpcServerAdapter(){
        return instance.getDockerRpcServerAdapter();
    }
    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServerSsl(){
        return instance.getDockerRpcServerSsl();
    }
    @Bean
    public RMIServerHandler dockerRpcServerAdapterSsl(){
        return instance.getDockerRpcServerAdapterSsl();
    }
    @Bean
    public RPCInterceptorFactory rpcInterceptorFactory(){
        return instance.getRPCInterceptorFactory();
    }
}
