package com.docker.script.servlet;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.server.OnlineServer;
import com.google.common.collect.Lists;
import script.groovy.object.GroovyObjectEx;
import script.groovy.servlets.GroovyServlet;
import script.groovy.servlets.GroovyServletManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by aplomb on 17-6-27.
 */
public class GroovyServletManagerEx extends GroovyServletManager {
    private static final String TAG = GroovyServletManagerEx.class.getSimpleName();
    private static final String ROOT = "rest/";
    private static final String FORCEPREFIX = "@PREFIX";
    private static final String FORCEPREFIXSLASH = FORCEPREFIX + "/";
    public static final String BASE_TIMER = "timer";
    public static final String BASE_MEMORY = "memory";
    public static final String BASE_REPAIR = "repair";
    public static final String BASE_MEMORY_BASE = "base";//底层内存
    public static final String BASE_CROSSCLUSTERACCESSSERVICE = "crossClusterAccessService";//call service cross cluster
    public static final String BASE_CROSSCLUSTERCREATETOKEN = "crossClusterCreateToken";//call service cross cluster
    public static final String BASE_SCALE = "scale";//call service cross cluster
    private String service;
    private Integer serviceVersion;
    public GroovyServletManagerEx(String service, Integer serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }
    @Override
    public String getService() {
        return this.service;
    }
    @Override
    public Integer getServiceVersion() {
        return this.serviceVersion;
    }

    @Override
    public String getIp() {
        return OnlineServer.getInstance().getIp();
    }

    @Override
    public String handleUri(String uri, GroovyObjectEx<GroovyServlet> groovyServlet, Method method) {
        uri = super.handleUri(uri, groovyServlet, method);
        if(uri.startsWith(ROOT)) {
            if(uri.startsWith(service + "/", ROOT.length())) {
                return uri;
            }

            int pos = uri.indexOf("/", ROOT.length());
            String restUri;
            if(pos != -1) {
                restUri = uri.substring(pos);
            } else {
                restUri = "";
            }
            String newuri = ROOT + service + restUri;
            LoggerEx.warn(TAG, "Uri " + uri + " is not legal, force to " + newuri);
            return newuri;
        } else if(uri.equals(FORCEPREFIX)) {
            return ROOT + service;
        } else if(uri.startsWith(FORCEPREFIXSLASH)) {
            return ROOT + service + "/" + uri.substring(FORCEPREFIXSLASH.length());
        } else {
            LoggerEx.error(TAG, "Uri " + uri + " is illegal, please use " + FORCEPREFIX + " or rest/" + service + "/ in front of your url");
            RuntimeException e = new RuntimeException("Uri " + uri + " is illegal, please use " + FORCEPREFIX + " or rest/" + service + "/ in front of your url");
            try {
                List<StackTraceElement> list = Lists.asList(new StackTraceElement(groovyServlet.getGroovyClass().getName(), method.getName(), /*groovyServlet.getGroovyClass().getSimpleName() + ".groovy"*/groovyServlet.getGroovyPath(), 1), e.getStackTrace());
                StackTraceElement[] elements = new StackTraceElement[list.size()];
                list.toArray(elements);
                e.setStackTrace(elements);
                e.printStackTrace();
            } catch (CoreException e1) {
                e1.printStackTrace();
            }
//            throw e;
//            if(uri.trim().equals("")) {
//                return ROOT + service;
//            } else {
//                return ROOT + service + "/" + uri;
//            }
            return null;
        }

    }
}
