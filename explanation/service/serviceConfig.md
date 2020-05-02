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