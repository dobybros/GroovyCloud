### 1.@ConfigProperty，该注解加在属性上
##### 1.1 介绍
每一个微服务service都有一个config.properties(字段名称如果是多个单词组成，必须是.分隔，比如room.count)，在代码中拿到config.properties的字段，就通过注解
@ConfigProperty
##### 1.2 使用举例
```$xslt
class ClassroomVerifyController {
    @ConfigProperty(name = "test.ip")
    private String testIp
    }
```
##### 1.2.1参数说明
> 参数
- name:config.properties中的字段名称
### 2.config获取,数据库中的字段会覆盖config.properties的字段
>方式
- config.propertis
- 数据库中
```$xslt
数据库:config    
数据表:servers
数据结构: 
{
   "_id":"tcuser_v1",微服务service的名字+版本号
    //后边以<key,value>的形式放入表中即可
}
```