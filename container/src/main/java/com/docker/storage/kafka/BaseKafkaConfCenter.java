package com.docker.storage.kafka;

import java.util.Properties;

/**
 * Created by lick on 2020/6/30.
 * Description：
 */
public class BaseKafkaConfCenter {
    public static volatile BaseKafkaConfCenter instance;
    private KafkaConfCenter kafkaConfCenter;

    public static BaseKafkaConfCenter getInstance() {
            synchronized (BaseKafkaConfCenter.class){
                if(instance == null){
                    instance = new BaseKafkaConfCenter();

                }
            }
            return instance;
    }

    public void setKafkaConfCenter(Properties producerProperties, Properties consumerProperties) {
        if(producerProperties != null || consumerProperties != null){
            kafkaConfCenter = new KafkaConfCenter();
            if(producerProperties != null){
                kafkaConfCenter.filterKafkaConf(producerProperties, KafkaConfCenter.FIELD_PRODUCE);
            }
            if(consumerProperties != null){
                kafkaConfCenter.filterKafkaConf(consumerProperties, KafkaConfCenter.FIELD_CONSUMER);
            }
        }
    }

    public KafkaConfCenter getKafkaConfCenter() {
        return kafkaConfCenter;
    }
}
