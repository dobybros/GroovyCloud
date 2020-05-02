### 1.@TimerTask(同时有bean的作用),加在类上
##### 1.1 介绍
该注解是用于单机定时器，只会回调到所在Groovycloud的微服务上，类中必须加上main方法，定时回调的逻辑写在main方法中<br/>
如果使用分布式定时任务，请前往[Distributed timer](https://github.com/dobybros/GroovyCloud/blob/master/explanation/timer/scheduleTask.md)
##### 1.2使用举例
```$xslt
@TimerTask(period = 5000L, key = "scheduleTaskCheckhandler")
class CheckAnnotationHandler {
    void main() {
       }
}
@TimerTask(cron = "0 0 0 * * ? *", key = "scheduleTaskCheckhandler")
class CheckAnnotationHandler {
    void main() {
       }
}
```
##### 1.3参数说明(period和cron传一个即可，如果都传会采用cron)
> 参数 :
- key:任务的唯一标识，最好是由service名字 + 类名组成<br/>
- period:多久执行一次，单位毫秒<br/>
- cron:cron表达式，具体参考https://cron.qqe2.com/

