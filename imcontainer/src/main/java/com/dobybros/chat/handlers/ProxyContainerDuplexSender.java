package com.dobybros.chat.handlers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyRequest;
import com.dobybros.chat.rpc.reqres.balancer.IMProxyResponse;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMRequest;
import com.dobybros.chat.rpc.reqres.balancer.ProxyIMResponse;
import com.docker.annotations.ProxyContainerTransportType;
import com.docker.data.Service;
import com.docker.data.ServiceAnnotation;
import com.docker.rpc.RPCClientAdapter;
import com.docker.rpc.remote.stub.RemoteServers;
import com.docker.storage.adapters.impl.DockerStatusServiceImpl;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lick
 * @date 2019/11/15
 */
public class ProxyContainerDuplexSender {
    @Resource
    RpcProxyContainerDuplexSender rpcProxyContainerDuplexSender;
//    @Resource
//    QueueProxyContainerDuplexSender queueProxyContainerDuplexSender;
    @Resource
    DockerStatusServiceImpl dockerStatusService;
    private final String TAG = ProxyContainerDuplexSender.class.getSimpleName();
    //serviceTransportTypeMap
    private Map<String, Map<String, Integer>> serviceMap = new ConcurrentHashMap<>();

    private Integer select(String service, String contentType){
        Integer type = ProxyContainerTransportType.TYPE_RPC;
        if(service != null){
            if(contentType != null){
                Map<String, Integer> serviceTypeMap = serviceMap.get(service);
                if(serviceTypeMap != null){
                    if(serviceTypeMap.get(contentType) != null){
                        type = serviceTypeMap.get(contentType);
                    }
                }
            }
        }
        return type;
    }

    public ProxyIMResponse sendIM(ProxyIMRequest request, String contentType, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener){
        if(server != null && request != null && request.checkParamsNotNull()){
            switch (select(request.getService(), contentType)){
                case ProxyContainerTransportType.TYPE_RPC:
                    return rpcProxyContainerDuplexSender.sendIM(request, server, clientAdapterStatusListener);
                case ProxyContainerTransportType.TYPE_QUEUE:
//                    queueProxyContainerDuplexSender.sendIM(request, server);
                    break;
                default:
                    break;
            }
        }
        return null;
    }
    public IMProxyResponse sendProxy(IMProxyRequest request, String contentType, RemoteServers.Server server, RPCClientAdapter.ClientAdapterStatusListener clientAdapterStatusListener) throws CoreException{
        if(server != null && request != null && request.checkParamsNotNull()){
            switch (select(request.getService(), contentType)){
                case ProxyContainerTransportType.TYPE_RPC:
                    return rpcProxyContainerDuplexSender.sendProxy(request, server, clientAdapterStatusListener);
                case ProxyContainerTransportType.TYPE_QUEUE:
//                    queueProxyContainerDuplexSender.sendProxy(request, server);
                    break;
                default:
                    break;
            }
        }
        return null;
    }
    public void init(){
        List<String> annotaions = new ArrayList<>();
        annotaions.add(ProxyContainerTransportType.class.getSimpleName());
        TimerEx.schedule(new TimerTaskEx() {
            @Override
            public void execute() {
                try {
                    List<Service> services = dockerStatusService.getServiceAnnotation(annotaions, null);
                    for (Service service : services) {
                        String serviceName = service.getService();
                        Map<String, Integer> serviceTypeMap = null;
                        List<ServiceAnnotation> serviceAnnotations = service.getServiceAnnotations();
                        for (ServiceAnnotation serviceAnnotationDocument : serviceAnnotations) {
                            serviceTypeMap = new ConcurrentHashMap<>();
                            Map<String, Object> params = serviceAnnotationDocument.getAnnotationParams();
                            Integer type = (Integer) params.get("type");
                            List<String> contentTypes =  (List<String>) params.get("contentType");
                            if(type != null && contentTypes != null){
                                for (String contentType : contentTypes){
                                    Integer typeOld = serviceTypeMap.get(contentType);
                                    if(typeOld != null && typeOld != type){
                                        LoggerEx.error(TAG, "Service: " + serviceName + " contentType: " + contentType + "are defined different type");
                                    }
                                    serviceTypeMap.put(contentType, type);
                                }
                            }
                        }
                        if(serviceTypeMap != null && !serviceTypeMap.isEmpty()){
                            serviceMap.put(serviceName, serviceTypeMap);
                        }
                    }
                } catch (CoreException e) {
                    e.printStackTrace();
                    LoggerEx.error(TAG, "Get dockerstatus annotation err, errMsg: " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 10000L, 10000L);
    }
}
