package com.docker.rpc.queue;

import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.rpc.QueueSimplexListener;
import com.docker.rpc.impl.RMIServerImplWrapper;
import com.docker.rpc.remote.RpcRequestCall;
import com.docker.server.OnlineServer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.core.appender.mom.kafka.DefaultKafkaProducerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaProducerFactory;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lick on 2020/2/8.
 * Description：
 */
public class KafkaSimplexListener implements QueueSimplexListener {
    @Resource
    RMIServerImplWrapper dockerRpcServer;
    private KafkaProducer producer;
    private KafkaConsumer consumer;
    private Properties producerProperties;
    private Properties consumerProperties;
    private String topic;
    private boolean started;
    private final String TAG = KafkaSimplexListener.class.getSimpleName();

    @Override
    public void send(String topic, String type, byte[] data, byte encode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("data", data);
        jsonObject.put("encode", encode);
        producer.send(new ProducerRecord(topic, jsonObject.toString().getBytes(StandardCharsets.UTF_8)), (recordMetadata, e) -> {
            if(e != null){
                LoggerEx.error(TAG, "Send message to kafka error!, topic: " + topic + ", err: " + ExceptionUtils.getFullStackTrace(e));
            }
        });
    }

    @Override
    public void send(String key, byte[] data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        producer.send(new ProducerRecord(topic, jsonObject.toString().getBytes(StandardCharsets.UTF_8)), (recordMetadata, e) -> {
            if(e != null){
                LoggerEx.error(TAG, "Send message to kafka error!, topic: " + topic + ", err: " + ExceptionUtils.getFullStackTrace(e));
            }
        });
    }

    @Override
    public void init() {
        started = true;
        KafkaProducerFactory factory = new DefaultKafkaProducerFactory();
        producer = (KafkaProducer) factory.newKafkaProducer(producerProperties);
        topic = OnlineServer.getInstance().getServer();
        consumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, topic);
        consumer = new KafkaConsumer(consumerProperties);
        consumer.subscribe(Arrays.asList(topic));
        while (started) {
            ConsumerRecords<String, byte[]> records =
                    consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, byte[]> record : records) {
                JSONObject jsonObject = JSON.parseObject(new String(record.value()));
                if(jsonObject != null){
                    try {
                        String type = jsonObject.getString("type");
                        if(type!= null){
                            if(RpcRequestCall.getInstance().containsType(type)){
                                if(jsonObject.getByte("encode") != null && jsonObject.getBytes("data") != null){
                                    RpcRequestCall.getInstance().call(dockerRpcServer, jsonObject.getString("type"), jsonObject.getByte("encode"), jsonObject.getBytes("data"));
                                }else {
                                    LoggerEx.error(TAG, "type or encode or data is null");
                                }
                            }
                        }
                    }catch (Throwable t){
                        LoggerEx.error(TAG, "Call local server err, errMsg: " + ExceptionUtils.getFullStackTrace(t));
                    }
                }
            }
        }
        if(!started){
            close();
        }
    }

    @Override
    public void shutdown() {
        this.started = false;
    }

    private void close(){
        try {
            producer.close();
        }catch (Throwable t){
            LoggerEx.error(TAG, "Producer close err, errMsg: " + ExceptionUtils.getFullStackTrace(t));
        }
        try {
            consumer.close();
        }catch (Throwable t){
            LoggerEx.error(TAG, "Consumer close err, errMsg: " + ExceptionUtils.getFullStackTrace(t));
        }
    }

    @Override
    public void setConfig(Map<String, String> config) {
//        producerProperties = new Properties();
//        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.get("bootstrap.servers"));
//        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.get("producer.key.serializer"));
//        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.get("producer.value.serializer"));
//        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, config.get("retries"));
//        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, config.get("linger.ms"));
//        consumerProperties = new Properties();
//        consumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.get("bootstrap.servers"));
//        consumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.get("consumer.key.serializer"));
//        consumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.get("consumer.value.serializer"));
    }
}
