package com.docker.utils;

import org.springframework.beans.BeansException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/12/25.
 * Description：
 */
public class GroovyCloudBean {
    private static Map<Class, String> classBeanNameMap = new ConcurrentHashMap<>();
    public static final String DOCKERSTATUSSERVICE = "dockerStatusService";
    public static final String SERVICEVERSIONSERVICE = "serviceVersionService";
    public static final String IMEXTENSIONCACHE = "imExtensionCache";
    public static final String ONLINEUSERMANAGER = "onlineUserManager";
    public static final String MESSAGEEVENTHANDLER = "messageEventHandler";
    public static final String PROXYCONTAINERDUPLEXENDER = "proxyContainerDuplexSender";
    public static final String REDISSUBSCRIBEHANDLER = "redisSubscribeHandler";
    public static final String SCRIPTMANAGER = "scriptManager";
    public static final String REPAIRSERVICE = "repairService";
    public static final String REPAIRTASKHANDLER = "repairTaskHandler";
    public static final String REDISLISTENERHANDLER = "redisListenerHandler";

    public static Object getBean(String name) throws BeansException {
        return SpringContextUtil.getBean(name);
    }
    public static Object getBean(Class claszz) throws BeansException{
        return SpringContextUtil.getBean(classBeanNameMap.get(claszz));
    }
    public static void map(){
        String[] beanNames = SpringContextUtil.getApplicationContext().getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            classBeanNameMap.put(getBean(beanNames[i]).getClass(), beanNames[i]);
        }
    }
}
