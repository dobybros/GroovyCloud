package com.docker.script;

import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import chat.scheduled.QuartzFactory;
import chat.utils.ChatUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.docker.data.ScheduleTask;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.servlet.GroovyServletManagerEx;
import com.docker.storage.adapters.impl.ScheduledTaskServiceImpl;
import com.docker.tasks.RepairTaskHandler;
import com.docker.utils.GroovyCloudBean;
import com.docker.utils.JWTUtils;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.servlets.GroovyServletDispatcher;
import script.groovy.servlets.GroovyServletManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/base", asyncSupported = true)
public class GroovyServletScriptDispatcher extends HttpServlet {
    private static final String TAG = GroovyServletManager.class.getSimpleName();
    ScriptManager scriptManager = null;
    private ServiceStubManager serviceStubManager = null;
    private String key = "FSDdfFDWfR324fs98DSF*@#";

    public void handle(HttpServletRequest request, HttpServletResponse response) {
        scriptManager = (ScriptManager) GroovyCloudBean.getBean(GroovyCloudBean.SCRIPTMANAGER);
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
                        String serviceVersion = getServiceVersion(service);
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
                }else if(uriStrs[1].equals(GroovyServletManagerEx.BASE_REPAIR)){
                    if(internalFilter(request, result).getCode() == 1){
                        String repairId = uriStrs[2];
                        RepairTaskHandler repairTaskHandler = (RepairTaskHandler) GroovyCloudBean.getBean(GroovyCloudBean.REPAIRTASKHANDLER);
                        try {
                            Object resultObj = repairTaskHandler.execute(repairId);
                            if(resultObj != null){
                                if(resultObj instanceof String){
                                    result.setData(resultObj);
                                }else {
                                    result.setData(JSON.toJSONString(resultObj));
                                }
                            }
                        }catch (CoreException e){
                            e.printStackTrace();
                            result.setMsg(e.toString());
                        }
                    }
                }
            } else if (uriStrs[1].equals(GroovyServletManagerEx.BASE_SCALE)) {
                if (internalFilter(request, result).getCode() == 1) {
                    if (uriStrs.length > 3) {
                        JSONObject jsonObject = null;
                        String requestStr = IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
                        if (requestStr != null) {
                            jsonObject = JSON.parseObject(requestStr);
                        }
                        String service = uriStrs[2];
                        String serviceVersion = getServiceVersion(service);
                        if (serviceVersion != null) {
                            GroovyRuntime groovyRuntime = getGroovyRuntime(serviceVersion);
                            if (groovyRuntime != null) {
                                ServiceScaleHandler serviceScaleHandler = (ServiceScaleHandler) groovyRuntime.getClassAnnotationHandler(ServiceScaleHandler.class);
                                if (serviceScaleHandler != null) {
                                    String methodName = uriStrs[3];
                                    if (uriStrs.length > 4) {
                                        for (int i = 4; i < uriStrs.length; i++) {
                                            methodName += captureName(uriStrs[i]);
                                        }
                                    }
                                    Object resultObject = null;
                                    try {
                                        resultObject = serviceScaleHandler.invoke(methodName, jsonObject);
                                    } catch (CoreException e) {
                                        result.setCode(e.getCode());
                                        result.setMsg(e.getMessage());
                                    }
                                    if (result.getMsg() == null) {
                                        result.setData(resultObject);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (uriStrs[1].equals(GroovyServletManagerEx.BASE_CROSSCLUSTERACCESSSERVICE)) {
                String token = request.getHeader("crossClusterToken");
                if (token == null) {
                    LoggerEx.error(TAG, "Token is null when Cross-cluster!!!");
                    result.setMsg("Token is null when Cross-cluster!!!");
                }
                try {
                    JWTUtils.getClaims("crossClusterToken", token);
                } catch (Throwable e) {
                    LoggerEx.error(TAG, "Jwt expired or not found when Cross-cluster, please check!!!err: " + ExceptionUtils.getFullStackTrace(e));
                    result.setMsg("Jwt expired or not found when Cross-cluster, please check!!!");
                }
                if (result.getMsg() == null) {
                    String requestStr = IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
                    JSONObject params = JSON.parseObject(requestStr);
                    String serviceName = params.getString("service");
                    String className = params.getString("className");
                    String methodName = params.getString("methodName");
                    JSONArray args = params.getJSONArray("args");
                    if (StringUtils.isNotBlank(serviceName) && StringUtils.isNotBlank(className) && StringUtils.isNotBlank(methodName)) {
                        Object[] objects = null;
                        if (args != null) {
                            objects = new Object[args.size()];
                            int i = 0;
                            for (Object o : args) {
                                objects[i] = o;
                                i++;
                            }
                        }
                        try {
                            if(serviceStubManager == null){
                                serviceStubManager = new ServiceStubManager();
                            }
                            Object o = serviceStubManager.call(serviceName, className, methodName, objects);
                            if (o != null) {
                                result.setData(o);
                            }
                        } catch (Throwable t) {
                            LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(t));
                            throw t;
                        }
                    }
                }
            } else if (uriStrs[1].equals(GroovyServletManagerEx.BASE_CROSSCLUSTERCREATETOKEN)) {
                if (internalFilter(request, result).getCode() == 1) {
                    String token = JWTUtils.createToken("crossClusterToken", null, 10800000L);//3小时
                    result.setData(token);
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

    private String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    public Result internalFilter(HttpServletRequest request, Result result) {
        String internalToken = request.getHeader("key");
        if (StringUtils.isBlank(internalToken) || !internalToken.equals(key)) {
            LoggerEx.error(TAG, "Cant find key in header!!!");
            result.setCode(4001);
            result.setMsg("Cant find key in header!!!");
        }
        return result;
    }

    private String getServiceVersion(String service) {
        String serviceVersion = null;
        if (!service.contains("_v")) {
            Integer version = scriptManager.getDefalutServiceVersionMap().get(service);
            if (version != null) {
                serviceVersion = service + "_v" + version;
            } else {
                LoggerEx.error(TAG, "The service not load, version is null");
            }
        } else {
            serviceVersion = service;
        }
        return serviceVersion;
    }

    private List handlerService(String service) {
        GroovyRuntime groovyRuntime = getGroovyRuntime(service);
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
        return null;
    }

    private GroovyRuntime getGroovyRuntime(String serviceVersion) {
        GroovyServletManager groovyServletManager = GroovyServletDispatcher.getGroovyServletManagerEx(serviceVersion);
        if (groovyServletManager != null) {
            GroovyRuntime groovyRuntime = groovyServletManager.getGroovyRuntime();
            return groovyRuntime;
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
                    if (jobName.contains("OneTimeTask") || jobName.contains("PeriodicTask")) {
                        if (scheduledTaskService != null) {
                            ScheduleTask scheduleTask = scheduledTaskService.getSchedeuleTask(jobName);
                            if (scheduleTask != null) {
                                map.put("status", scheduleTask.getStatus() == null ? "null" : scheduleTask.getStatus().toString());
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

    private class CallServiceParams {
        private String service;
        private String className;
        private String methodName;
        private JSONArray args;

        private Boolean checkParamsNotNull() {
            return service != null && className != null && methodName != null;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public JSONArray getArgs() {
            return args;
        }

        public void setArgs(JSONArray args) {
            this.args = args;
        }
    }
}
