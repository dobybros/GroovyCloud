package com.docker.tasks;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.data.RepairData;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.impl.RepairServiceImpl;
import com.docker.tasks.annotations.RepairTaskListener;
import com.docker.utils.GroovyCloudBean;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationGlobalHandler;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyRuntime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
public class RepairTaskHandler extends ClassAnnotationGlobalHandler {
    private final String TAG = RepairTaskHandler.class.getSimpleName();
    private Map<String, GroovyObjectEx> groovyObjectExMap = new ConcurrentHashMap<>();
    private RepairServiceImpl repairService = (RepairServiceImpl) GroovyCloudBean.getBean(GroovyCloudBean.REPAIRSERVICE);

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return RepairTaskListener.class;
    }

    @Override
    public void handleAnnotatedClassesInjectBean(GroovyRuntime groovyRuntime) {
        for (GroovyObjectEx groovyObjectEx : groovyObjectExMap.values()) {
            try {
                groovyObjectEx = ((GroovyBeanFactory) groovyRuntime.getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyObjectEx.getGroovyClass());
            }catch (CoreException e){
                LoggerEx.error(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, GroovyRuntime groovyRuntime) {
        LoggerEx.info(TAG, "I will add repair task");
        if (annotatedClassMap != null) {
            Collection<Class<?>> values = annotatedClassMap.values();

            for (Class<?> groovyClass : values) {
                RepairTaskListener repairTaskListener = groovyClass.getAnnotation(RepairTaskListener.class);
                if (repairTaskListener != null) {
                    String description = repairTaskListener.description();
                    String createTime = repairTaskListener.createTime();
                    String id = repairTaskListener.id();
                    int type = repairTaskListener.type();
                    GroovyObjectEx<?> groovyObj = ((GroovyBeanFactory) groovyRuntime.getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
//                    GroovyObjectEx existGroovyObj = groovyObjectExMap.get(id);
//                    boolean newGroovyPath = false;
//                    if(existGroovyObj != null){
//                        if (existGroovyObj.getGroovyPath().equals(groovyObj.getGroovyPath())) {
//                            newGroovyPath = true;
//                        } else {
//                            LoggerEx.error(TAG, "Repair task: " + groovyClass + " has been ignored because of duplicated id: " + id);
//                        }
//                    }else {
//                        newGroovyPath = true;
//                    }
//                    if(newGroovyPath){
                        groovyObjectExMap.put(id, groovyObj);
                        try {
                            RepairData repairData = repairService.getRepairData(id);
                            if (repairData == null) {
                                repairData = new RepairData(id, description, createTime, type, "null", "http://" + OnlineServer.getInstance().getIp() + ":" + OnlineServer.getInstance().getHttpPort());
                                repairData.setExecuteResult("null");
                                repairService.addRepairData(repairData);
                            } else {
                                repairData.setServerUri("http://" + OnlineServer.getInstance().getIp() + ":" + OnlineServer.getInstance().getHttpPort());
                                repairData.setCreateTime(createTime);
                                repairData.setDescription(description);
                                repairData.setType(type);
                                repairService.updateRepairData(repairData);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                            LoggerEx.error(TAG, "Add repairData error, errMsg:" + t.getMessage());
                        }
//                    }
                }
            }
        }
    }

    public Object execute(String id) throws CoreException {
        GroovyObjectEx groovyObjectEx = groovyObjectExMap.get(id);
        if (groovyObjectEx != null) {
            return groovyObjectEx.invokeRootMethod("repair");
        }
        return null;
    }

    public RepairServiceImpl getRepairService() {
        return repairService;
    }
}
