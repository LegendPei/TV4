package com.peitianbao.www.util;

import com.peitianbao.www.util.token.RedisUtil;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author leg
 */
public class VoucherId {
    //2025.4.28.20.20
    private static final long BEGIN_TIMESTAMP = 1745871607L;

    //序列号位数
    private static final int COUNT_BITS = 32;

    public static long voucherId(String keyPrefix) {
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        //生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key = "icr" + keyPrefix + ":" + date;

        //生成自增ID
        try (Jedis jedis = RedisUtil.getJedis()) {
            long count = jedis.incr(key);

            return timestamp << COUNT_BITS | count;
        }
    }
}
