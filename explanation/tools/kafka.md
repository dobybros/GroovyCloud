##前提是在集群中运行KafkaService
### 1.生产者
##### 1.1引入
KafkaService为一个中间件Service，需要通过rpc远程调用
注意：新加一个kafkaListener或者topic时，需要重启kafkaService所在的server	
##### 1.2 一般使用举例	
```$xslt
public class TCClassRoomService{
    @ServiceBean(name = KafkaService.SERVICE)
    KafkaService kafkaService
    public void test(){
        kafkaService.send(SERVICE, topic, "test")
    }
}
```
##### 1.2.1 参数说明
send的第一个参数为微服务的service名字，用于选择producer<br/>
第二个参数为topic<br/>
第三个参数为发送的消息，为String

##### 1.3 自定义producer参数，并发送到kafka
```$xslt
public class TCClassRoomService{
    @ServiceBean(name = KafkaService.SERVICE)
    KafkaService kafkaService
    public void test(){
        kafkaService.send(SERVICE, topic, "test", map)
    }
}
```
##### 1.3.1参数说明
send的前三个参数见1.2.1，最后一个参数为map，里边可以放producer的配置，比如你想自定义bootstrap.servers，就放入map中传过来

### 2. 消费者，使用注解@KafkaListener，注解用于方法上
##### 2.1 介绍
该注解是通过远程service的方式回调回来的，所以需要在类上加上@RemoteService
##### 2.2 使用说明
```$xslt
@RemoteService
class CallbackConsumerHandler {
   
    @KafkaListener(groupId = "test", topics = ["#{topic.callback}"])
    void consumer(JSONObject task){
        ...
    }
}

```
##### 2.2.1 参数说明
>参数
- groupId:为kafka的组id，如果不定义将会生成一个包含topic的grouId，不是必须传<br/>
- topics:是一个数组，可以传多个，订阅的topic都会回调到这儿来，必须传<br/>
- 回调的参数tas: 里边会包含生产者生产的消息，以及这次消息的topic，业务系统根据topic进行相应的操作<br/>

