### 1.@Bean,加在类上
##### 1.1 介绍
该微服务service全局唯一实例，可以在其它类上有注解的地方引用
##### 1.2 使用说明
```$xslt
@Bean
public class Test{
    
}

@Bean
public class Test1{
    @Bean
    Test test
}

@RemoteService
public class Test2{
    @Bean
    Test test
}
都可以使用test中的方法
```