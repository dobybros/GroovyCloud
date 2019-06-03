package com.docker.rpc.method;

import chat.errors.CoreException;

/**
 * Created by aplombchen on 19/5/17.
 */
public class MethodReturn {
    private Object returnObject;
    private CoreException exception;

    public MethodReturn(Object returnObj, CoreException exception) {
        this.returnObject = returnObj;
        this.exception = exception;
    }

    public CoreException getException() {
        return exception;
    }

    public void setException(CoreException exception) {
        this.exception = exception;
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }
}
