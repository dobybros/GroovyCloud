package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.errors.CoreErrorCodes;
import com.docker.storage.cache.CacheStorageFactory;
import com.docker.storage.cache.CacheStorageMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisMovedDataException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author wangrongxuan
 * @version 1.1
 * @Description: 读写redis。 要求： 1、配置文件名字为redis.properties，放在根目录下;
 * 2、redis.properties中必须包含redis.pool字段，指定redis地址。
 * 3、redis.pool格式为host
 * :port。如果有多个pool，用逗号分隔，如host1:port1,host2:port2
 * @create time 2016-3-8 下午3:28:32
 */
public class RedisHandler {

    public static final String NXXX = "NX";
    public static final String XXXX = "XX";
    public static final String EXPX = "PX";
    static final Integer TYPE_SHARD = 1;
    static final String TYPE_SHARD_PROTOCOL_NAME = "shard";

    static final Integer TYPE_CLUSTER = 2;
    static final String TYPE_CLUSTER_PROTOCOL_NAME = "cluster";

    public static final String LOCK_PREFIX = "LOCKED_";

    private final String TAG = RedisHandler.class.getSimpleName();

    private ShardedJedisPool pool = null;
    private Boolean isSubscribe = false;
    private JedisCluster cluster = null;
    private Map<String, Method> pipelineMethodMap = null;
    private String hosts;
    private String passwd;
    private Integer type = TYPE_SHARD;
    private Set<HostAndPort> redisNodes = new HashSet<>();
    private String[] subscribeChannels = {"__keyevent@0__:expired", "__keyevent@1__:expired", "__keyevent@2__:expired", "__keyevent@3__:expired", "__keyevent@4__:expired", "__keyevent@5__:expired", "__keyevent@6__:expired", "__keyevent@7__:expired", "__keyevent@8__:expired", "__keyevent@9__:expired", "__keyevent@10__:expired", "__keyevent@11__:expired", "__keyevent@12__:expired", "__keyevent@13__:expired", "__keyevent@14__:expired", "__keyevent@15__:expired"};
    private final int[] subscribeLock = new int[0];

    public RedisHandler(String hosts) {
        this.hosts = hosts;
    }

