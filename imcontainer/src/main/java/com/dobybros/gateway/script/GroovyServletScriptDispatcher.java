package com.dobybros.gateway.script;

import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import com.dobybros.gateway.onlineusers.OnlineUserManager;
import com.docker.data.DockerStatus;
import com.docker.script.servlet.GroovyServletManagerEx;
import com.docker.server.OnlineServer;
import com.docker.utils.GroovyCloudBean;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by lick on 2020/6/5.
 * Description：
 */
@WebServlet(urlPatterns = "/base", asyncSupported = true)
public class GroovyServletScriptDispatcher extends com.docker.script.GroovyServletScriptDispatcher {
    private final String TAG = GroovyServletScriptDispatcher.class.getSimpleName();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        super.handle(request, response);
        try {
            String uri = request.getRequestURI();
            LoggerEx.info(TAG, "RequestURI " + uri + " method " + request.getMethod() + " from " + request.getRemoteAddr());
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            String[] uriStrs = uri.split("/");
            if (uriStrs.length == 3) {
                if (uriStrs[1].equals(GroovyServletManagerEx.BASE_GATEWAY)) {
                    Result theResult = new Result();
                    theResult.setCode(1);
                    if (uriStrs[2].equals(GroovyServletManagerEx.BASE_PARAMS)) {
                        if (OnlineServer.getInstance().getType() == DockerStatus.TYPE_GATEWAY) {
                            OnlineUserManager onlineUserManager = (OnlineUserManager) GroovyCloudBean.getBean(GroovyCloudBean.ONLINEUSERMANAGER);
                            Map onlineUserMap = onlineUserManager.getOnlineUsersHolder().getOnlineUserMap();
                            if (!onlineUserMap.isEmpty()) {
                                theResult.setData(onlineUserMap.keySet());
                            }
                            respond(response, theResult);
                        } else if (OnlineServer.getInstance().getType() == DockerStatus.TYPE_PROXY) {
                            String serviceVersion = getServiceVersion("improxy");
                            if (serviceVersion != null) {
                                List<Map> list = handlerService(serviceVersion);
                                if (list != null && !list.isEmpty()) {
                                    Map proxyUserMap = (Map) list.get(0).get("proxyUserMap");
                                    if (proxyUserMap != null && !proxyUserMap.isEmpty()) {
                                        LoggerEx.info(TAG, "Boolean setData");
                                        theResult.setData(proxyUserMap.keySet());
                                    }
                                }
                            } else {
                                throw new CoreException(500, "Version is null, service: " + "improxy");
                            }
                            respond(response, theResult);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Request url " + request.getRequestURL().toString() + " occur error " + ExceptionUtils.getFullStackTrace(e));
            try {
                response.sendError(500, e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
