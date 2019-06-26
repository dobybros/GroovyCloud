package com.docker.script.callers;

import chat.utils.TimerTaskEx;

public abstract class TimerTaskCaller extends TimerTaskEx {
    private static final String TAG = TimerTaskCaller.class.getSimpleName();

    public abstract void call();
    @Override
    public final void execute() {
        CallerUtils.callMethod(this, "call");
    }
}
