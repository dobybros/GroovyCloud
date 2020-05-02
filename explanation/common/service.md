### 1.@RemoteService(同时有bean的作用)，该注解加在类上
##### 1.1 介绍
当一个类需要被远程调用时，比如当你在TCClassCrontroller添加教室时，需要调用TCClassroomService的addRoom方法，需要在TCClassroomService
添加注解@RemoteService<br/>
##### 1.2 一般使用举例
```$xslt
@RemoteService<br/>
public class TCClassRoomService{
    TCClassRoom addTCClassRoom(TCClassRoom classRoom， List<String> memberIds) {
    }
}
```
##### 1.3 升级使用
当异步调用远程service时，@RemoteService中可以限制对这个远程方法的并发访问数以及可以等待访问该远程方法的数量
##### 1.3.1举例
```$xslt
@RemoteService(concurrentLimit = 1， waitingSize = 0)
public class TCUserService{
    CompletableFuture<String> getStringTestAsync() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            String get() {
                return "licklulu"
            }
        })
        return future
    }
}
```
##### 1.3.2 说明
concurrentLimit 为同时访问这个远程service的最大个数，waitingSize为如果超出最大个数，有多少可以进入队列等待；执行完正在执行的任务后，会优先执行队列的任务，并返回执行结果

### 2.@ServiceBean  该注解加在属性上
##### 2.1 介绍
当需要远程调用一个注有@RemoteService的类的方法时，使用该注解
##### 2.2 一般使用介绍
```$xslt
class TCRecordController {
    @ServiceBean(name = TCRecordService.SERVICE)
    TCRecordService tcRecordService

    @RequestMapping(uri = "@PREFIX/record/start"， method = POST)
    public def startRecord(HttpServletRequest request， HttpServletResponse response) {
        def json = RequestUtils.readJson(request)
        ValidateUtils.checkAllNotNull(json.roomId， json.name， json.userId， json.type)
        if (json.type != TCUser.TYPE_OBSERVER) {
            throw new CoreException(Errors.ERROR_ILLEGAL_PARAMETERS， "User's type must be observer")
        }
        tcRecordService.startRecordInstance(json)
    }
}
```
##### 2.2.1 说明
@ServiceBean中的name为需要调用的远程service的名字

##### 2.3 升级使用
@ServiceBean(name = TCRecordService.SERVICE， lanId="America")
TCRecordService tcRecordService
##### 2.3.1说明
当需要跨集群调用远程service时，可以加上lanId，lanId为在数据库中配置的lan的id，当跨集群时，会根据lanId调用所在集群的service
##### 2.3.2配置说明
```$xslt
数据库:config    
数据表:lans 
数据结构: 
{
    domain:地址  
    protocol: http或https  
    port: http端口  
    type:0为rpc 1为http，目前只支持http
}
```
### 3.@RedeployMain，加在类上
##### 3.1 介绍
当重启一个微服务service,会先调用这个注解的类的main方法，类中必须加上main方法，启动执行的逻辑就放在main方法里；关闭这个service时会调用
这个类的shutdown方法
#### 3.2 举例
```$xslt
@script.groovy.annotation.RedeployMain
class RedeployMain {
    public void main() {
       
    }

    public void shutdown() {
        
    }
}
```
### 4.@ConfigProperty，该注解加在属性上
##### 4.1 介绍
每一个微服务service都有一个config.properties(字段名称如果是多个单词组成，必须是.分隔，比如room.count)，在代码中拿到config.properties的字段，就通过注解
@ConfigProperty
##### 4.2 使用举例
```$xslt
class ClassroomVerifyController {
    @ConfigProperty(name = "test.ip")
    private String testIp
    }
```
##### 4.2.1参数说明
> 参数
- name:config.properties中的字段名称
