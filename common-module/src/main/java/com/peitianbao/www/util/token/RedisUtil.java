package com.peitianbao.www.util.token;

import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.VoucherId;
import io.micrometer.core.instrument.util.IOUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author leg
 */
public class RedisUtil {
    private static volatile JedisPool JEDIS_POOL = null;
    private static final Object INIT_LOCK = new Object();

    /**
     * 获取 Redis 连接池资源
     */
    public static Jedis getJedis() {
        if (JEDIS_POOL == null) {
            synchronized (INIT_LOCK) {
                if (JEDIS_POOL == null) {
                    try {
                        // 🚨 尝试重新初始化
                        Properties properties = LoadProperties.load("application.properties");

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

                        // 创建连接池
                        JEDIS_POOL = new JedisPool(config, redisHost, redisPort);
                    } catch (Exception e) {
                        System.err.println("RedisUtil 初始化失败！");
                        System.err.println("错误类型: " + e.getClass().getName());
                        System.err.println("错误信息: " + e.getMessage());
                        e.printStackTrace(); // 打印完整堆栈跟踪
                        throw new RuntimeException("RedisUtil 初始化失败", e);
                    }
                }
            }
        }

        if (JEDIS_POOL == null) {
            throw new RuntimeException("JedisPool 未正确初始化");
        }

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

    /**
     * 从classpath或resource中读取Lua文件
     */
    public static String loadLuaScript(String scriptName) {
        try {
            InputStream is = RedisUtil.class.getClassLoader().getResourceAsStream(scriptName);
            if (is == null) {
                throw new RuntimeException("找不到 Lua 脚本：" + scriptName);
            }
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("加载 Lua 脚本失败：" + scriptName, e);
        }
    }
}