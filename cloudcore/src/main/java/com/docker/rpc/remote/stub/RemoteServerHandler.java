package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.server.OnlineServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * used for remote service invoke
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServerHandler {
    @Autowired
    RemoteServersManager remoteServersManager;
    @Autowired
    RPCClientAdapterMap rpcClientAdapterMap;
    @Autowired
    RPCClientAdapterMap rpcClientAdapterMapSsl;
    @Autowired
    RpcCacheManager rpcCacheManager;
    private Random random = new Random();
    private long touch;
    private RemoteServers remoteServers;
    private ServiceStubManager serviceStubManager;
    private String toService;
    private String callbackFutureId;
    private RPCClientAdapterMap thisRpcClientAdapterMap;
    private final String TAG = RemoteServerHandler.class.getSimpleName();

    RemoteServerHandler(String toService, ServiceStubManager serviceStubManager){
        this.toService = toService;
        this.serviceStubManager = serviceStubManager;
        this.remoteServers = new RemoteServers();
    }

    public String getToService() {
        return toService;
    }

    public String getCallbackFutureId() {
        return callbackFutureId;
    }

    public void setCallbackFutureId(String callbackFutureId) {
        this.callbackFutureId = callbackFutureId;
    }

    private void available() {
        if (this.serviceStubManager.getUsePublicDomain()) {
            thisRpcClientAdapterMap = rpcClientAdapterMapSsl;
        } else {
            thisRpcClientAdapterMap = rpcClientAdapterMap;
        }
        List<RemoteServers.Server> newSortedServers = new ArrayList<>();
        Collection<RemoteServers.Server> theServers = this.remoteServers.getServers().values();
        for (RemoteServers.Server server : theServers) {
            RPCClientAdapter clientAdapter = thisRpcClientAdapterMap.getClientAdapter(server.getServer());
            if (clientAdapter != null) {
                if (!clientAdapter.isConnected()) {
                    continue;
                }
            }
            newSortedServers.add(server);
        }
        this.remoteServers.setSortedServers(newSortedServers);
    }

    public class RandomDraw {
        private int[] array;

        public RandomDraw(int count) {
            array = new int[count];
            for (int i = 0; i < count; i++)
                array[i] = i;
        }

        public int next() {
            if (array.length <= 0)
                return -1;
            int index = random.nextInt(array.length);
            int value = array[index];
            int[] newArray = new int[array.length - 1];
            if (index == 0) {
                System.arraycopy(array, 1, newArray, 0, newArray.length);
            } else if (index == array.length - 1) {
                System.arraycopy(array, 0, newArray, 0, newArray.length);
            } else {
                System.arraycopy(array, 0, newArray, 0, index);
                System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
            }
            array = newArray;
            return value;
        }
    }

    public void touch() {
        touch = System.currentTimeMillis();
    }

    public CompletableFuture<?> callAsync(MethodRequest request) throws CoreException {
        String id = ObjectId.get().toString();
        setSortedServers(request);
        List<RemoteServers.Server> keptSortedServers = this.remoteServers.getSortedServers();
        int count = 0;
        int maxCount = 5;
        int size = keptSortedServers.size();
        maxCount = size < 5 ? size : 5;
        RandomDraw randomDraw = new RandomDraw(size);
        for (int i = 0; i < maxCount; i++) {
            int index = randomDraw.next();
            if (index == -1)
                continue;
            RemoteServers.Server server = keptSortedServers.get(index);
            if (server == null)
                continue;
            if (count++ > maxCount)
                break;
            try {
                String ip = null;
                if (this.serviceStubManager.getUsePublicDomain()) {
                    ip = server.getPublicDomain();
                } else {
                    ip = server.getIp();
                }
                Integer port = null;
                if (thisRpcClientAdapterMap.isEnableSsl()) {
                    port = server.getSslRpcPort();
                } else {
                    port = server.getRpcPort();
                }
                request.setService(toService + "_v" + server.getVersion());
                request.setCallbackFutureId(this.callbackFutureId);
                if (OnlineServer.getInstance() != null) {
                    request.setFromServerName(OnlineServer.getInstance().getServer());
                    request.setSourceIp(OnlineServer.getInstance().getIp());
                    request.setSourcePort(Integer.valueOf(OnlineServer.getInstance().getRpcPort()));
                }
                LoggerEx.info(TAG, "The service: " + toService + " ,the version being used is " + server.getVersion());
                if (ip != null && port != null) {
                    RPCClientAdapter clientAdapter = thisRpcClientAdapterMap.registerServer(ip, port, server.getServer());
                    clientAdapter.callAsync(request);
                    LoggerEx.info(TAG, "Successfully callAsync Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
                    return rpcCacheManager.getAsyncRpcFuture(callbackFutureId).getFuture();
                } else {
                    LoggerEx.info(TAG, "No ip " + ip + " or port " + port + ", fail to callSync Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
                }
            } catch (Throwable t) {
                if (t instanceof CoreException) {
                    CoreException ce = (CoreException) t;
                    switch (ce.getCode()) {
                        case ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED:
                        case ChatErrorCodes.ERROR_RPC_DISCONNECTED:
                            break;
                        default:
                            throw t;
                    }
                }
                LoggerEx.error(TAG, "Fail to callAsync Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount + " available size " + keptSortedServers.size() + " error " + ExceptionUtils.getFullStackTrace(t) + " exception " + t);
            }
        }
        throw new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "CallAsync request " + request + " outside failed with several retries.");
    }

    public MethodResponse call(MethodRequest request) throws CoreException {
        setSortedServers(request);
        List<RemoteServers.Server> keptSortedServers = this.remoteServers.getSortedServers();
        int count = 0;
        int maxCount = 5;
        int size = keptSortedServers.size();
        maxCount = size < 5 ? size : 5;
        RandomDraw randomDraw = new RandomDraw(size);
        for (int i = 0; i < maxCount; i++) {
            int index = randomDraw.next();
            if (index == -1)
                continue;
            RemoteServers.Server server = keptSortedServers.get(index);
            if (server == null)
                continue;
            if (count++ > maxCount)
                break;
            try {
                String ip = null;
                if (this.serviceStubManager.getUsePublicDomain()) {
                    ip = server.getPublicDomain();
                } else {
                    ip = server.getIp();
                }
                Integer port = null;
                if (thisRpcClientAdapterMap.isEnableSsl()) {
                    port = server.getSslRpcPort();
                } else {
                    port = server.getRpcPort();
                }
                request.setService(toService + "_v" + server.getVersion());
                if (OnlineServer.getInstance() != null) {
                    request.setFromServerName(OnlineServer.getInstance().getServer());
                    request.setSourceIp(OnlineServer.getInstance().getIp());
                    request.setSourcePort(Integer.valueOf(OnlineServer.getInstance().getRpcPort()));
                }
                LoggerEx.info(TAG, "The service: " + toService + " ,the version being used is " + server.getVersion());
                if (ip != null && port != null) {
                    RPCClientAdapter clientAdapter = thisRpcClientAdapterMap.registerServer(ip, port, server.getServer());
                    MethodResponse response = (MethodResponse) clientAdapter.call(request);
                    if (response.getException() != null) {
                        LoggerEx.error(TAG, "Failed to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                        throw response.getException();
                    }
                    LoggerEx.info(TAG, "Successfully call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                    return response;
                } else {
                    LoggerEx.info(TAG, "No ip " + ip + " or port " + port + ", fail to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                if (t instanceof CoreException) {
                    CoreException ce = (CoreException) t;
                    switch (ce.getCode()) {
                        case ChatErrorCodes.ERROR_RMICALL_CONNECT_FAILED:
                        case ChatErrorCodes.ERROR_RPC_DISCONNECTED:
                            break;
                        default:
                            throw t;
                    }
                }
                LoggerEx.error(TAG, "Fail to Call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount + " available size " + keptSortedServers.size() + " error " +ExceptionUtils.getFullStackTrace(t) + " exception " + t);
            }
        }
        throw new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "Call request " + request + " outside failed with several retries.");
    }

    private void setSortedServers(MethodRequest request) throws CoreException {
        ConcurrentHashMap<String, RemoteServers.Server> servers = (ConcurrentHashMap<String, RemoteServers.Server>) remoteServersManager.getServers(toService, this.serviceStubManager.getHost());
        if (servers != null && servers.size() > 0) {
            this.remoteServers.setServers(servers);
            //TODO Calculate everytime will slow down performance too.
            available();
        } else {
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "RemoteServers doesn't be found! service:" + toService);
        }
        touch();
        if (this.remoteServers.getSortedServers().isEmpty())
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "No server is found for service " + toService + " fromService " + request.getFromService() + " crc " + request.getCrc());
    }
}
