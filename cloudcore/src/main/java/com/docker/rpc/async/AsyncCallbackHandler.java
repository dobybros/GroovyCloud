package com.docker.rpc.async;

import java.util.Map;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public abstract class AsyncCallbackHandler {
    //handler with params want
    protected Map map;
    //async return result
    protected Object result;

    public AsyncCallbackHandler(Map map){
        this.map = map;
    }

    public abstract void handle();

    public void setResult(Object result) {
        this.result = result;
    }
}
