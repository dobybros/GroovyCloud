package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface OneTimeTask {
    //业务自行指定,type固定
    public String type();

    //延迟时间（单位:秒）
    public int delay() default 0;

    //是否自动开始,true为立即开始，不用触发;false相反
    public boolean autoStart() default false;

    //失败了尝试多少次
    int tryTimes() default 100;

    //失败了每次尝试间隔多少时间(单位：秒)
    int tryPeriod() default 30;

    //任务多久过期(默认300天)
    int allowedExpireSeconds() default 25920000;

    //执行回调的版本号
    int version() default 1;
}
