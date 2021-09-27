package com.docker.script.callers;

import chat.utils.TimerTaskEx;

public abstract class TimerTaskCaller extends TimerTaskEx {

    public abstract void call();
    @Override
    public final void execute() {
        CallerUtils.callMethod(this, "call");
    }
}
