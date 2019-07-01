package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.script.BaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;

import java.util.Arrays;

/**
 * Created by aplomb on 17-5-16.
 */
public class RPCServerMethodInvocation implements RPCServerAdapter<MethodRequest, MethodResponse> {

    private static final String TAG = RPCServerMethodInvocation.class.getSimpleName();

    @Override
    public MethodResponse onCall(MethodRequest request) throws CoreException {
        Long crc = request.getCrc();
//        if(crc == 0 || crc == -1)
//            throw new CoreException(CoreErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest");
        String service = request.getService();
        if(service == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NULL, "Service is null for crc " + crc);

        ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
        BaseRuntime baseRuntime = scriptManager.getBaseRuntime(service);
        if(baseRuntime == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + service + " not found for crc " + crc);
        ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = (ServiceSkeletonAnnotationHandler) baseRuntime.getClassAnnotationHandler(ServiceSkeletonAnnotationHandler.class);
        if(serviceSkeletonAnnotationHandler == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SKELETON_NULL, "Skeleton handler is not for service " + service + " on method crc " + crc);
        ServiceSkeletonAnnotationHandler.SkelectonMethodMapping methodMapping = serviceSkeletonAnnotationHandler.getMethodMapping(crc);
        if(methodMapping == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);
//        if(methodMapping == null)
//            throw new CoreException(CoreErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);
        MethodResponse response = methodMapping.invoke(request);
        LoggerEx.info(TAG, "Successfully call Method " + methodMapping.getMethod().getName() + "#" + methodMapping.getRemoteService().getGroovyClass().getSimpleName() + " crc " + request.getCrc() + "#" + request.getService() + " args " + Arrays.toString(request.getArgs()) + " return " + response.getReturnObject() + " exception " + response.getException());
        return response;
    }
}
