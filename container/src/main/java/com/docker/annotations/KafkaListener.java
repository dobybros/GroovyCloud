package com.docker.annotations;

import java.lang.annotation.*;

/**
 * Created by lick on 2019/9/6.
 * Description：
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface KafkaListener {
    //消费的topic,一个service可以消费多个topic
    public String[] topics();

    //所属消费组,可以不写，不写会随机生成
    public String groupId() default "";

    public String keyDeserializer() default "String";

    public String valueDeserializer() default "String";
}
