package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface PeriodicTask {
    //业务自行指定,每个任务type固定，最好带上项目标识
    public String type();

    //间隔时间(单位：秒)
    public int period() default 0;

    // 第一次执行时间  2018-1-3 11:00:00
    public String scheduleTime() default "";

    //时区
    public String timezone() default "Asia/Shanghai";

    //是否自动开始,true为立即开始，不用触发;false相反
    public boolean autoStart() default true;

    //失败了尝试多少次
    int tryTimes() default 100;

    //失败了每次尝试间隔多少时间(单位：秒)
    int tryPeriod() default 30;

    //cron表达式
    String cron() default "";

    int allowedExpireSeconds() default 0;

    //执行回调的版本号
    int version() default 1;
}
