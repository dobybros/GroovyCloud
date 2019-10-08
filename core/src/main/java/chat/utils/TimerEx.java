package chat.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.scheduled.QuartzFactory;
import chat.thread.CloudThreadFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TimerEx {
    private static final String TAG = TimerEx.class.getSimpleName();
    public static void schedule(TimerTaskEx task, Long delay) {
        try {
            if (delay != null) {
                task.setDelay(delay);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            QuartzFactory.getInstance().addJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static void schedule(TimerTaskEx task, Long delay, Long period) {
        try {
            if (delay != null) {
                task.setDelay(delay);
            }
            if (period != null) {
                task.setPeriod(period);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            QuartzFactory.getInstance().addJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule Period TimerTask " + task + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }
    public static void schedule(TimerTaskEx task, String cron) {
        try {
            if (cron != null) {
                task.setCron(cron);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            QuartzFactory.getInstance().addCronJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + ExceptionUtils.getFullStackTrace(e));
        }
    }
    //传参数即可
    public static void schedule(TimerTaskEx task){
        if(task.getId() != null){
            if(task.getCron() != null){
                schedule(task, task.getCron());
            }else {
                if(task.getScheduleTime() != null){
                    try {
                        QuartzFactory.getInstance().addJobByScheduletime(task);
                    } catch (CoreException e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + ExceptionUtils.getFullStackTrace(e));
                    }
                }else {
                    schedule(task, task.getDelay(), task.getPeriod());
                }
            }
        }
    }
    public static void cancel(TimerTaskEx task) {
        try {
            if(task.getId() != null){
                QuartzFactory.getInstance().removeJob(task.getId());
            }
        }catch (Throwable e){
            e.printStackTrace();
            LoggerEx.error(TAG, "Remove timetask filed, taskId: " + task.getId() + ",e: " + ExceptionUtils.getFullStackTrace(e));
        }
    }
}