package com.docker.rpc.remote;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.errors.CoreErrorCodes;
import com.docker.rpc.*;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.rpc.impl.RPCEntity;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.groovy.object.GroovyObjectEx;

import java.rmi.RemoteException;

/**
 * Created by lick on 2020/2/8.
 * Description：
 */
public class RpcRequestCall {
    private static volatile RpcRequestCall instance;
    private final String TAG = RpcRequestCall.class.getSimpleName();
    public byte[] call(RMIServerImplWrapper serverWrapper, String type, Byte encode, byte[] data) throws RemoteException{
        RPCResponse response = null;
        try {
            RPCRequest request = null;
            RPCServerAdapter serverAdapter = null;
            RPCEntity entity = null;
            if (MethodRequest.RPCTYPE.equals(type)) {
                if (serverWrapper.getServerMethodInvocation() == null)
                    serverWrapper.setServerMethodInvocation(new RPCServerMethodInvocation());
                request = new MethodRequest();

                request.setEncode(encode);
                request.setType(type);
                request.setData(data);
                request.resurrect();
                response = serverWrapper.getServerMethodInvocation().onCall((MethodRequest) request);
            } else {
                GroovyObjectEx<RPCServerAdapter> adapter = serverWrapper.getServerAdapterMap().get(type);
                if (adapter == null)
                    throw new CoreException(CoreErrorCodes.ERROR_RPC_TYPE_NOSERVERADAPTER, "No server adapter found by type " + type);

                entity = serverWrapper.getRmiServerHandler().getRPCEntityForServer(type, adapter.getGroovyClass());
                serverAdapter = adapter.getObject();
                request = entity.requestClass.newInstance();

                request.setEncode(encode);
                request.setType(type);
                request.setData(data);
                request.resurrect();
                response = serverAdapter.onCall(request);
            }
            if (response != null) {
                byte[] responseData = response.getData();
                if (responseData == null) {
                    if (response.getEncode() == null)
                        response.setEncode(RPCBase.ENCODE_PB);
                    response.persistent();
                }
                return response.getData();
            }
            return null;
        } catch (Throwable t) {
            LoggerEx.error(TAG, "RPC call type " + type + " occur error on server side, " + ExceptionUtils.getFullStackTrace(t));
            String message = null;
            if (t instanceof CoreException) {
                message = ((CoreException) t).getCode() + "|" + t.getMessage();
            } else {
                message = t.getMessage();
            }
            throw new RemoteException(message, t);
        }
    }
    public static synchronized RpcRequestCall getInstance(){
        if(instance == null){
            synchronized (RpcRequestCall.class){
                if(instance == null){
                    instance = new RpcRequestCall();
                }
            }
        }
        return instance;
    }
    public boolean containsType(String type){
        return type.equals("smsg") || type.equals("uol") || type.equals("proxyim") || type.equals("improxy") || type.equals("mthd");
    }
}
