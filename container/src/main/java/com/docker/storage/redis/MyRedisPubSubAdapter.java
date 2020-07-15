package com.docker.storage.redis;

import chat.main.ServerStart;
import com.docker.utils.GroovyCloudBean;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubAdapter;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.async.NodeSelectionPubSubAsyncCommands;
import io.lettuce.core.cluster.pubsub.api.async.PubSubAsyncNodeSelection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import redis.clients.jedis.HostAndPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lick on 2020/2/18.
 * Description：
 */
public class MyRedisPubSubAdapter extends RedisPubSubAdapter {
    private static volatile MyRedisPubSubAdapter instance;
    private RedisClusterClient redisClusterClient;
    private ClusterGrooveAdapter clusterGrooveAdapter;
    private Set<HostAndPort> hostAndPorts;
    private StatefulRedisClusterPubSubConnection<String, String> pubSubConnection;
    private NodeSelectionPubSubAsyncCommands<String, String> commands;

    private MyRedisPubSubAdapter() {
        clusterGrooveAdapter = new ClusterGrooveAdapter();
    }

    private void setHostAndPorts(Set<HostAndPort> hostAndPorts, String passswd) {
        if (redisClusterClient == null) {
            this.hostAndPorts = hostAndPorts;
            if (hostAndPorts != null) {
                List<RedisURI> redisURIS = new ArrayList<>();
                for (HostAndPort hostAndPort : hostAndPorts) {
                    RedisURI redisURI = RedisURI.create(hostAndPort.getHost(), hostAndPort.getPort());
                    if(passswd !=  null){
                        redisURI.setPassword(passswd);
                    }
                    redisURIS.add(redisURI);
                }
                if (!redisURIS.isEmpty()) {
                    redisClusterClient = RedisClusterClient.create(redisURIS);
                }
            }
        }
    }

    public static MyRedisPubSubAdapter getInstance() {
        if (instance == null) {
            synchronized (MyRedisPubSubAdapter.class) {
                if (instance == null) {
                    instance = new MyRedisPubSubAdapter();
                }
            }
        }
        return instance;
    }

    public void shutdown() {
        if (redisClusterClient != null) {
            redisClusterClient.shutdown();
        }
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
    }

    void psubscribe(String[] subscribeChannels, Set<HostAndPort> redisNodes, String passwd) {
        setHostAndPorts(redisNodes, passwd);
        if (commands == null) {
            // 异步订阅
            pubSubConnection = redisClusterClient.connectPubSub();
            pubSubConnection.setNodeMessagePropagation(true);
            pubSubConnection.addListener(clusterGrooveAdapter);
            PubSubAsyncNodeSelection<String, String> masters = pubSubConnection.async().masters();
            commands = masters.commands();
            try {
                commands.punsubscribe(subscribeChannels);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            commands.psubscribe(subscribeChannels);
        }
    }

    private class ClusterGrooveAdapter extends RedisClusterPubSubAdapter {
        private final String TAG = ClusterGrooveAdapter.class.getSimpleName();
        private RedisSubscribeHandler redisSubscribeHandler = (RedisSubscribeHandler) GroovyCloudBean.getBean(GroovyCloudBean.REDISSUBSCRIBEHANDLER);

        @Override
        public void message(RedisClusterNode node, Object channel, Object message) {
            ServerStart.getInstance().getThreadPool().execute(() -> redisSubscribeHandler.redisCallback((String) message));
        }

        @Override
        public void message(RedisClusterNode node, Object pattern, Object channel, Object message) {
            ServerStart.getInstance().getThreadPool().execute(() -> redisSubscribeHandler.redisCallback((String) message));
        }

        @Override
        public void subscribed(RedisClusterNode node, Object channel, long count) {
            super.subscribed(node, channel, count);
        }

        @Override
        public void psubscribed(RedisClusterNode node, Object pattern, long count) {
            super.psubscribed(node, pattern, count);
        }

        @Override
        public void unsubscribed(RedisClusterNode node, Object channel, long count) {
            super.unsubscribed(node, channel, count);
        }

        @Override
        public void punsubscribed(RedisClusterNode node, Object pattern, long count) {
            super.punsubscribed(node, pattern, count);
        }
    }
}
