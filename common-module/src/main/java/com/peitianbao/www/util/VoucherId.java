package com.peitianbao.www.util;

import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.util.token.RedisUtil;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author leg
 */
public class VoucherId {
    //2025.4.28.20.20
    private static final long BEGIN_TIMESTAMP = 1745871607L;

    //序列号位数
    private static final int COUNT_BITS = 32;

    public static long voucherId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String redisKey = "id:" + keyPrefix + ":" + date;

        try (Jedis jedis = RedisUtil.getJedis()) {
            long count = jedis.incr(redisKey);

            if (count == 1) {
                //第一次写入时设置过期时间（到当天结束）
                long secondsUntilMidnight = now.toLocalTime().until(LocalTime.MAX, ChronoUnit.SECONDS) + 1;
                jedis.expire(redisKey, (int) secondsUntilMidnight);
            }

            return (timestamp << COUNT_BITS) | count;
        } catch (Exception e) {
            throw new VoucherException("[ERROR] 全局ID生成失败：" + e.getMessage());
        }
    }
    public static void parseVoucherId(long id) {
        long count = id & (~(-1L << COUNT_BITS));
        long timestamp = id >>> COUNT_BITS;

        LocalDateTime beginTime = LocalDateTime.of(2025, 4, 28, 20, 20, 7);
        LocalDateTime realTime = beginTime.plusSeconds(timestamp);

        System.out.println("生成时间：" + realTime.format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println("当日序号：" + count);
    }
}
