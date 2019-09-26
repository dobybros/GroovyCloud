package com.docker.rpc.async;

import chat.utils.TimerTaskEx;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public class AsyncFuture {
    private Long crc;
    private CompletableFuture<?> future;
    private List<AsyncCallbackHandler> asyncCallbackHandlers;
    private TimerTaskEx timerTaskEx;

    public TimerTaskEx getTimerTaskEx() {
        return timerTaskEx;
    }

    public void setTimerTaskEx(TimerTaskEx timerTaskEx) {
        this.timerTaskEx = timerTaskEx;
    }

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public CompletableFuture<?> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<?> future) {
        this.future = future;
    }

    public List<AsyncCallbackHandler> getAsyncCallbackHandlers() {
        return asyncCallbackHandlers;
    }

    public void setAsyncCallbackHandlers(List<AsyncCallbackHandler> asyncCallbackHandlers) {
        this.asyncCallbackHandlers = asyncCallbackHandlers;
    }
}
