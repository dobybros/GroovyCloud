### 1.@RedeployMain(同时有@Bean的作用)，加在类上
##### 1.1 介绍
当重启一个微服务service,会先调用这个注解的类的main方法，类中必须加上main方法，启动执行的逻辑就放在main方法里；关闭这个service时会调用
这个类的shutdown方法
#### 1.2 举例
```$xslt
@script.groovy.annotation.RedeployMain
class RedeployMain {
    public void main() {

    }

    public void shutdown() {

    }
}
```