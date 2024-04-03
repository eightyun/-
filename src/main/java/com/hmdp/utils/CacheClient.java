package com.hmdp.utils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: CacheClient
 * Package: com.hmdp.utils
 * Description:
 * Create: 2024/4/3 - 16:29
 */

@Slf4j
@Component
public class CacheClient
{
    public final StringRedisTemplate stringRedisTemplate ;

    // 构造函数注入cacheclient
    public CacheClient(StringRedisTemplate stringRedisTemplate)
    {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key , Object value , Long time , TimeUnit unit)
    {
        stringRedisTemplate.opsForValue().set(key , JSONUtil.toJsonStr(value) , time , unit);
    }
}
