package com.dobybros.chat.handlers;

import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyResponse;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMRequest;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMResponse;
import com.docker.rpc.QueueSimplexListener;
import com.docker.rpc.remote.stub.RemoteServers;

import javax.annotation.Resource;

/**
 * @author lick
 * @date 2019/11/15
 */
public class QueueProxyContainerDuplexSender {
    private final String TAG = QueueProxyContainerDuplexSender.class.getSimpleName();
    @Resource
    QueueSimplexListener queueSimplexListener;
    ProxyIMResponse sendIM(ProxyIMRequest request, RemoteServers.Server server) {
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                queueSimplexListener.send(server.getServer(), request.getType(), request.getData(), request.getEncode());
            }
        }
        return null;
    }

    IMProxyResponse sendProxy(IMProxyRequest request, RemoteServers.Server server){
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                queueSimplexListener.send(server.getServer(), request.getType(), request.getData(), request.getEncode());
            }
        }
        return null;
    }
}
