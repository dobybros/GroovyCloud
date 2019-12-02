package com.dobybros.chat.handlers;

import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.channels.Channel;
import com.dobybros.chat.open.data.IMConfig;
import com.dobybros.chat.script.annotations.gateway.GatewayGroovyRuntime;
import com.dobybros.gateway.channels.tcp.TcpChannel;
import com.dobybros.gateway.onlineusers.OnlineServiceUser;
import com.dobybros.gateway.onlineusers.OnlineUser;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.dobybros.gateway.onlineusers.OnlineUsersHolder;
import com.docker.script.BaseRuntime;
import com.docker.script.ScriptManager;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/11/26.
 * Description：
 */
public class PingHandler {
    @Resource
    OnlineUserManager onlineUserManager;
    @Resource
    ScriptManager scriptManager;
    private Boolean useProxy = false;
    public void init(){
        if(!useProxy){
            TimerEx.schedule(new TimerTaskEx() {
                @Override
                public void execute() {
                    OnlineUsersHolder onlineUserHolder = onlineUserManager.getOnlineUsersHolder();
                    if (onlineUserHolder != null) {
                        for (OnlineUser user : onlineUserHolder.onlineUsers()) {
                            ConcurrentHashMap<String, OnlineServiceUser> serviceUserMap = user.getServiceUserMap();
                            Collection<OnlineServiceUser> values = serviceUserMap.values();
                            for (OnlineServiceUser serviceUser : values) {
                                Map<Integer, Channel> channelMap = serviceUser.getChannelMap();
                                if (!channelMap.isEmpty()) {
                                    for (Integer terminal : channelMap.keySet()) {
                                        Channel channel = channelMap.get(terminal);
                                        if (channel instanceof TcpChannel) {
                                            TcpChannel tcpChannel = (TcpChannel) channel;
                                            BaseRuntime runtime = scriptManager.getBaseRuntime(serviceUser.getServiceAndVersion());
                                            if (runtime != null && runtime instanceof GatewayGroovyRuntime) {
                                                IMConfig imConfig = ((GatewayGroovyRuntime) runtime).getIMConfig(user.getUserId(), serviceUser.getService());
                                                if(imConfig == null){
                                                    imConfig = new IMConfig();
                                                }
                                                if ((System.currentTimeMillis() - tcpChannel.getPingTime()) > (imConfig.getPingInterval() * 2 + 2000)) {
                                                    ((GatewayGroovyRuntime) runtime).pingTimeoutReceived(user.getUserId(), serviceUser.getService(), terminal);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, 10000L, 20000L);
        }
    }

    public void setUseProxy(Boolean useProxy) {
        this.useProxy = useProxy;
    }
}
