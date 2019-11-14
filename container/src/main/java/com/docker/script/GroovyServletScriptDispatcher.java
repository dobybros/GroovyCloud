package com.docker.script;

import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import chat.scheduled.QuartzFactory;
import chat.utils.ChatUtils;
import com.alibaba.fastjson.JSON;
import com.docker.data.ScheduleTask;
import com.docker.script.servlet.GroovyServletManagerEx;
import com.docker.storage.adapters.impl.ScheduledTaskServiceImpl;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.servlets.GroovyServletDispatcher;
import script.groovy.servlets.GroovyServletManager;
import script.groovy.servlets.RequestHolder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(urlPatterns = "/base", asyncSupported = true)
public class GroovyServletScriptDispatcher extends HttpServlet {
    private static final String TAG = GroovyServletManager.class.getSimpleName();

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        try {
            String uri = request.getRequestURI();
            LoggerEx.info(TAG, "RequestURI " + uri + " method " + request.getMethod() + " from " + request.getRemoteAddr());
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            String[] uriStrs = uri.split("/");
            Result result = new Result();
            result.setCode(1);
            if (uriStrs.length == 3) {
                if (uriStrs[1].equals(GroovyServletManagerEx.BASE_MEMORY)) {
                    if (uriStrs[2].equals(GroovyServletManagerEx.BASE_MEMORY_BASE)) {
                        //get base memory
                    } else {
                        String service = uriStrs[2];
                        String serviceVersion = null;
                        if (!service.contains("_v")) {
                            ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                            Integer version = scriptManager.getDefalutServiceVersionMap().get(service);
                            if (version != null) {
                                serviceVersion = service + "_v" + version;
                            } else {
                                LoggerEx.error(TAG, "The service not load, version is null");
                            }
                        } else {
                            serviceVersion = service;
                        }
                        if (serviceVersion != null) {
                            List list = handlerService(serviceVersion);
                            result.setData(list);
                        } else {
                            throw new CoreException(500, "Version is null, service: " + service);
                        }
                    }
                } else if (uriStrs[1].equals(GroovyServletManagerEx.BASE_TIMER)) {
                    List list = handleTimer();
                    result.setData(list);
                }
            }
            respond(response, result);
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

    private List handlerService(String service) {
        GroovyServletManager groovyServletManager = GroovyServletDispatcher.getGroovyServletManagerEx(service);
        if (groovyServletManager != null) {
            GroovyRuntime groovyRuntime = groovyServletManager.getGroovyRuntime();
            if (groovyRuntime != null) {
                ServiceMemoryHandler classAnnotationHandler = (ServiceMemoryHandler) groovyRuntime.getClassAnnotationHandler(ServiceMemoryHandler.class);
                if (classAnnotationHandler != null) {
                    List list = classAnnotationHandler.getMemory();
                    if (list == null) {
                        list = new ArrayList();
                    }
                    Map<String, Object> baserunTimeMap = ((BaseRuntime) groovyRuntime).getMemoryCache();
                    if (!baserunTimeMap.isEmpty()) {
                        for (String key : baserunTimeMap.keySet()) {
                            list.add(baserunTimeMap.get(key));
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    private List handleTimer() {
        try {
            Scheduler scheduler = QuartzFactory.getInstance().getSchedulerFactory().getScheduler();
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            Map<String, String> map = null;
            ScheduledTaskServiceImpl scheduledTaskService = (ScheduledTaskServiceImpl) SpringContextUtil.getBean("scheduledTaskService");
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();
                    map = new HashMap<String, String>();
                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    String nextFireTime = "null";
                    if (triggers.get(0).getNextFireTime() != null) {
                        nextFireTime = ChatUtils.dateString(triggers.get(0).getNextFireTime());
                    }
                    String prefireTime = "null";
                    if (triggers.get(0).getPreviousFireTime() != null) {
                        prefireTime = ChatUtils.dateString(triggers.get(0).getPreviousFireTime());
                    }
                    map.put("taskId", jobName);
                    if(jobName.contains("OneTimeTask") || jobName.contains("PeriodicTask")){
                        if(scheduledTaskService != null){
                            ScheduleTask scheduleTask = scheduledTaskService.getSchedeuleTask(jobName);
                            if(scheduleTask != null){
                                map.put("status", scheduleTask.getStatus()  == null ? "null" : scheduleTask.getStatus().toString());
                                map.put("reason", scheduleTask.getReason() == null ? "null" : scheduleTask.getReason());
                            }
                        }
                    }
                    map.put("nextFireTime", nextFireTime);
                    map.put("preFireTime", prefireTime);
                    TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
                    map.put("state", scheduler.getTriggerState(triggerKey).toString());
                    list.add(map);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void respond(HttpServletResponse response, Object result) throws Throwable {
        String returnStr = JSON.toJSONString(result);
        response.setContentType("application/json");
        response.getOutputStream().write(returnStr.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }
}