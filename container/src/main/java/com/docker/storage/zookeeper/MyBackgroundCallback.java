package com.docker.storage.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;

/**
 * Created by lick on 2020/4/22.
 * Description：
 */
public abstract class MyBackgroundCallback implements BackgroundCallback {
    @Override
    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
        callback();
    }
    public abstract void callback();
}
