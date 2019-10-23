package com.docker.rpc.async;

import chat.utils.TimerTaskEx;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by lick on 2019/9/25.
 * Description：
 */
public class AsyncRpcFuture {
    private String callbackFutureId;
    private Long crc;
    private CompletableFuture<?> future;
    private List<AsyncCallbackHandler> asyncCallbackHandlers;
    private TimerTaskEx timerTaskEx;

    public AsyncRpcFuture(Long crc) {
        this.callbackFutureId = ObjectId.get().toString();
        this.asyncCallbackHandlers = new ArrayList<>();
        this.crc = crc;
        this.future = new CompletableFuture<>();
    }

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

    public void addHandler(AsyncCallbackHandler asyncCallbackHandler) {
        if (!this.asyncCallbackHandlers.contains(asyncCallbackHandler)) {
            this.asyncCallbackHandlers.add(asyncCallbackHandler);
        }
    }

    public List<AsyncCallbackHandler> getAsyncCallbackHandlers() {
        return asyncCallbackHandlers;
    }

    public String getCallbackFutureId() {
        return callbackFutureId;
    }

    public void setCallbackFutureId(String callbackFutureId) {
        this.callbackFutureId = callbackFutureId;
    }

    public void handleAsyncHandler(Object result, List<String> exceptHandlerClass) {
        List<AsyncCallbackHandler> asyncCallbackHandlers = getAsyncCallbackHandlers();
        if (!asyncCallbackHandlers.isEmpty()) {
            for (AsyncCallbackHandler asyncCallbackHandler : asyncCallbackHandlers) {
                if (exceptHandlerClass != null && exceptHandlerClass.contains(asyncCallbackHandler.getClass().getSimpleName())) {
                    continue;
                }
                asyncCallbackHandler.setResult(result);
                asyncCallbackHandler.handle();
            }
        }
    }
}
