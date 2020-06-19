package com.docker.rpc;

import java.util.Map;

/**
 * Created by lick on 2020/2/8.
 * Description：
 */
public interface QueueSimplexListener {
    public void send(String key, String type, byte[] data, byte encode);

    public void init();

    public void setConfig(Map<String, String> config);

    public void shutdown();
}
