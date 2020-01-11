package com.docker.utils;

import org.springframework.beans.BeansException;

/**
 * Created by lick on 2019/12/25.
 * Description：
 */
public class GroovyCloudBean {
    public static final String DOCKERSTATUSSERVICE = "dockerStatusService";
    public static final String SERVICEVERSIONSERVICE = "serviceVersionService";
    public static final String IMEXTENSIONCACHE = "imExtensionCache";
    public static final String ONLINEUSERMANAGER = "onlineUserManager";
    public static final String MESSAGEEVENTHANDLER = "messageEventHandler";
    public static final String PROXYCONTAINERDUPLEXENDER = "proxyContainerDuplexSender";
    public static Object getBean(String name) throws BeansException {
        return SpringContextUtil.getBean(name);
    }
}