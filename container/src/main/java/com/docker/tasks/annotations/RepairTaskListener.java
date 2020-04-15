package com.docker.tasks.annotations;

import java.lang.annotation.*;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RepairTaskListener {
    //unique
    public String id();

    //description of repairtask
    public String description() default "";

    //create time of task, specify by developer
    public String createTime() default "";

    public static final Integer TYPE_ONETIME_REPAIR = 1;
    public static final Integer TYPE_MULTIPLETIMES_REPAIR = 2;
    public int type() default 1;

}
