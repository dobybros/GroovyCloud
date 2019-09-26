package com.docker.rpc.async;

import java.util.Map;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public abstract class AsyncCallbackHandler {
    public Map map;
    public abstract void handle();
}
