package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * ClassName: RedisIdWorker
 * Package: com.hmdp.utils
 * Description:
 * Create: 2024/4/6 - 16:52
 */
@Component
public class RedisIdWorker
{
    // 开始时间戳
    private static final long BEGIN_TIMESTMAP = 1640995200L;

    // 序列号左移位数
    private static final int COUNT_BITS = 32 ;

    private StringRedisTemplate stringRedisTemplate ;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate)
    {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix)
    {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTMAP ;

        // 2.生成序列号
        // 2.1 获取当天日期 精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.拼接并返回
        return timestamp << COUNT_BITS | count ;
    }
}
