package chat.scheduled;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.QuartzJob;
import chat.utils.TimerTaskEx;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * Created by lick on 2019/4/20.
 * Description：
 */
public class QuartzFactory {
    private static final String TAG = QuartzFactory.class.getSimpleName();
    private SchedulerFactory schedulerFactory;
    private static QuartzFactory instance;

    public void addJob(TimerTaskEx task) throws CoreException{
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(task.getId(), task.getId()).build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("TimerTaskEx", task);
            Trigger trigger = null;
            if (task.getPeriod() != null) {
                if (task.getDelay() == null) {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .repeatForever()
                                    .withIntervalInMilliseconds(task.getPeriod()))
                            .build();
                } else {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .startAt(new Date(System.currentTimeMillis() + task.getDelay()))
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .repeatForever()
                                    .withIntervalInMilliseconds(task.getPeriod()))
                            .build();
                }
            } else {
                if (task.getDelay() == null) {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .build();
                } else {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .startAt(new Date(System.currentTimeMillis() + task.getDelay()))
                            .build();
                }
            }

            if (trigger != null) {
                // 调度容器设置JobDetail和Trigger
                sched.scheduleJob(jobDetail, trigger);

                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                }
            }
        } catch (Throwable e) {
            LoggerEx.error(TAG, "Add quartz job failed, type: " + task.getId() + ",e: " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_QUARTZ_ADDFAILED, "Add quartz job failed, type: " + task.getId() + ",e: " + e.getMessage());
        }
    }

    public void addJobByScheduletime(TimerTaskEx task) throws CoreException{
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(task.getId(), task.getId()).build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("TimerTaskEx", task);
            Trigger trigger = null;
            if ((task.getScheduleTime() < System.currentTimeMillis()) && (task.getScheduleTime() + 5000 > System.currentTimeMillis())) {
                //作业的触发器
                if (task.getPeriod() != null) {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .repeatForever()
                                    .withIntervalInMilliseconds(task.getPeriod()))
                            .build();
                } else {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .build();
                }
            } else {
                //作业的触发器
                if (task.getPeriod() != null) {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .startAt(new Date(task.getScheduleTime()))
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .repeatForever()
                                    .withIntervalInMilliseconds(task.getPeriod()))
                            .build();
                } else {
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(task.getId(), task.getId())
                            .startAt(new Date(task.getScheduleTime()))
                            .build();
                }
            }
            if (trigger != null) {
                // 调度容器设置JobDetail和Trigger
                sched.scheduleJob(jobDetail, trigger);

                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                    LoggerEx.info(QuartzFactory.class.getSimpleName(), "The period task " + task.getId() + " add successful");
                }
            }
        } catch (Throwable e) {
            LoggerEx.error(TAG, "Add quartz job failed, type: " + task.getId() + ",e: " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_QUARTZ_ADDFAILED, "Add quartz job failed, type: " + task.getId() + ",e: " + e.getMessage());
        }
    }

    public void addCronJob(TimerTaskEx task) throws CoreException{
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(task.getId(), task.getId()).build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("TimerTaskEx", task);
            //作业的触发器
            CronTrigger trigger = TriggerBuilder.//和之前的 SimpleTrigger 类似，现在的 CronTrigger 也是一个接口，通过 Tribuilder 的 build()方法来实例化
                    newTrigger().
                    withIdentity(task.getId(), task.getId()).
                    withSchedule(CronScheduleBuilder.cronSchedule(task.getCron())). //在任务调度器中，使用任务调度器的 CronScheduleBuilder 来生成一个具体的 CronTrigger 对象
                    build();

            // 调度容器设置JobDetail和Trigger
            sched.scheduleJob(jobDetail, trigger);

            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            LoggerEx.error(TAG, "Add quartzCron job failed, type: " + task.getId() + ",e: " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    public void removeJob(String id) throws CoreException{
        try {
            Scheduler sched = schedulerFactory.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(id, id);

            sched.pauseTrigger(triggerKey);// 停止触发器
            sched.unscheduleJob(triggerKey);// 移除触发器
            sched.deleteJob(JobKey.jobKey(id, id));// 删除任务
        } catch (Throwable e) {
            LoggerEx.error(TAG, "Delete quarz job failed, type: " + id + ",e: " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_QUARTZ_CANCELEDFAILED, "Delete quarz job failed, type: " + id + ",e: " + e.getMessage());
        }
    }

    public void shutdownJobs() throws CoreException{
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Throwable e) {
            LoggerEx.error(TAG, "Delete all quarz job failed,e: " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_QUARTZ_CANCELEDFAILED, "Delete all quarz job failed,e: " + e.getMessage());
        }
    }

    void setSchedulerFactory(SchedulerFactory schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    public SchedulerFactory getSchedulerFactory() {
        return schedulerFactory;
    }

    public synchronized static QuartzFactory getInstance() {
        if (instance == null) {
            instance = new QuartzFactory();
            instance.setSchedulerFactory(new StdSchedulerFactory());
            LoggerEx.warn(TAG, "Initial quartz success");
        }
        return instance;
    }
}
