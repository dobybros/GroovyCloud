package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.errors.CoreErrorCodes;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import redis.clients.jedis.*;
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
    private PipelineBase pipeline = null;
    private Map<String, Method> pipelineMethodMap = null;
    private String hosts;
    private Integer type = TYPE_SHARD;

    public RedisHandler(String hosts) {
        this.hosts = hosts;
    }

    public RedisHandler connect() {
        disconnect();

        LoggerEx.info(TAG, "JedisPool initializing...");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);// 最大连接数
        config.setMaxIdle(20);// 最大空闲实例数
        config.setMaxWaitMillis(30000L);// 最长等待时间
        config.setTestOnBorrow(true);// 在borrow一个jedis实例时，是否进行有效性检查。为true，则得到的jedis实例均是可用的

        String[] detechedStrs = detachHosts(hosts);

        if (type == TYPE_SHARD) {
            String[] strArray = hosts.split(",");// redis.properties中必须包含redis.pool字段，指定redis地址。如果有多个，用逗号分隔。
            List<JedisShardInfo> shardJedis = new ArrayList<JedisShardInfo>();
            for (int i = 0; i < strArray.length; i++) {
                if (strArray[i].indexOf(":") > 0) {
                    String host = strArray[i].trim().substring(0,
                            strArray[i].indexOf(":"));
                    int port = Integer.parseInt(strArray[i].substring(strArray[i]
                            .indexOf(":") + 1));
                    shardJedis.add(new JedisShardInfo(host, port));
                } else {
                    shardJedis.add(new JedisShardInfo(strArray[i]));
                }
            }
            pool = new ShardedJedisPool(config, shardJedis);
        } else if (type == TYPE_CLUSTER) {
            Set<HostAndPort> nodes = new HashSet<HostAndPort>();
            for (String host : detechedStrs) {
                String[] splitedHost = host.split(":");
                if (splitedHost.length > 1) {
                    nodes.add(new HostAndPort(splitedHost[0], Integer.parseInt(splitedHost[1])));
                }
            }
            cluster = new JedisCluster(nodes, config);
        }
        pipelineMethodMap = new HashMap<>();
//        new Thread(this::subscribe).start();
        LoggerEx.info(TAG, "Jedis Cluster connected, " + hosts);
        return this;
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
        }
        return clearedHosts.split(",");
    }

    public void disconnect() {
        try {
            if (pool != null && pool.isClosed()) {
                pool.close();
                LoggerEx.info(TAG, "JedisPool closed, " + hosts);
            }
            if (cluster != null) {
                cluster.close();
            }
            if (pipeline != null && pipeline instanceof JedisClusterPipeline) {
                JedisClusterPipeline clusterPipeline = (JedisClusterPipeline) pipeline;
                clusterPipeline.close();
            }
            SubscribeListener.getInstance().punsubscribe();
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
        } finally {
            return true;
        }
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
    public Long lrem(String key, long l, String value) throws CoreException{
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

    // sortedSet
    public Integer zadd(String key, double score, String member) throws CoreException {
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

    public void subscribe() {
        if (!isSubscribe) {
            isSubscribe = true;
            JedisCommands jedis = getJedis();
            if (jedis instanceof ShardedJedis) {
                Jedis[] jedisArray = new Jedis[]{};
                jedisArray = ((ShardedJedis) jedis).getAllShards().toArray(jedisArray);
                Jedis theJedis = jedisArray[0];
                theJedis.psubscribe(SubscribeListener.getInstance(), "__keyevent@*__:expired");
            } else if (jedis instanceof JedisCluster) {
                ((JedisCluster) jedis).psubscribe(SubscribeListener.getInstance(), "__keyevent@*__:expired");
            }
        }
    }

    private <V> V doJedisExecute(JedisExcutor executor) throws CoreException {
        JedisCommands jedis = null;
        try {
            jedis = getJedis();
            return (V) executor.execute(jedis);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CoreException(CoreErrorCodes.ERROR_REDIS, "Redis execute failed." + e.getMessage());
        } finally {
            if (jedis != null && jedis instanceof ShardedJedis) {
                ((ShardedJedis) jedis).close();
            }
        }
    }

    private JedisCommands getJedis() {
        JedisCommands jedis = null;
        if (type == TYPE_SHARD) {
            jedis = pool.getResource();
        } else if (type == TYPE_CLUSTER) {
            jedis = cluster;
        }
        return jedis;
    }

    // hash
    public List<Object> hgetAllByPipeline(final List<String> keys, final String prefixKey) throws CoreException {
        Object result = invokePipelineMethod(true, new PipelineExcutor() {
            @Override
            public void execute(PipelineBase pipelineBase) {
                if (StringUtils.isBlank(prefixKey))
                    for (String key : keys) {
                        pipelineBase.hgetAll(key);
                    }
                else
                    for (String key : keys) {
                        pipelineBase.hgetAll(prefixKey + key);
                    }
            }
        }, RedisContants.PIPELINE_SYNC_AND_RETURN_ALL);
        if (result instanceof List)
            return (List) result;
        return null;
    }

    private Object invokePipelineMethod(Boolean needTryAgain, PipelineExcutor excutor, String methodName, Object... args) {
        ShardedJedis shardedJedis = null;
        try {
            if (type == TYPE_SHARD) {
                shardedJedis = pool.getResource();
                pipeline = shardedJedis.pipelined();
            } else if (type == TYPE_CLUSTER) {
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
        } catch (Throwable t) {
            t.printStackTrace();
            LoggerEx.error(TAG, "invokePipelineMethod: " + methodName + ", args: " + args + " error, eMsg: " + t.getMessage());
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