    public RedisHandler connect() {
//        disconnect();

        LoggerEx.info(TAG, "JedisPool initializing...");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);// 最大连接数
        config.setMaxIdle(20);// 最大空闲实例数
        config.setMaxWaitMillis(30000L);// 最长等待时间
        config.setTestOnBorrow(true);// 在borrow一个jedis实例时，是否进行有效性检查。为true，则得到的jedis实例均是可用的
        if(hosts.contains("@")){
            String[] passwdStrs = hosts.split("@");
            if(passwdStrs.length == 2){
                hosts = passwdStrs[0];
                passwd = passwdStrs[1];
            }
        }
        String[] detechedStrs = detachHosts(hosts);
        if (type.equals(TYPE_SHARD)) {
            String[] strArray = hosts.split(",");// redis.properties中必须包含redis.pool字段，指定redis地址。如果有多个，用逗号分隔。
            List<JedisShardInfo> shardJedis = new ArrayList<JedisShardInfo>();
            for (int i = 0; i < strArray.length; i++) {
                JedisShardInfo jedisShardInfo = null;
                if (strArray[i].indexOf(":") > 0) {
                    String host = strArray[i].trim().substring(0,
                            strArray[i].indexOf(":"));
                    int port = Integer.parseInt(strArray[i].substring(strArray[i]
                            .indexOf(":") + 1));
                    jedisShardInfo = new JedisShardInfo(host, port);
                    if(passwd != null){
                        jedisShardInfo.setPassword(passwd);
                    }
                    shardJedis.add(jedisShardInfo);
                } else {
                    jedisShardInfo = new JedisShardInfo(strArray[i]);
                    if(passwd != null){
                        jedisShardInfo.setPassword(passwd);
                    }
                    shardJedis.add(jedisShardInfo);
                }
            }
            pool = new ShardedJedisPool(config, shardJedis);
        } else if (type.equals(TYPE_CLUSTER)) {
            ArrayList<HostAndPort> nodes = new ArrayList<>();
            for (String host : detechedStrs) {
                String[] splitedHost = host.split(":");
                if (splitedHost.length > 1) {
                    nodes.add(new HostAndPort(splitedHost[0], Integer.parseInt(splitedHost[1])));
                }
            }
            createCluster(nodes, config, nodes.size());
        }
        pipelineMethodMap = new HashMap<>();
        LoggerEx.info(TAG, "Jedis Cluster connected, " + hosts);
        return this;
    }

    private void createCluster(ArrayList<HostAndPort> nodes, JedisPoolConfig config, Integer retryCount) {
        try {
            if (retryCount != nodes.size())
                nodes.add(nodes.remove(0));
            redisNodes = new LinkedHashSet<>(nodes);
            if(passwd != null){
                cluster = new JedisCluster(redisNodes, 3000, 3000, 3, passwd, config);
            }else {
                cluster = new JedisCluster(redisNodes, 3000, 3000, 3, config);
            }
        } catch (JedisConnectionException e) {
            --retryCount;
            if (retryCount > 0) {
                LoggerEx.error(TAG, "create redis cluster error JedisConnectionException, will retry " + retryCount + ", current node: " + redisNodes + ", eMsg: " + e.getMessage());
                createCluster(nodes, config, retryCount);
            } else {
                LoggerEx.fatal(TAG, "create redis cluster error JedisConnectionException, will not retry " + retryCount + ", current node: " + redisNodes + ", eMsg: " + e.getMessage());
                throw e;
            }
        }
    }

    private String[] detachHosts(String hosts) {
        String clearedHosts = hosts;
        String pName = null;
        if (hosts.contains("://")) {
            String[] strHosts = hosts.split("://");
            pName = strHosts[0];
            switch (pName) {
                case TYPE_CLUSTER_PROTOCOL_NAME:
                    this.type = TYPE_CLUSTER;
                    break;
                case TYPE_SHARD_PROTOCOL_NAME:
                    this.type = TYPE_SHARD;
                    break;
                default:
                    break;
            }
            clearedHosts = clearedHosts.replace(pName + "://", "");
            if(passwd != null){
                clearedHosts = clearedHosts.replace("@" + passwd, "");
            }
        }
        return clearedHosts.split(",");
    }

    public void disconnect() {
        try {
            if (pool != null && pool.isClosed()) {
                try {
                    pool.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                LoggerEx.info(TAG, "JedisPool closed, " + hosts);
            }
            if (cluster != null) {
                try {
                    cluster.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            LoggerEx.info(TAG, "Jedis Cluster closed, " + hosts);
        } catch (Exception e) {
            LoggerEx.info(TAG, "Jedis Cluster closed exception, " + hosts);
        }
    }

    public String lock(String id, Long expirseTime) throws CoreException {
        String lock = UUID.randomUUID().toString() + '_' + new Date().getTime();
        String lockedCode = "OK";
        if (!lockedCode.equals(set(LOCK_PREFIX + id, lock, "NX", "EX", expirseTime))) {
            throw new CoreException(CoreErrorCodes.ERROR_LOCK, "Can't lock.");
        }
        return lock;
    }

    public boolean unlock(String id, String lock) {
        try {
            String trastLock = get(LOCK_PREFIX + id);
            if (trastLock == null) {
                return false;
            }
            if (!trastLock.equals(lock)) {
                return false;
            }
            if (del(LOCK_PREFIX + id) < 1) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    /**
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
     * <p>
     * 若域 field 已经存在，该操作无效。
     * <p>
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
     * <p>
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 设置成功，返回 1 。
     * 如果给定域已经存在且没有操作被执行，返回 0 。
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @throws CoreException
     */
    public Long hsetnx(String key, String field, String value)
            throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hsetnx(key, field, value);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hsetnx(key, field, value);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hsetnx " + key
                    + " " + field + " " + value + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。
     * <p>
     * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     * <p>
     * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * <p>
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
     * 如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @throws CoreException
     */
    public Long hset(String key, String field, String value)
            throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hset(key, field, value);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hset(key, field, value);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hset " + key
                    + " " + field + " " + value + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     * 时间复杂度:
     * O(N)， N 为要删除的域的数量。
     * 返回值:
     * 被成功移除的域的数量，不包括被忽略的域。
     *
     * @param key
     * @param fields
     * @return
     * @throws CoreException
     */
    public Long hdel(String key, String... fields) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hdel(key, fields);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hdel(key, fields);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hdel " + key
                    + " " + Arrays.toString(fields) + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。
     * <p>
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 给定域的值。
     * 当给定域不存在或是给定 key 不存在时，返回 nil 。
     *
     * @param key
     * @param field
     * @return
     * @throws CoreException
     */
    public String hget(String key, String field) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hget(key, field);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hget(key, field);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hget " + key
                    + " " + field + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public Long hsetObject(String key, String field, Object obj) throws CoreException {
        String jsonStr = JSON.toJSONString(obj);
        return hset(key, field, jsonStr);
    }

    public Long hsetnxObject(String key, String field, Object obj) throws CoreException {
        String jsonStr = JSON.toJSONString(obj);
        return hsetnx(key, field, jsonStr);
    }

    public <T> T hgetObject(String key, String field, Class<T> clazz) throws CoreException {
        String value = hget(key, field);
        if (value != null) {
            try {
                return JSON.parseObject(value, clazz);
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + value + " is not  json format, return null for key " + key + " field " + field + ", error " + ExceptionUtils.getFullStackTrace(t));
                return null;
            }
        }
        return null;
    }

    public JSONObject hgetJsonObject(String key, String field) throws CoreException {
        String value = hget(key, field);
        if (value != null) {
            try {
                return JSON.parseObject(value);
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + value + " is not  json format, return null for key " + key + " field " + field + ", error " + ExceptionUtils.getFullStackTrace(t));
                return null;
            }
        }
        return null;
    }

    /**
     * 获取哈希表中字段的数量
     * O(1)
     */
    public Long hlen(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hlen(key);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hlen(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hlen " + key
                    + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String setObject(String prefix, String key, Object obj, String nxxx, String expx, long time) throws CoreException {
        return setObject(prefix + "_" + key, obj, nxxx, expx, time);
    }

    public String setObject(String prefix, String key, Object obj) throws CoreException {
        return setObject(prefix + "_" + key, obj);
    }

    public Long ttl(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.ttl(key);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.ttl(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "ttl " + key + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public Long pttl(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.pttl(key);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.pttl(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "pttl " + key + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String set(String key, String value, String nxxx, String expx, long time) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.set(key, value, nxxx, expx, time);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key, value, nxxx, expx, time);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "set " + key
                    + " " + value + " " + nxxx + " " + expx + " " + time + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String set(String key, String value, String nxxx) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.set(key, value, nxxx);
        });
    }

    public String set(String key, String value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.set(key, value);
        });
    }

    public Long setNX(String key, String value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.setnx(key, value);
        });
    }

    //return old value
    public String getSet(String key, String value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.getSet(key, value);
        });
    }

    public Long del(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.del(key);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.del(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS);
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String psetex(String key, String value, long time) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.psetex(key, time, value);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.psetex(key, time, value);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "psetex " + key
                    + " " + value + " " + time + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String setObject(String key, Object obj) throws CoreException {
        return doJedisExecute(jedis -> {
            String jsonStr = JSON.toJSONString(obj);
            return jedis.set(key, jsonStr);
        });
    }

    public String setObject(String key, Object obj, String nxxx, String expx, long time) throws CoreException {
        return doJedisExecute(jedis -> {
            String jsonStr = JSON.toJSONString(obj);
            return jedis.set(key, jsonStr, nxxx, expx, time);
        });
/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            String jsonStr = JSON.toJSONString(obj);
            return jedis.set(key, jsonStr, nxxx, expx, time);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "setObject " + key
                    + " " + obj + " " + nxxx + " " + expx + " " + time + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public <T> T getObject(String prefix, String key, Class<T> clazz) throws CoreException {
        return getObject(prefix + "_" + key, clazz);
    }

    public <T> T getObject(String prefix, String key, Type type) throws CoreException {
        return getObject(prefix + "_" + key, type);
    }

    public <T> T getObject(String key, Class<T> clazz) throws CoreException {
        String json = doJedisExecute(jedis -> {
            return jedis.get(key);
        });
        if (json != null) {
            try {
                return JSON.parseObject(json, clazz);
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + json + " is not  json format, return null for key " + key + " class " + clazz + ", error " + ExceptionUtils.getFullStackTrace(t));
                return null;
            }
        }
        return null;
    }

    public <T> T getObject(String key, Type type) throws CoreException {
        String json = doJedisExecute(jedis -> {
            return jedis.get(key);
        });
        if (json != null) {
            try {
                return JSON.parseObject(json, type);
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + json + " is not  json format, return null for key " + key + " type " + type + ", error " + ExceptionUtils.getFullStackTrace(t));
                return null;
            }
        }
        return null;
    }

    public Long delObject(String prefix, String key) throws CoreException {
        return del(prefix + "_" + key);
    }

    public String get(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.get(key);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            value = jedis.get(key);
            return value;
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "get " + key
                    + " " + value + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public Long expire(String prefix, String key, long expire) throws CoreException {
        return expire(prefix + "_" + key, expire);
    }

    public Long expire(String key, long expire) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.pexpire(key, expire);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.pexpire(key, expire);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "expire " + key
                    + " " + expire + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个对象插入到列表头部
     *
     * @param key
     * @param obj
     * @return
     * @throws CoreException
     */
    public Long lpushObject(String key, Object obj) throws CoreException {
        return doJedisExecute(jedis -> {
            String jsonStr = JSON.toJSONString(obj);
            return jedis.lpush(key, jsonStr);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            String jsonStr = JSON.toJSONString(obj);
            return jedis.lpush(key, jsonStr);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpushObject " + key
                    + " " + JSON.toJSONString(obj) + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个字符串插入到列表头部
     *
     * @param key
     * @param value
     * @return
     * @throws CoreException
     */
    public Long lpush(String key, String value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.lpush(key, value);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpush " + key
                    + " " + value + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public Long lrem(String key, long l, String value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.lrem(key, l, value);
        });
    }

    /**
     * 将一个对象从列表头部取出
     *
     * @param key
     * @param clazz 获取类的类型
     * @return
     * @throws CoreException
     */
    public <T> T lpopObject(String key, Class<T> clazz) throws CoreException {
        return doJedisExecute(jedis -> {
            String value = jedis.lpop(key);
            return JSON.parseObject(value, clazz);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = jedis.lpop(key);
            return JSON.parseObject(value, clazz);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpopObject " + key
                    + " " + key + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个字符串从列表头部取出
     *
     * @param key
     * @return
     * @throws CoreException
     */
    public String lpop(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.lpop(key);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpop(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpop " + key
                    + " " + key + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个对象从列表尾部取出
     *
     * @param key
     * @param obj
     * @return
     * @throws CoreException
     */
    public Long rpushObject(String key, Object obj) throws CoreException {
        return doJedisExecute(jedis -> {
            String jsonStr = JSON.toJSONString(obj);
            return jedis.rpush(key, jsonStr);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            String jsonStr = JSON.toJSONString(obj);
            return jedis.rpush(key, jsonStr);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpushObject " + key
                    + " " + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个字符串从列表尾部取出
     *
     * @param key
     * @return
     * @throws CoreException
     */
    public Long rpush(String key, String... value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.rpush(key, value);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.rpush(key, value);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpush " + key
                    + " " + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个对象从列表尾部取出
     *
     * @param key
     * @param clazz
     * @return
     * @throws CoreException
     */
    public <T> T rpopObject(String key, Class<T> clazz) throws CoreException {
        return doJedisExecute(jedis -> {
            String value = jedis.rpop(key);
            return JSON.parseObject(value, clazz);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            String value = jedis.rpop(key);
            return JSON.parseObject(value, clazz);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpopObject " + key
                    + " " + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 将一个字符串从列表尾部取出
     *
     * @param key
     * @return
     * @throws CoreException
     */
    public String rpop(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.rpop(key);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.rpop(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpop " + key
                    + " " + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 获取list长度
     *
     * @param key
     * @return
     * @throws CoreException
     */
    public Long llen(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.llen(key);
        });

/*        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.llen(key);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "llen " + key
                    + " " + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }*/
    }

    /**
     * 获取list中string对象
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @throws CoreException
     */
    public List<String> lrange(String key, long start, long end) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.lrange(key, start, end);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, start, end);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lrange " + key
                    + " " + start + " " + end + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    /**
     * 获取list中object对象
     *
     * @param key
     * @param index
     * @param clazz
     * @param <T>
     * @return
     * @throws CoreException
     */
    public <T> Object lIndex(String key, long index, Class<T> clazz) throws CoreException {
        String redisResult = doJedisExecute(jedis -> {
            return jedis.lindex(key, index);
        });
/*
        ShardedJedis jedis = null;
        String redisResult;
        try {
            jedis = pool.getResource();
            redisResult = jedis.lindex(key, index);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lindex key: " + key
                    + ", index: " + index + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }*/

        if (redisResult != null) {
            try {
                return JSON.parseObject(redisResult, clazz);
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + redisResult + " is not  json format, return null for key " + key + " class " + clazz + ", error " + t.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 获取list中object对象
     *
     * @param key
     * @param start
     * @param end
     * @param clazz
     * @param <T>
     * @return
     * @throws CoreException
     */
    public <T> List<T> lrange(String key, long start, long end, Class<T> clazz) throws CoreException {
        List<String> redisResult = doJedisExecute(jedis -> {
            return jedis.lrange(key, start, end);
        });

/*
        ShardedJedis jedis = null;
        List<String> redisResult;
        try {
            jedis = pool.getResource();
            redisResult = jedis.lrange(key, start, end);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lrange object " + key
                    + " " + start + " " + end + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/

        if (redisResult != null) {
            try {
                List<T> T = new ArrayList<T>();
                for (String json : redisResult) {
                    T.add(JSON.parseObject(json, clazz));
                }
                return T;
            } catch (Throwable t) {
                LoggerEx.warn(TAG, "Value " + redisResult + " is not  json format, return null for key " + key + " class " + clazz + ", error " + t.getMessage());
                return null;
            }
        }
        return null;
    }


    public String ltrim(String key, long start, long end) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.ltrim(key, start, end);
        });

/*
        ShardedJedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.ltrim(key, start, end);
        } catch (Throwable e) {
            e.printStackTrace();
            // LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "ltrim " + key
                    + " " + start + " " + end + " failed, " + e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
*/
    }

    public String hmset(String key, Map<String, String> hash)
            throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hmset(key, hash);
        });
    }

    public List<String> hmget(String key, String... fields) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hmget(key, fields);
        });
    }

    public Map<String, String> hgetAll(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hgetAll(key);
        });
    }

    public <T> Map<String, T> hgetAllObject(String key, Class<T> clazz) throws CoreException {
        Map<String, String> map = hgetAll(key);
        if (map != null && !map.isEmpty()) {
            Map<String, T> result = new HashMap<>();
            for (String theKey : map.keySet()) {
                result.put(theKey, JSON.parseObject(map.get(theKey), clazz));
            }
            return result;
        }
        return null;
    }

    public Long hincrby(String key, String field, long value) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.hincrBy(key, field, value);
        });
    }


    public Long sadd(String key, String... members) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.sadd(key, members);
        });
    }

    public Set<String> smembers(String key) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.smembers(key);
        });
    }

    public boolean sismembers(String key, String memeber) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.sismember(key, memeber);
        });
    }

    public Long srem(String key, String... members) throws CoreException {
        return doJedisExecute(jedis -> {
            return jedis.srem(key, members);
        });
    }


    // sortedSet
    public Long zadd(String key, double score, String member) throws CoreException {
        if (key != null && member != null) {
            return doJedisExecute(jedis -> {
                return jedis.zadd(key, score, member);
            });
        }
        return null;
    }

    public Set<String> zrangebyscoreWithoutScore(String key, double minScore, double maxScore) throws CoreException {
        if (key != null) {
            return doJedisExecute(jedis -> {
                return jedis.zrangeByScore(key, minScore, maxScore);
            });
        }
        return null;
    }

    public Long zremrangebyrank(String key, long start, long end) throws CoreException {
        if (key != null) {
            return doJedisExecute(jedis -> {
                return jedis.zremrangeByRank(key, start, end);
            });
        }
        return null;
    }

    public Set<String> zrange(String key, Integer start, Integer end) throws CoreException {
        if (key != null) {
            return doJedisExecute(jedis -> {
                return jedis.zrange(key, start, end);
            });
        }
        return null;
    }

    public Long zcard(String key) throws CoreException {
        if (key != null) {
            return doJedisExecute(jedis -> {
                return jedis.zcard(key);
            });
        }
        return null;
    }

    public Set<Tuple> zrangebyscoreWithScore(String key, double minScore, double maxScore) throws CoreException {
        if (key != null) {
            return doJedisExecute(jedis -> {
                return jedis.zrangeByScoreWithScores(key, minScore, maxScore);
            });
        }
        return null;
    }

    public Long zrem(String key, String... member) throws CoreException {
        if (key != null && member != null) {
            return doJedisExecute(jedis -> {
                return jedis.zrem(key, member);
            });
        }
        return null;
    }

    /**
     * run lua script
     * warn: if use many key in shard redis, these keys must in same shard, so only support one key currently.
     */
    public Object eval(String script, String key, String... values) throws CoreException {
        if (StringUtils.isNotBlank(script) && StringUtils.isNotBlank(key)) {
            return doJedisExecute(jedis -> {
                List<String> paramsList = new ArrayList<>();
                paramsList.add(key);
                paramsList.addAll(Arrays.asList(values));
                String[] params = paramsList.toArray(new String[0]);
                if (jedis instanceof JedisCluster) {
                    return ((JedisCluster) jedis).eval(script, 1, params);
                } else if (jedis instanceof ShardedJedis) {
                    Jedis shardJ = ((ShardedJedis) jedis).getShard(key);
                    return shardJ.eval(script, 1, params);
                }
                return null;
            });
        }
        return null;
    }

    void subscribe() {
        boolean canExecute = false;
        if (!isSubscribe) {
            synchronized (subscribeLock) {
                if (!isSubscribe) {
                    isSubscribe = true;
                    canExecute = true;
                }
            }
        }
        if (canExecute) {
            new Thread(() -> {
                JedisCommands jedis = getJedis();
                if (jedis instanceof ShardedJedis) {
                    Jedis[] jedisArray = new Jedis[]{};
                    jedisArray = ((ShardedJedis) jedis).getAllShards().toArray(jedisArray);
                    Jedis theJedis = jedisArray[0];
                    theJedis.psubscribe(SubscribeListener.getInstance(), subscribeChannels);
                } else if (jedis instanceof JedisCluster) {
                    MyRedisPubSubAdapter.getInstance().psubscribe(subscribeChannels, redisNodes, passwd);
                }
            }).start();

        }
    }

    private <V> V doJedisExecute(JedisExcutor executor) throws CoreException {
        JedisCommands jedis = null;
        try {
            jedis = getJedis();
            return (V) executor.execute(jedis);
        } catch (JedisConnectionException e) {
            CacheStorageFactory.getInstance().reloadCacheStorageAdapter(CacheStorageMethod.METHOD_REDIS, hosts);
            LoggerEx.fatal(TAG, "Redis execute err JedisConnectionException, pleaseCheck,host:" + hosts + " ,errMsg: " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "Redis execute failed. host: " + hosts + ",errMsg:" + e.getMessage());
        } catch (Throwable e) {
//             || e.getMessage().contains("No reachable node")
            LoggerEx.fatal(TAG, "Redis execute err, pleaseCheck,host:" + hosts + " ,errMsg: " + ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "Redis execute failed. host: " + hosts + ",errMsg:" + e.getMessage());
        } finally {
            if (jedis != null && jedis instanceof ShardedJedis) {
                ((ShardedJedis) jedis).close();
            }
        }
    }

    private JedisCommands getJedis() {
        JedisCommands jedis = null;
        if (type.equals(TYPE_SHARD)) {
            jedis = pool.getResource();
        } else if (type.equals(TYPE_CLUSTER)) {
            jedis = cluster;
        }
        return jedis;
    }

    public void expireByPipeline(String prefix, List<String> keys, long expire) {
        invokePipelineMethod(true, pipelineBase -> {
            if (!keys.isEmpty()) {
                if (!StringUtils.isBlank(prefix)) {
                    for (String key : keys) {
                        pipelineBase.pexpire(prefix + "_" + key, expire);
                    }
                } else {
                    for (String key : keys) {
                        pipelineBase.pexpire(key, expire);
                    }
                }
            }
        }, RedisContants.PIPELINE_SYNC);
    }

    public void hsetByPipeline(List<String> keys, String field, String value) {
        invokePipelineMethod(true, pipelineBase -> {
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    pipelineBase.hset(key, field, value);
                }
            }
        }, RedisContants.PIPELINE_SYNC);
    }

    public void rpushByPipeline(String key, List<Object> objects) {
        invokePipelineMethod(true, pipelineBase -> {
            if (objects != null && !objects.isEmpty()) {
                for (Object o : objects) {
                    if (o instanceof String) {
                        pipelineBase.rpush(key, (String) o);
                    } else {
                        pipelineBase.rpush(key, JSON.toJSONString(o));
                    }
                }
            }
        }, RedisContants.PIPELINE_SYNC);
    }

    public <T> List<T> hgetObjectByPipeline(final String key, final List<String> fileds, Class<T> clazz) throws CoreException {
        Object result = invokePipelineMethod(true, pipelineBase -> {
            for (String field : fileds) {
                pipelineBase.hget(key, field);
            }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        if (result instanceof List) {
            List<T> list = new ArrayList<>();
            List<String> theResult = (List) result;
            for (String o : theResult) {
                list.add(JSON.parseObject(o, clazz));
            }
            return list;
        }
        return null;
    }

    public void hdelByPipeline(List<String> keys, String[] fields) {
        invokePipelineMethod(true, pipelineBase -> {
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    pipelineBase.hdel(key, fields);
                }
            }
        }, RedisContants.PIPELINE_SYNC);
    }

    public List<Long> hlenByPipeline(final List<String> keys) throws CoreException {
        Object result = invokePipelineMethod(true, pipelineBase -> {
            for (String key : keys) {
                pipelineBase.hlen(key);
            }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        if (result instanceof List)
            return (List) result;
        return null;
    }

    // hash
    public List<Object> hgetAllByPipeline(final List<String> keys, final String prefixKey) throws CoreException {
        Object result = invokePipelineMethod(true, pipelineBase -> {
            if (StringUtils.isBlank(prefixKey))
                for (String key : keys) {
                    pipelineBase.hgetAll(key);
                }
            else
                for (String key : keys) {
                    pipelineBase.hgetAll(prefixKey + key);
                }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        if (result instanceof List)
            return (List) result;
        return null;
    }

    public List<Object> hmgetByPipeline(final List<String> keys, final String prefixKey, final String... field) throws CoreException {
        Object result = invokePipelineMethod(true, pipelineBase -> {
            if (StringUtils.isBlank(prefixKey))
                for (String key : keys) {
                    pipelineBase.hmget(key, field);
                }
            else
                for (String key : keys) {
                    pipelineBase.hmget(prefixKey + key, field);
                }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        if (result instanceof List)
            return (List) result;
        return null;
    }

    public Map<String, String> hmsetByPipeline(final HashMap<String, Map<String, String>> hashMap, final String prefixKey) throws CoreException {
        List<String> keys = new ArrayList<>();
        Object result = invokePipelineMethod(true, pipelineBase -> {
            if (StringUtils.isBlank(prefixKey))
                for (String key : hashMap.keySet()) {
                    keys.add(key);
                    Map<String, String> value = hashMap.get(key);
                    pipelineBase.hmset(key, value);
                }
            else
                for (String key : hashMap.keySet()) {
                    keys.add(key);
                    Map<String, String> value = hashMap.get(key);
                    pipelineBase.hmset(prefixKey + key, value);
                }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        Map<String, String> resultMap = new HashMap<>();
        if (result instanceof List) {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = (String) ((List<?>) result).get(i);
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    private Object invokePipelineMethod(Boolean needTryAgain, PipelineExcutor excutor, String methodName, Object... args) {
        ShardedJedis shardedJedis = null;
        PipelineBase pipeline = null;
        try {
            if (type.equals(TYPE_SHARD)) {
                shardedJedis = pool.getResource();
                pipeline = shardedJedis.pipelined();
            } else if (type.equals(TYPE_CLUSTER)) {
                pipeline = JedisClusterPipeline.pipelined(cluster);
            }
            if (excutor != null)
                excutor.execute(pipeline);
            if (methodName != null && pipelineMethodMap != null) {
                Method method = pipelineMethodMap.get(methodName);
                if (method == null) {
                    method = pipeline.getClass().getMethod(methodName);
                    pipelineMethodMap.put(methodName, method);
                }
                return method.invoke(pipeline, args);
            }
        } catch (JedisConnectionException e) {
            e.printStackTrace();
            CacheStorageFactory.getInstance().reloadCacheStorageAdapter(CacheStorageMethod.METHOD_REDIS, hosts);
            LoggerEx.fatal(TAG, "Redis execute err JedisConnectionException, pleaseCheck,host:" + hosts + " ,errMsg: " + ExceptionUtils.getFullStackTrace(e));
        } catch (Throwable t) {
            t.printStackTrace();
            LoggerEx.fatal(TAG, "invokePipelineMethod: " + methodName + ", args: " + args + " error, eMsg: " + t.getMessage());
            if (t instanceof JedisMovedDataException && pipeline instanceof JedisClusterPipeline) {
                LoggerEx.error(TAG, "Have occurred JedisMovedDataException, will refresh cluster!");
                JedisClusterPipeline clusterPipeline = (JedisClusterPipeline) pipeline;
                clusterPipeline.refreshCluster();
            }
            if (needTryAgain != null && needTryAgain)
                return invokePipelineMethod(false, excutor, methodName, args);
        } finally {
            if (shardedJedis != null)
                shardedJedis.close();
            if (pipeline instanceof JedisClusterPipeline)
                ((JedisClusterPipeline) pipeline).close();
        }
        return null;
    }

    @FunctionalInterface
    interface JedisExcutor<T> {
        T execute(JedisCommands commands);
    }

    @FunctionalInterface
    interface PipelineExcutor<T> {
        void execute(PipelineBase pipelineBase);
    }
}
