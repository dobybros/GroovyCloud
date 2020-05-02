### 1.业务层使用方式
@JavaBean(fromBeanClass = com.docker.storage.zookeeper.ZookeeperFactory.class, params = ["#{db.zk.host}"])

com.docker.storage.zookeeper.ZookeeperClient zookeeperClient

接下来就可以调用zookeeperClient的方法来使用zookeeper了
### 2.说明
#### 2.1参数说明
db.zk.host为zookeeper的地址从config中获取的，获取方式见[config](https://github.com/dobybros/GroovyCloud/blob/master/explanation/common/config.md)，集群使用逗号隔开

### 3.举例
```$xslt
@Bean
class RoomZK {
    @JavaBean(fromBeanClass = ZookeeperFactory.class, params = ["#{db.zk.host}"])
    ZookeeperClient zookeeperClient

    public void markRoomStatus(String roomId, Integer status, boolean stopWatch) {
        if (zookeeperClient.checkExists().forPath(getPath(roomId)) == null) {
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(getPath(roomId), status.toString().getBytes(Charset.defaultCharset()))
        } 
    }
}
```