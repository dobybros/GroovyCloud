package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.*;
import com.docker.rpc.async.AsyncRpcFuture;
import com.docker.rpc.remote.MethodMapping;
import com.docker.server.OnlineServer;
import com.docker.utils.ScriptHttpUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * used for remote service invoke
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServerHandler {
    private Random random = new Random();
    private long touch;
    private RemoteServers remoteServers;
    private ServiceStubManager serviceStubManager;
    private String toService;
    private String callbackFutureId;
    private RPCClientAdapterMap thisRpcClientAdapterMap;
    private final String TAG = RemoteServerHandler.class.getSimpleName();

    RemoteServerHandler(String toService, ServiceStubManager serviceStubManager) {
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
            thisRpcClientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMapSsl();
        } else {
            thisRpcClientAdapterMap = RPCClientAdapterMapFactory.getInstance().getRpcClientAdapterMap();
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
                Integer port = null;
                if (thisRpcClientAdapterMap.isEnableSsl()) {
                    ip = server.getPublicDomain();
                    port = server.getSslRpcPort();
                } else {
                    ip = server.getIp();
                    port = server.getRpcPort();
                }
                request.setService(toService + "_v" + server.getVersion());
                request.setCallbackFutureId(this.callbackFutureId);
                if (OnlineServer.getInstance() != null) {
                    request.setFromServerName(OnlineServer.getInstance().getServer());
                    request.setSourceIp(OnlineServer.getInstance().getIp());
                    request.setSourcePort(Integer.valueOf(OnlineServer.getInstance().getRpcPort()));
                }
                if (ip != null && port != null) {
                    RPCClientAdapter clientAdapter = thisRpcClientAdapterMap.registerServer(ip, port, server.getServer());
                    clientAdapter.addToRemoteServerFutureList(callbackFutureId);
                    AsyncRpcFuture asyncRpcFuture = RpcCacheManager.getInstance().getAsyncRpcFuture(callbackFutureId);
                    asyncRpcFuture.setRemoteServers(server.getServer(), keptSortedServers, thisRpcClientAdapterMap, request);
                    clientAdapter.callAsync(request);
                    LoggerEx.info(TAG, "Successfully callAsync Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount);
                    if (asyncRpcFuture != null) {
                        return asyncRpcFuture.getFuture();
                    } else {
                        LoggerEx.error(TAG, "Call async method err,asyncRpcFuture is null, callbackFutureId" + callbackFutureId + ",CurrentThread: " + Thread.currentThread());
                    }
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
                if (ip != null && port != null) {
                    long time = System.currentTimeMillis();
                    RPCClientAdapter clientAdapter = thisRpcClientAdapterMap.registerServer(ip, port, server.getServer());
                    MethodResponse response = (MethodResponse) clientAdapter.call(request);
                    if (response.getException() != null) {
                        LoggerEx.error(TAG, "Failed to call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount);
                        throw response.getException();
                    }
                    LoggerEx.info(TAG, "Successfully call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException() + " on server " + server + " " + count + "/" + maxCount, System.currentTimeMillis() - time);
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
                LoggerEx.error(TAG, "Fail to Call Method " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " on server " + server + " " + count + "/" + maxCount + " available size " + keptSortedServers.size() + " error " + ExceptionUtils.getFullStackTrace(t) + " exception " + t);
            }
        }
        throw new CoreException(ChatErrorCodes.ERROR_RPC_CALLREMOTE_FAILED, "Call request " + request + " outside failed with several retries.");
    }

    private void setSortedServers(MethodRequest request) throws CoreException {
        ConcurrentHashMap<String, RemoteServers.Server> servers = (ConcurrentHashMap<String, RemoteServers.Server>) RemoteServersManager.getInstance().getServers(toService);
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

    public MethodResponse callHttp(MethodRequest request) throws CoreException {
        String token = RemoteServersManager.getInstance().getRemoteServerToken(this.serviceStubManager.getHost());
        if (token != null) {
            String serviceClassMethod = RpcCacheManager.getInstance().getMethodByCrc(request.getCrc());
            if (serviceClassMethod == null) {
                LoggerEx.error(TAG, "Cant find crc in RpcCacheManager, crc: " + request.getCrc());
                throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "Cant find crc in RpcCacheManager, crc: " + request.getCrc());
            }
            String[] serviceClassMethods = serviceClassMethod.split("_");
            if (serviceClassMethods.length == 3) {
                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("service", serviceClassMethods[0]);
                dataMap.put("className", serviceClassMethods[1]);
                dataMap.put("methodName", serviceClassMethods[2]);
                dataMap.put("args", request.getArgs());
                Map<String, Object> headerMap = new HashMap<String, Object>();
                headerMap.put("crossClusterToken", token);
                long time = System.currentTimeMillis();
                Result result = ScriptHttpUtils.post(JSON.toJSONString(dataMap), this.serviceStubManager.getHost() + "/base/crossClusterAccessService", headerMap, Result.class);
                if (result != null) {
                    LoggerEx.info(TAG, "Call remote server success, requestParams: " + dataMap.toString() + ",serverHost: " + this.serviceStubManager.getHost(), System.currentTimeMillis() - time);
                    MethodResponse response = new MethodResponse();
                    MethodMapping methodMapping = request.getServiceStubManager().getMethodMapping(request.getCrc());
                    if (methodMapping == null || methodMapping.getReturnClass().equals(Object.class)) {
                        response.setReturnObject(JSON.parse(JSON.toJSONString(result.getData())));
                    } else {
                        response.setReturnObject(JSON.parseObject(JSON.toJSONString(result.getData()), methodMapping.getGenericReturnClass()));
                    }
                    return response;
                }
//                else {
//                    throw new CoreException(ChatErrorCodes.ERROR_REMOTE_RPC_FAILED, "Call remote rpc failed, requestParams: " + dataMap.toString());
//                }
            }
        } else {
            LoggerEx.error(TAG, "Remote server is unavailabe, host: " + this.serviceStubManager.getHost());
            throw new CoreException(ChatErrorCodes.ERROR_SERVER_CONNECT_FAILED, "Remote server is unavailabe, host: " + this.serviceStubManager.getHost());
        }
        return null;
    }
}
