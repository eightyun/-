package com.hmdp.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: SimpleRedisLock
 * Package: com.hmdp.utils
 * Description:
 * Create: 2024/4/8 - 0:01
 */
public class SimpleRedisLock implements ILock
{

    private String name ;
    private StringRedisTemplate stringRedisTemplate ;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate)
    {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:" ;
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-" ;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static
    {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    @Override
    public boolean tryLock(long timeoutSec)
    {
        // 获取锁线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();

        //获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId , timeoutSec, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success) ;
    }

    @Override
    public void unlcok()
    {
        // 调用lua脚本
        stringRedisTemplate.execute
                (
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name) ,
                ID_PREFIX + Thread.currentThread().getId()
        ) ;
    }

    // 解决分布式锁误删问题代码
    /*@Override
    public void unlcok()
    {
        // 获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId() ;

        // 获取锁中的标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);

        if(threadId.equals(id))
        {
            // 释放锁
            stringRedisTemplate.delete(KEY_PREFIX + name) ;
        }
    }*/
}
