package com.docker.script.callers;

public abstract class RunnableCaller implements Runnable {
    private String TAG = RunnableCaller.class.getSimpleName();

    public abstract void call();

    @Override
    public final void run() {
        CallerUtils.callMethod(this, "call");
    }
}
