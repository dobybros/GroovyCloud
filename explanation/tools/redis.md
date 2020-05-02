### 1.@RedisListener，加在类上
##### 1.1介绍
RedisListener使用的是config.properties中db.redis.uri属性所对应的
redis地址，集群用逗号隔开；需要继承MyRedisListener拿到RedisHandler，然后操作redis

##### 1.2 举例
```$xslt
package db

import com.docker.storage.redis.MyRedisListener
import com.docker.storage.redis.annotation.RedisListener

@RedisListener
class ConvertRedis extends MyRedisListener{
    private final String PREFIX = "CONVERTTASKQUEUE"

    public String getConvertTask(){
        return redisHandler.rpop(PREFIX)
    }

    public Long getConvertTaskSize(){
        return redisHandler.llen(PREFIX)
    }
}
```

### 2.@RedisSubscribe，加在类上
##### 2.1介绍
存入String到redis以后，过期时会通知业务系统，业务系统需要在这个类上加方法public void redisCallback(String key)，具体回调逻辑写在这个方法中
##### 2.2举例
```$xslt
@RedisSubscribe(keys = [CLASSUSER_PREFIX])
class TCAPISessionMap {
    public void redisCallback(String key) {
            LoggerEx.error(TAG, "Key has expired, key: " + key)
        }
}
```
##### 2.3参数说明
>参数
- @RedisSubscribe中的keys:需要订阅为前缀的String类型的数据的过期时间，比如keys为["test1"]
那么key为test1eee,test1fff过期了都会回调到这儿来<br/>
- redisCallback中的key:回调回来的数据，这个key指的是过期的那个key