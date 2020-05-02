### 1.所有注解，如果参数是String型的，注解中支持#{}
##### 1.1举例
```
@PeriodicTask(type = "PeriodicTask_resource_deleteResourcePhysicalEveryDay", cron = "#{test.cron}")
void deleteResourcePhysical() {

}
```
则test.cron会去所在service的config中拿到test.cron放到这个属性中，获取方式见[config](https://github.com/dobybros/GroovyCloud/blob/master/explanation/common/config.md)