package script.groovy.servlets;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chat.logs.AnalyticsLogger;
import chat.main.ServerStart;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.servlets.GroovyServletManager.PermissionIntercepter;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import script.groovy.servlets.grayreleased.GrayReleased;
import script.memodb.ObjectId;

public class RequestHolder {
    private static final String TAG = RequestHolder.class.getSimpleName();
    private RequestURIWrapper requestUriWrapper;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HashMap<String, String> pathVariables;
    private GroovyObjectEx<RequestIntercepter> interceptor;
    private GroovyServletManager groovyServletManager;
    private ParameterHandler parameterHandler;
    private AsyncContext asyncContext;

    RequestHolder(RequestURIWrapper requestUriWrapper,
                  HttpServletRequest request, HttpServletResponse response,
                  HashMap<String, String> pathVariables, GroovyObjectEx<RequestIntercepter> interceptor, GroovyServletManager groovyServletManager) {
        this.requestUriWrapper = requestUriWrapper;
        this.request = request;
        this.response = response;
        this.pathVariables = pathVariables;
        this.interceptor = interceptor;
        this.groovyServletManager = groovyServletManager;
    }

    public interface ParameterHandler {
        public Object valueForParameter(Parameter parameter);
    }

    public String getResponseType() {
        if (requestUriWrapper != null)
            return requestUriWrapper.getResponseType();
        return null;
    }

    public String getPathVariable(String key) throws CoreException {
        return getPathVariable(key, true);
    }

