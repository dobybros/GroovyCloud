package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import script.groovy.servlets.Tracker;

public class Proxy {
    private static final String TAG = Proxy.class.getSimpleName();
    protected RemoteServiceDiscovery remoteServiceDiscovery;
    private ServiceStubManager serviceStubManager;

    public Proxy(RemoteServiceDiscovery remoteServiceDiscovery, ServiceStubManager serviceStubManager) {
//        LoggerEx.info(TAG, "new proxy with params RemoteServiceDiscovery: " + remoteServiceDiscovery + ", ServiceStubManager: " + serviceStubManager);
        this.remoteServiceDiscovery = remoteServiceDiscovery;
        this.serviceStubManager = serviceStubManager;
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
        RemoteServiceDiscovery.RemoteServers lanServers = remoteServiceDiscovery.getRemoteServers();//没有用了
        if (lanServers == null)
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "RemoteService " + remoteServiceDiscovery.getService() + " doesn't be found while invoke method " + crc);
            MethodResponse response = (MethodResponse) lanServers.call(request);
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