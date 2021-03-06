package script.groovy.servlets;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(urlPatterns = "/", asyncSupported = true)
public class GroovyServletDispatcher extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 7785123445517306608L;
    private static final String TAG = GroovyServletManager.class.getSimpleName();
    private static GroovyServletManager groovyServletManager = null;

    private static ConcurrentHashMap<String, GroovyServletManager> groovyServletMap = new ConcurrentHashMap<>();

    public static void setDefaultGroovyServletManager(GroovyServletManager servletManager) {
        groovyServletManager = servletManager;
    }

    public static void addGroovyServletManagerEx(String rootPath, GroovyServletManager servletManager) {
        GroovyServletManager oldGroovyServletMap = groovyServletMap.put(rootPath, servletManager);
        //TODO whether need clear memory for oldGroovyServletMap
    }

    public static GroovyServletManager getGroovyServletManagerEx(String rootPath) {
        return groovyServletMap.get(rootPath);
    }

    public static void removeGroovyServletManagerEx(String rootPath) {
        groovyServletMap.remove(rootPath);
    }

    public void servletDispatch(HttpServletRequest request, HttpServletResponse response) {
        long time = System.currentTimeMillis();
        try {
            RequestHolder holder = null;
            String uri = request.getRequestURI();
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            String[] uriStrs = uri.split("/");
            String matchStr = uriStrs.length > 1 ? uriStrs[1] : null;
            if (matchStr != null) {
                GroovyServletManager servletManagerEx = groovyServletMap.get(matchStr);
                if (servletManagerEx != null) {
                    int index = matchStr.lastIndexOf("_v");
                    if (index > 0)
                        uriStrs[1] = matchStr.substring(0, index);
                    holder = servletManagerEx.parseUri(request, response, uriStrs);
                }
            }
            if (holder == null) {
                if (groovyServletManager != null) {
                    holder = groovyServletManager.parseUri(request, response, uriStrs);
                } else {
                    LoggerEx.error(TAG, "No handler for uri " + request.getRequestURI() + " method " + request.getMethod() + " from " + request.getRemoteAddr());
                }
            }
            if (holder == null) {
                try {
                    response.sendError(404, "Url not found");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                holder.handleRequest();
            }
            LoggerEx.info(TAG, "RequestURI " + uri + " method " + request.getMethod() + " from " + request.getRemoteAddr(), System.currentTimeMillis() - time);
        } catch (Throwable e) {
            boolean moveServer = false;
            if(e instanceof CoreException){
                if(((CoreException) e).getCode() == ChatErrorCodes.ERROR_GROOVYSERVLET_SERVLET_NOT_INITIALIZED){
                    try {
                        response.sendError(504, e.getMessage());
                        moveServer = true;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            LoggerEx.error(TAG, "Request url " + request.getRequestURL().toString() + " occur error " + ExceptionUtils.getFullStackTrace(e));
            if(!moveServer){
                try {
                    response.sendError(500, e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }

    @Override
    public void doTrace(HttpServletRequest request, HttpServletResponse response) {
        servletDispatch(request, response);
    }
}
