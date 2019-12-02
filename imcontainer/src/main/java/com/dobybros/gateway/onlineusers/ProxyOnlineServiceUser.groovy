package com.dobybros.gateway.onlineusers

import chat.logs.LoggerEx
import chat.utils.ConcurrentHashSet
import com.dobybros.chat.binary.data.Data
import com.dobybros.chat.channels.Channel
import com.dobybros.chat.handlers.ProxyContainerDuplexSender
import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime
import com.dobybros.gateway.channels.data.OutgoingData
import com.dobybros.gateway.channels.data.OutgoingMessage
import com.dobybros.gateway.channels.tcp.SimulateTcpChannel
import com.dobybros.gateway.pack.Pack
import com.docker.rpc.BinaryCodec
import com.docker.rpc.remote.stub.RemoteServers
import com.docker.script.BaseRuntime
import com.docker.utils.SpringContextUtil
import org.apache.commons.lang.exception.ExceptionUtils

import java.util.concurrent.ConcurrentHashMap
/**
 * @author lick
 * @date 2019/11/19
 */
class ProxyOnlineServiceUser extends OnlineServiceUser {
    private final String TAG = ProxyOnlineServiceUser.class.getSimpleName();
    private ProxyContainerDuplexSender proxyContainerDuplexSender = (ProxyContainerDuplexSender) SpringContextUtil.getBean("proxyContainerDuplexSender");

    private Map<String, RemoteServers.Server> serversMap = new ConcurrentHashMap<>();
    private Map<String, Set<Integer>> serverTerminaMap = new ConcurrentHashMap<>();
    @Override
    protected void pushToChannelsSync(Data event, Integer excludeTerminal, Integer toTerminal) {
        BaseRuntime runtime = getScriptManager().getBaseRuntime(getServiceAndVersion());
        if(runtime != null && runtime instanceof GatewayGroovyRuntime) {
            ((GatewayGroovyRuntime)runtime).messageSent(event, excludeTerminal, toTerminal, getUserInfo().getUserId(), getService());
        }
        if(toTerminal != null){
            SimulateTcpChannel channel = (SimulateTcpChannel) getChannel(toTerminal);
            if(channel != null){
                channel.setUserId(getUserInfo().getUserId());
                channel.setService(getServiceAndVersion());
                channel.send(event);
            }
        }else{
            try {
                IMProxyRequest request = new IMProxyRequest();
                request.setEncode(BinaryCodec.ENCODE_JAVABINARY)
                if(excludeTerminal != null){
                    Integer[] excludeTerminals = new Integer[]{excludeTerminal};
                    request.setExcludeTerminals(excludeTerminals);
                }
                request.setService(getService());
                request.setUserId(getUserInfo().getUserId());
                if(event.getData() == null){
                    try {
                        event.persistent();
                    }catch (Throwable throwable){
                        LoggerEx.error(TAG, "Event persistent error,errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                    }
                }
                if(event.getData() != null){
                    request.setTheData(event.getData());
                }
                request.setTheType(event.getType());
                String contentType = null;
                switch (event.getType()){
                    case Pack.TYPE_OUT_OUTGOINGMESSAGE:
                        contentType = ((OutgoingMessage)event).getContentType();
                        break;
                    case Pack.TYPE_OUT_OUTGOINGDATA:
                        contentType = ((OutgoingData)event).getContentType();
                        break;
                    default:
                        break
                }
                for (RemoteServers.Server server : serversMap.values()){
                    try {
                        proxyContainerDuplexSender.sendProxy(request, contentType, server, null);
                    }catch (Throwable t){
                        this.exceptionCaught(t);
                        serversMap.remove(server.getServer())
                        Set<Integer> terminals = serverTerminaMap.remove(server.getServer());
                        if(terminals != null && !terminals.isEmpty()){
                            for (Integer terminal : terminals){
                                Channel channel = getChannel(terminal);
                                if(channel != null){
                                    this.removeChannel(channel, CLOSE_ERROR);
                                }
                            }
                        }
                    }
                }
            }catch (Throwable throwable){
                throwable.printStackTrace();
                LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(throwable));
            }

        }
    }
    public void addServer(RemoteServers.Server server, Integer terminal){
        serversMap.putIfAbsent(server.getServer(), server);
        Set<Integer> terminals = serverTerminaMap[server.getServer()];
        if(terminals == null){
            terminals = new ConcurrentHashSet<>();
            serverTerminaMap.putIfAbsent(server.getServer(), terminals);
        }
        terminals.add(terminal);
    }
}
