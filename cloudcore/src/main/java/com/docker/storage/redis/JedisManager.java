package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author wangrongxuan
 * @version 1.1
 * @Description: 读写redis。 要求： 1、配置文件名字为redis.properties，放在根目录下;
 *               2、redis.properties中必须包含redis.pool字段，指定redis地址。
 *               3、redis.pool格式为host
 *               :port。如果有多个pool，用逗号分隔，如host1:port1,host2:port2
 * @create time 2016-3-8 下午3:28:32
 */
public class JedisManager {

	private static final String TAG = JedisManager.class.getSimpleName();
//	private static ShardedJedisPool pool = null;

	private static RedisHandler redisHandler = null;
	static {
		LoggerEx.info(TAG, "JedisPool初始化开始");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);// 最大连接数
		config.setMaxIdle(20);// 最大空闲实例数
		config.setMaxWaitMillis(30000L);// 最长等待时间
		config.setTestOnBorrow(true);// 在borrow一个jedis实例时，是否进行有效性检查。为true，则得到的jedis实例均是可用的

		ClassPathResource resource = new ClassPathResource("redis.properties");
		Properties pro = new Properties();
		try {
			pro.load(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			LoggerEx.error(TAG,
					"Prepare redis.properties is failed, " + e.getMessage());
		}

		String hosts = pro.getProperty("redis.pool");
		redisHandler = new RedisHandler(hosts);
		redisHandler.connect();
//		String[] strArray = pro.getProperty("redis.pool").split(",");// redis.properties中必须包含redis.pool字段，指定redis地址。如果有多个，用逗号分隔。
//		List<JedisShardInfo> shardJedis = new ArrayList<JedisShardInfo>();
//		for (int i = 0; i < strArray.length; i++) {
//			if (strArray[i].indexOf(":") > 0) {
//				String host = strArray[i].trim().substring(0,
//						strArray[i].indexOf(":"));
//				int port = Integer.parseInt(strArray[i].substring(strArray[i]
//						.indexOf(":") + 1));
//				shardJedis.add(new JedisShardInfo(host, port));
//			} else {
//				shardJedis.add(new JedisShardInfo(strArray[i]));
//			}
//		}
//		pool = new ShardedJedisPool(config, shardJedis);
		LoggerEx.info(TAG, "JedisPool初始化完成，连接到" + pro.getProperty("redis.pool"));
	}

	private JedisManager() {
	}

	/**
	 删除给定的一个或多个 key 。

	 不存在的 key 会被忽略。

	 时间复杂度：
	 O(N)， N 为被删除的 key 的数量。
	 删除单个字符串类型的 key ，时间复杂度为O(1)。
	 删除单个列表、集合、有序集合或哈希表类型的 key ，时间复杂度为O(M)， M 为以上数据结构内的元素数量。

	 返回值：
	 被删除 key 的数量。

	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public static Long del(String key) throws CoreException {
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.del(key);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis删除异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "del " + key
//					+ " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
		return redisHandler.del(key);
	}

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。

		若域 field 已经存在，该操作无效。
		
		如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
		
		时间复杂度：
		O(1)
		返回值：
		设置成功，返回 1 。
		如果给定域已经存在且没有操作被执行，返回 0 。
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public static Long hsetnx(String key, String field, String value)
			throws CoreException {
		return redisHandler.hsetnx(key, field, value);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hsetnx(key, field, value);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hsetnx " + key
//					+ " " + field + " " + value + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
		
		如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
		
		如果域 field 已经存在于哈希表中，旧值将被覆盖。
		
		时间复杂度：
		O(1)
		返回值：
		如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
		如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public static Long hset(String key, String field, String value)
			throws CoreException {
		return redisHandler.hset(key, field, value);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hset(key, field, value);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hset " + key
//					+ " " + field + " " + value + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。

	 此命令会覆盖哈希表中已存在的域。

	 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作

	 时间复杂度：
	 O(N)， N 为 field-value 对的数量。
	 返回值：
	 如果命令执行成功，返回 OK 。
	 当 key 不是哈希表(hash)类型时，返回一个错误。
	 * @param key
	 * @param hash
	 * @return
	 * @throws CoreException
	 */
	public static String hmset(String key, Map<String, String> hash)
			throws CoreException {
		return redisHandler.hmset(key, hash);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hmset(key, hash);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hmset " + key
//					+ " " + hash + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
		时间复杂度:
		O(N)， N 为要删除的域的数量。
		返回值:
		被成功移除的域的数量，不包括被忽略的域。
	 * 
	 * @param key
	 * @param fields
	 * @return
	 * @throws CoreException
	 */
	public static Long hdel(String key, String... fields) throws CoreException {
		return redisHandler.hdel(key, fields);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hdel(key, fields);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hdel " + key
//					+ " " + Arrays.toString(fields) + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。

		时间复杂度：
		O(1)
		返回值：
		给定域的值。
		当给定域不存在或是给定 key 不存在时，返回 nil 。
	 * 
	 * @param key
	 * @param field
	 * @return
	 * @throws CoreException
	 */
	public static String hget(String key, String field) throws CoreException {
		return redisHandler.hget(key, field);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hget(key, field);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hget " + key
//					+ " " + field + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。

	 如果给定的域不存在于哈希表，那么返回一个 nil 值。

	 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。

	 时间复杂度：
	 O(N)， N 为给定域的数量。

	 返回值：
	 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 * @param key
	 * @param fields
	 * @return
	 * @throws CoreException
	 */
	public static List<String> hmget(String key, String... fields) throws CoreException {
		return redisHandler.hmget(key, fields);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hmget(key, fields);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis获取 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hmget " + key
//					+ " " + fields + " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 * 返回哈希表 key 中，所有的域和值。

	 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。

	 时间复杂度：
	 O(N)， N 为哈希表的大小。

	 返回值：
	 以列表形式返回哈希表的域和域的值。
	 若 key 不存在，返回空列表。
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public static Map<String, String> hgetAll(String key) throws CoreException {
		return redisHandler.hgetAll(key);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hgetAll(key);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis获取 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hgetAll " + key
//					+ " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	/**
	 为哈希表 key 中的域 field 的值加上增量 increment 。

	 增量也可以为负数，相当于对给定域进行减法操作。

	 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。

	 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。

	 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。

	 本操作的值被限制在 64 位(bit)有符号数字表示之内。

	 时间复杂度：
	 O(1)

	 返回值：
	 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值。

	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public static Long hincrby(String key, String field, long value) throws CoreException {
		return redisHandler.hincrby(key, field, value);
//		ShardedJedis jedis = null;
//		try {
//			jedis = pool.getResource();
//			return jedis.hincrBy(key, field, value);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			// LoggerEx.error(TAG, "redis获取 " + e.getMessage());
//			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hgetAll " + key
//					+ " failed, " + e.getMessage());
//		} finally {
//			if (jedis != null)
//				jedis.close();
//		}
	}

	public static Long hsetObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hset(key, field, jsonStr);
	}

	public static Long hsetnxObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hsetnx(key, field, jsonStr);
	}

	public static <T> T hgetObject(String key, String field, Class<T> clazz) throws CoreException {
		String value = hget(key, field);
		if(value != null) {
			try {
				return JSON.parseObject(value, clazz);
			} catch(Throwable t) {
				LoggerEx.warn(TAG, "Value " + value + " is not  json format, return null for key " + key + " field " + field + ", error " + t.getMessage());
				return null;
			}
		}
		return null;
	}
}
