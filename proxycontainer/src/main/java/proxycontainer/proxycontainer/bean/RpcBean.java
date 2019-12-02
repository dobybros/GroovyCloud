package proxycontainer.proxycontainer.bean;

import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.impl.RMIServerHandler;
import imcontainer.imcontainer.bean.IMBeanApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: lick
 * @Description:
 * @Date:2019/5/26 16:50
 */
@Configuration
public class RpcBean {
    private IMBeanApp instance;

    RpcBean() {
        instance = IMBeanApp.getInstance();
    }

    @Bean
    public RPCClientAdapterMap rpcClientAdapterMap() {
        return instance.getRpcClientAdapterMap();
    }

    @Bean
    public RPCClientAdapterMap rpcClientAdapterMapSsl() {
        return instance.getRpcClientAdapterMapSsl();
    }

    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServer() {
        return instance.getDockerRpcServer();
    }

    @Bean
    public RMIServerHandler dockerRpcServerAdapter() {
        return instance.getDockerRpcServerAdapter();
    }

    @Bean
    public com.docker.rpc.impl.RMIServerImplWrapper dockerRpcServerSsl() {
        return instance.getDockerRpcServerSsl();
    }

    @Bean
    public RMIServerHandler dockerRpcServerAdapterSsl() {
        return instance.getDockerRpcServerAdapterSsl();
    }
}
