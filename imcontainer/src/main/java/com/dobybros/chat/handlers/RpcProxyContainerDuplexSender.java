package com.dobybros.chat.handlers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyResponse;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMResponse;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMRequest;
import com.dobybros.gateway.channels.data.Result;
import com.dobybros.gateway.pack.HailPack;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.RPCClientAdapterMapFactory;
import com.docker.rpc.remote.stub.RemoteServers;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * @author lick
 * @date 2019/11/15
 */
public class RpcProxyContainerDuplexSender {
    private final String TAG = RpcProxyContainerDuplexSender.class.getSimpleName();
    private RPCClientAdapterMap rpcClientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMap();

    ProxyIMResponse sendIM(ProxyIMRequest request, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener) {
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                ProxyIMResponse response = null;
                try {
                    RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(server.getIp(), server.getRpcPort(), server.getServer(), clientAdapterStatusListener);
                    if (clientAdapter != null) {
                        response = (ProxyIMResponse) clientAdapter.call(request);
                        if(response != null){
                            response.resurrect();
                            return response;
                        }
                    }
                } catch (Throwable throwable) {
                    LoggerEx.error(TAG, "Call remoteservice failed,ProxyIMRequest: " + JSON.toJSONString(request) + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                    throwable.printStackTrace();
                    if (throwable instanceof CoreException) {
                        Result result = new Result();
                        result.setForId(request.getForId());
                        result.setCode(((CoreException) throwable).getCode());
                        result.setDescription(((CoreException) throwable).getMessage());
                        response = new ProxyIMResponse();
                        try {
                            result.persistent();
                            response.setReturnData(result.getData());
                            response.setReturnType(HailPack.TYPE_OUT_RESULT);
                            return response;
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    IMProxyResponse sendProxy(IMProxyRequest request, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener) throws CoreException{
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(server.getIp(), server.getRpcPort(), server.getServer(), clientAdapterStatusListener);
                if (clientAdapter != null) {
                    IMProxyResponse response = (IMProxyResponse) clientAdapter.call(request);
                    if(response != null){
                        response.resurrect();
                        return response;
                    }
                }
            }
        }
        return null;
    }
}
