package com.dobybros.chat.handlers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest;
import com.dobybros.chat.rpc.reqres.balancer.IMResponse;
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

    IMResponse sendIM(ProxyIMRequest request, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener) {
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                IMResponse response = null;
                try {
                    RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(server.getIp(), server.getRpcPort(), server.getServer());
                    if (clientAdapter != null) {
                        response = (IMResponse) clientAdapter.call(request);
                        response.resurrect();
                        return response;
                    }
                } catch (Throwable throwable) {
                    LoggerEx.error(TAG, "Call remoteservice failed,ProxyIMRequest: " + JSON.toJSONString(request) + ",errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                    throwable.printStackTrace();
                    if (throwable instanceof CoreException) {
                        Result result = new Result();
                        result.setForId(request.getForId());
                        result.setCode(((CoreException) throwable).getCode());
                        result.setDescription(((CoreException) throwable).getMessage());
                        response = new IMResponse();
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

    IMResponse sendProxy(IMProxyRequest request, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener) throws CoreException{
        if (request != null && request.checkParamsNotNull()) {
            if (server != null) {
                RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(server.getIp(), server.getRpcPort(), server.getServer(), clientAdapterStatusListener);
                if (clientAdapter != null) {
                    IMResponse response = (IMResponse) clientAdapter.call(request);
                    response.resurrect();
                    return response;
                }
            }
        }
        return null;
    }
}
