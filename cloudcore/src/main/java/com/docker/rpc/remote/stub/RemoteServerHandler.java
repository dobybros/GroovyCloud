package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.rpc.MethodRequest;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.RPCClientAdapterMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServerHandler {
    private Boolean usePublicDomain;
    private RPCClientAdapterMap rpcClientAdapterMap;
    private Random random = new Random();
    private long touch;
    private RemoteServers remoteServers;
    private ServiceStubManager serviceStubManager;
    private String service;
    private final String TAG = RemoteServerHandler.class.getSimpleName();

    RemoteServerHandler(String service, ServiceStubManager serviceStubManager) throws CoreException {
        this.serviceStubManager = serviceStubManager;
        this.rpcClientAdapterMap = this.serviceStubManager.getClientAdapterMap();
        this.usePublicDomain = this.serviceStubManager.getUsePublicDomain();
        this.service = service;
        this.remoteServers = new RemoteServers();
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void calculate() {
        List<RemoteServers.Server> newSortedServers = new ArrayList<>();
        Collection<RemoteServers.Server> theServers = this.remoteServers.getServers().values();
        for (RemoteServers.Server server : theServers) {
            int score = 100;
            RPCClientAdapter clientAdapter = rpcClientAdapterMap.getClientAdapter(server.getServer());
            if (clientAdapter != null) {
//                    score += 10;
                if (!clientAdapter.isConnected()) {
                    score = -1;
                    continue;
                }
            }
            server.setScore(score);
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

    public MethodResponse call(MethodRequest request) throws CoreException {
        ConcurrentHashMap<String, RemoteServers.Server> servers = (ConcurrentHashMap<String, RemoteServers.Server>) this.serviceStubManager.getRemoteServersDiscovery().getServers(service);
        if (servers != null && servers.size() > 0) {
            this.remoteServers.setServers(servers);
            calculate();
        } else {
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "RemoteService " + service + " doesn't be found!");
        }
        touch();
        if (this.remoteServers.getSortedServers().isEmpty())
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "No server is found for service " + service + " fromService " + request.getFromService() + " crc " + request.getCrc());

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
                if (this.usePublicDomain) {
                    ip = server.getPublicDomain();
                } else {
                    ip = server.getIp();
                }
                Integer port = null;
                if (rpcClientAdapterMap.isEnableSsl()) {
                    port = server.getSslRpcPort();
                } else {
                    port = server.getRpcPort();
                }
                request.setService(service + "_v" + server.getVersion());
                if (ip != null && port != null) {
                    RPCClientAdapter clientAdapter = rpcClientAdapterMap.registerServer(ip, port, server.getServer());
                    MethodResponse response = (MethodResponse) clientAdapter.call(request);
                    if (response.getException() != null) {
                        LoggerEx.info(TAG, "Failed to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                        throw response.getException();
                    }
                    LoggerEx.info(TAG, "Successfully call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                    return response;
                } else {
                    LoggerEx.info(TAG, "No ip " + ip + " or port " + port + ", fail to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
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
                LoggerEx.error(TAG, "Fail to Call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount + " available size " + keptSortedServers.size() + " error " + t.getMessage() + " exception " + t);
            }
        }
        throw new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "Call request " + request + " outside failed with several retries.");
    }
}
