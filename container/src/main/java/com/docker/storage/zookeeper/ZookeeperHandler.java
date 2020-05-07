package com.docker.storage.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * Created by lick on 2020/4/22.
 * Description：
 */
public class ZookeeperHandler {
    private ZookeeperClient client = new ZookeeperClient();

    public void connect(String zkHost) {
        RetryNTimes retryNTimes = new RetryNTimes(5, 10000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(zkHost)
                .sessionTimeoutMs(30000)
                .connectionTimeoutMs(15000)
                .namespace("groovycloud")
                .retryPolicy(retryNTimes)
                .build();
        client.setCuratorFramework(curatorFramework);
    }
    public void disconnect(){
        if(client != null){
            client.close();
        }
    }
    public ZookeeperClient getClient() {
        return client;
    }
}
