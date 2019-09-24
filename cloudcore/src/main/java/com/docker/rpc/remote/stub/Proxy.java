package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.remote.MethodMapping;
import script.groovy.servlets.Tracker;

public class Proxy {
    private static final String TAG = Proxy.class.getSimpleName();
    protected RemoteServerHandler remoteServerHandler;
    private ServiceStubManager serviceStubManager;
    private RPCInvocationHandler invocationHandler;

    public Proxy(RemoteServerHandler remoteServerHandler, ServiceStubManager serviceStubManager) {
        this.remoteServerHandler = remoteServerHandler;
        this.serviceStubManager = serviceStubManager;
        invocationHandler = new RPCInvocationHandlerImpl(remoteServerHandler);
    }

    //远程service调用
    public Object invoke(Long crc, Object[] args) throws Throwable {
        // TODO Auto-generated method stub
        MethodRequest request = new MethodRequest();
        request.setEncode(MethodRequest.ENCODE_JAVABINARY);
        request.setArgs(args);
        //TODO should consider how to optimize get CRC too often.
        Tracker tracker = Tracker.trackerThreadLocal.get();
        request.setTrackId(tracker == null ? null : tracker.getTrackId());
        request.setCrc(crc);
        request.setServiceStubManager(serviceStubManager);
        request.setFromService(serviceStubManager.getFromService());
        MethodMapping methodMapping = serviceStubManager.getMethodMapping(crc);
        return invocationHandler.invoke(methodMapping, request);
    }

    public static Object getReturnObject(MethodRequest request, MethodResponse response) throws CoreException {
        if (response != null) {
            CoreException e = response.getException();
            if (e != null) {
                throw e;
            }
            Object returnObject = response.getReturnObject();
            return returnObject;
        }
        throw new CoreException(ChatErrorCodes.ERROR_METHODRESPONSE_NULL, "Method response is null for request " + request);
    }
}