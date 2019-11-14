package com.docker.rpc.async;

public class AsyncRuntimeException extends RuntimeException {
    private int code;
    public AsyncRuntimeException(int code, String message){
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