    public String getPathVariable(String key, boolean required) throws CoreException {
        if (pathVariables == null)
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_VARIABLE_NULL, "PathVariable is null.");
            } else {
                return null;
            }
        return pathVariables.get(key);
    }

    public Set<String> variablesKeySet() {
        if (pathVariables == null)
            return null;
        return pathVariables.keySet();
    }

    public HashMap<String, String> getPathVariables() {
        return pathVariables;
    }

    public HttpServletResponse getResponse() {
        Boolean isAsync = isAysnc();
        if(isAsync != null && isAsync && asyncContext != null) {
            return (HttpServletResponse) asyncContext.getResponse();
        }
        return response;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public Integer getIntegerParam(String key) throws CoreException {
        return getIntegerParam(key, false);
    }

    public Integer getIntegerParam(String key, boolean required) throws CoreException {
        String para = request.getParameter(key);
        if (para != null) {
            try {
                return Integer.parseInt(para);
            } catch (Exception e) {
            }
            return null;
        } else {
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
            } else {
                return null;
            }
        }
    }

    public Double getDoubleParam(String key) throws CoreException {
        return getDoubleParam(key, false);
    }

    public Double getDoubleParam(String key, boolean required) throws CoreException {
        String para = request.getParameter(key);
        if (para != null) {
            try {
                return Double.parseDouble(para);
            } catch (Exception e) {
            }
            return null;
        } else {
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
            } else {
                return null;
            }
        }
    }

    public Boolean getBooleanParam(String key) throws CoreException {
        return getBooleanParam(key, false);
    }

    public Boolean getBooleanParam(String key, boolean required) throws CoreException {
        String para = request.getParameter(key);
        if (para != null) {
            try {
                return Boolean.parseBoolean(para);
            } catch (Exception e) {
            }
            return null;
        } else {
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
            } else {
                return null;
            }
        }
    }

    public Long getLongParam(String key) throws CoreException {
        return getLongParam(key, false);
    }

    public Long getLongParam(String key, boolean required) throws CoreException {
        String para = request.getParameter(key);
        if (para != null) {
            try {
                return Long.parseLong(para);
            } catch (Exception e) {
            }
            return null;
        } else {
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
            } else {
                return null;
            }
        }
    }

    public String getParam(String key) throws CoreException {
        return getParam(key, false);
    }

    public String getParam(String key, boolean required) throws CoreException {
        String para = request.getParameter(key);
        if (para != null) {
            return para;
        } else {
            if (required) {
                throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter " + key + " is required but null.");
            } else {
                return null;
            }
        }
    }
	
	/*
	public abstract class RequestInterceptor{
		protected void proceed() {
			invoke(servlet, groovyMethod);
		}
		public abstract void invoke(RequestHolder holder);
	}
	
	public class MyInterceptor extends RequestInterceptor{

		@Override
		public void invoke(RequestHolder holder) {
			super.proceed();
		}
		
	}
	*/

    public void handleRequest() throws CoreException {
        GroovyObjectEx<GroovyServlet> servletObj = requestUriWrapper.getGroovyObject();
        String groovyMethod = requestUriWrapper.getMethod();
        if (servletObj != null && groovyMethod != null) {
            if (interceptor != null) {
                GroovyRuntime groovyRuntime = interceptor.getGroovyRuntime();
                if (groovyRuntime != null) {
                    try {
                        GrayReleased grayReleased = new GrayReleased();
                        if (!StringUtils.isEmpty(GrayReleased.getCookieValue(request.getCookies()))) {
                            grayReleased.setType(GrayReleased.getCookieValue(request.getCookies()));
                        }
                        GrayReleased.grayReleasedThreadLocal.set(grayReleased);
                        RequestIntercepter theInterceptor = interceptor.getObject();
                        if (theInterceptor != null) {
                            if (isAysnc()) {
                                this.asyncContext = request.startAsync();
//                                asyncContext.addListener(new MyAsyncListener(request.getRequestURI()));
                                asyncContext.setTimeout(90000L);
                                response = (HttpServletResponse) asyncContext.getResponse();
                                ServerStart.getInstance().getCoreThreadPoolExecutor().execute(()->{
                                    GrayReleased.grayReleasedThreadLocal.set(grayReleased);
                                    theInterceptor.invokeInternal(this);
                                });
                            } else {
                                theInterceptor.invokeInternal(this);
                            }
                            return;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (e instanceof CoreException)
                            throw e;
                        throw new CoreException(ChatErrorCodes.ERROR_GROOVY_UNKNOWN, "Unknown error while executing controller intercepter " + servletObj.getGroovyPath() + " : " + e.getMessage());
                    }
                }
            }
//			servletObj.invokeRootMethod(groovyMethod, this);
            //TODO 没有做异步实现，不影响
            invokeMethod(groovyMethod, servletObj);
        } else {
            LoggerEx.error(TAG,
                    "Handle request failed by illegal paramenters, servlet "
                            + servletObj + " groovyMethod " + groovyMethod
                            + " for uri " + request.getRequestURI());
        }
    }

    public GroovyObjectEx<RequestIntercepter> getInterceptor() {
        return interceptor;
    }

    RequestURIWrapper getRequestUriWrapper() {
        return requestUriWrapper;
    }

    public Object invokeMethod(String groovyMethod,
                               GroovyObjectEx<GroovyServlet> servletObj) throws CoreException {
        //TODO annotation
        String parentTrackId = request.getHeader("X-Track-Id");
        String trackId = ObjectId.get().toString();
        Tracker tracker = new Tracker(trackId, parentTrackId);
        Tracker.trackerThreadLocal.set(tracker);
        long time = System.currentTimeMillis();
        long invokeTokes = -1;
        boolean error = false;
        StringBuilder builder = new StringBuilder();
        try {
            String remoteHost = request.getHeader("X-Real-IP");
            builder.append("$$url:: " + request.getRequestURI() + " $$host:: " + (remoteHost != null ? remoteHost : request.getRemoteHost()) + " $$method:: " + request.getMethod() + " $$parenttrackid:: " + parentTrackId + " $$currenttrackid:: " + trackId);
            builder.append(" $$service:: " + groovyServletManager.getService() + " $$serviceversion:: " + groovyServletManager.getServiceVersion());
//			builder.append(" args:: " + JSON.toJSONString(args));
            Object[] args = requestUriWrapper.getActualParameters(this);
            String[] permissions = requestUriWrapper.getPermissions();
            if (permissions != null && permissions.length > 0) {
                GroovyObjectEx<PermissionIntercepter> permissionIntecepter = groovyServletManager.getPermissionIntercepter();
                if (permissionIntecepter != null) {
                    permissionIntecepter.getObject().invoke(permissions, requestUriWrapper.getMethod(), request, response);
                }
//				return servletObj.invokeMethod(groovyMethod, args);
            }
            Object returnObj = servletObj.invokeMethod(groovyMethod, args);
            builder.append(" $$returnobj:: " + (returnObj != null ? JSON.toJSONString(returnObj) : returnObj));
            return returnObj;
        } catch (Throwable t) {
            error = true;
            builder.append(" $$error:: " + t.getClass() + " $$errorMsg:: " + t.getMessage());
            throw t;
        } finally {
            builder.append(" $$sdockerip:: " + groovyServletManager.getIp());
            invokeTokes = System.currentTimeMillis() - time;
            builder.append(" $$takes:: " + invokeTokes);
            Tracker.trackerThreadLocal.remove();
            if (error)
                AnalyticsLogger.error(TAG, builder.toString());
            else
                AnalyticsLogger.info(TAG, builder.toString());
        }
    }

    public GroovyServletManager getGroovyServletManager() {
        return groovyServletManager;
    }

    public void setGroovyServletManager(GroovyServletManager groovyServletManager) {
        this.groovyServletManager = groovyServletManager;
    }

    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }

    public void setParameterHandler(ParameterHandler parameterHandler) {
        this.parameterHandler = parameterHandler;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    public Boolean isAysnc() {
        return this.requestUriWrapper.getAsyncSupported();
    }
    public void complete(){
        Boolean isAsync = isAysnc();
        if(isAsync != null && isAsync)
            this.asyncContext.complete();
    }
    private class MyAsyncListener implements AsyncListener {
        private String uri;

        MyAsyncListener(String uri) {
            this.uri = uri;
        }

        @Override
        public void onComplete(AsyncEvent asyncEvent) throws IOException {
            LoggerEx.info(TAG, "Complete async request, uri: " + uri);
        }

        @Override
        public void onTimeout(AsyncEvent asyncEvent) throws IOException {
//            HttpServletResponse response = (HttpServletResponse) asyncEvent.getAsyncContext().getResponse();
//            response.setContentType("application/json");
//            response.getOutputStream().write(("Async request timeout, uri: " + uri).getBytes("utf-8"));
            asyncEvent.getAsyncContext().complete();
            LoggerEx.info(TAG, "Async request timeout, uri: " + uri);
        }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {
            LoggerEx.error(TAG, "Async request error, uri: " + uri);
            //Aplomb 8.29 asyncEvent.getAsyncContext().complete()?
        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            LoggerEx.info(TAG, "Async request start, uri: " + uri);
        }
    }
}
