package com.peitianbao.www.util.token;

import com.peitianbao.www.util.LoadProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * @author leg
 */
public class RedisUtil {
    private static final JedisPool JEDIS_POOL;

    static {
        // 配置 JEDIS_POOL 参数
        String configFileName = "application.properties";
        Properties properties = LoadProperties.load(configFileName);
        boolean testOnBorrow = Boolean.parseBoolean(properties.getProperty("jp.testOnBorrow"));
        int maxTotal = Integer.parseInt(properties.getProperty("jp.maxTotal"));
        int maxIdle = Integer.parseInt(properties.getProperty("jp.maxIdle"));
        int minIdle = Integer.parseInt(properties.getProperty("jp.minIdle"));
        int redisPort = Integer.parseInt(properties.getProperty("jp.redisPort"));
        String redisHost = properties.getProperty("jp.redisHost");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);

        // 初始化 JEDIS_POOL
        JEDIS_POOL = new JedisPool(config, redisHost, redisPort);
    }

    /**
     * 获取 Redis 连接
     */
    public static Jedis getJedis() {
        return JEDIS_POOL.getResource();
    }

    /**
     * 关闭连接池
     */
    public static void close() {
        if (JEDIS_POOL != null) {
            JEDIS_POOL.close();
        }
    }

    /**
     * 设置键值对（带过期时间）
     *
     * @param key          键
     * @param value        值
     * @param expireSeconds 过期时间（单位：秒）
     */
    public static void set(String key, String value, int expireSeconds) {
        try (Jedis jedis = getJedis()) {
            jedis.setex(key, expireSeconds, value);
        }
    }

    /**
     * 设置键值对（不过期）
     *
     * @param key   键
     * @param value 值
     */
    public static void set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
        }
    }

    /**
     * 获取键对应的值
     *
     * @param key 键
     * @return 值（如果不存在返回 null）
     */
    public static String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    /**
     * 删除指定的键
     *
     * @param key 键
     */
    public static void delete(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(key);
        }
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return true 表示存在，false 表示不存在
     */
    public static boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        }
    }

    /**
     * 设置键的过期时间
     *
     * @param key          键
     * @param expireSeconds 过期时间（单位：秒）
     */
    public static void expire(String key, int expireSeconds) {
        try (Jedis jedis = getJedis()) {
            jedis.expire(key, expireSeconds);
        }
    }
}