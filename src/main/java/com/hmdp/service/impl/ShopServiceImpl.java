package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;


@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService
{
    @Resource
    private StringRedisTemplate stringRedisTemplate ;

    @Override
    public Result queryById(Long id)
    {
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        // Shop shop = queryWithMutex(id) ;

        // 逻辑过期解决缓存击穿
        Shop shop = queryWithLogicalExpire(id) ;
        if (shop == null)
        {
            return Result.fail("店铺不存在") ;
        }

        // 返回
        return Result.ok(shop);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10) ;

    public Shop queryWithLogicalExpire(long id)
    {
        String key = CACHE_SHOP_KEY + id ;
        // 从redis查询缓存
        String shopjson = stringRedisTemplate.opsForValue().get(key);

        // 判断是否命中
        if (StrUtil.isBlank(shopjson))
        {
            // 未命中 返回空
            return null ;
        }

        // 命中 反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopjson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 未过期返回 然后结束
        if (expireTime.isAfter(LocalDateTime.now()))
        {
            return shop ;
        }

        // 过期 缓存重建
        String lockKey = LOCK_SHOP_KEY + id ;
        boolean isLock = tryLock(lockKey);

        if (isLock)
        {           // 成功 返回信息 开子线程缓存重建
            CACHE_REBUILD_EXECUTOR.submit(()->
            {
                try
                {
                    this.saveShop2redis(id , 30);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    unLock(lockKey);
                }
            });
        }

        return shop;
    }

    public Shop queryWithMutex(long id)
    {
        String key = CACHE_SHOP_KEY + id ;
        // 从redis查询缓存
        String shopjson = stringRedisTemplate.opsForValue().get(key);

        // 判断是否命中
        if (StrUtil.isNotBlank(shopjson))
        {
            // 命中 返回
            return JSONUtil.toBean(shopjson, Shop.class);
        }

        if (shopjson != null)
        {
            return null ;
        }


        // 未命中 是实现缓存重建

        String lockKey = "lock:shop:" + id ;
        Shop shop = null;
        try
        {
            boolean islock = tryLock(lockKey);

            // 判断锁是否获取成功
            if(!islock)
            {
                Thread.sleep(50);
                return queryWithMutex(id) ;
            }

            // 成功
            shop = queryById(id);

            // 不存在 返回错误
            if (shop == null)
            {
                stringRedisTemplate.opsForValue().set(key , "" , CACHE_NULL_TTL , TimeUnit.MINUTES);
                return null;
            }

            // 存在 写入redis 返回
            stringRedisTemplate.opsForValue().set(key , JSONUtil.toJsonStr(shop) , CACHE_SHOP_TTL  , TimeUnit.MINUTES);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // 释放互斥锁
            unLock(key);
        }

        // 返回
        return shop;
    }

    public Shop queryWithPassThrough(long id)
    {
        String key = CACHE_SHOP_KEY + id ;
        // 从redis查询缓存
        String shopjson = stringRedisTemplate.opsForValue().get(key);

        // 判断是否命中
        if (StrUtil.isNotBlank(shopjson))
        {
            // 命中 返回
            return JSONUtil.toBean(shopjson, Shop.class);
        }

        if (shopjson != null)
        {
            return null ;
        }

        // 未命中 根据id查询数据库
        Shop shop = getById(id);

        // 不存在 返回错误
        if (shop == null)
        {
            stringRedisTemplate.opsForValue().set(key , "" , CACHE_NULL_TTL , TimeUnit.MINUTES);
            return null;
        }

        // 存在 写入redis 返回
        stringRedisTemplate.opsForValue().set(key , JSONUtil.toJsonStr(shop) , CACHE_SHOP_TTL  , TimeUnit.MINUTES);

        return shop;
    }

    private boolean tryLock (String key)
    {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag) ;
    }

    private void unLock (String key)
    {
        stringRedisTemplate.delete(key) ;
    }

    public void saveShop2redis(long id , long expireSeconds)
    {
        // 查询店铺数据
        Shop shop = getById(id);

        // 封装逻辑过期时间
        RedisData redisData = new RedisData() ;
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        // 写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id , JSONUtil.toJsonStr(redisData));
    }

    @Transactional
    @Override
    public Result update(Shop shop)
    {
        Long id = shop.getId();
        if (id == null)
        {
            return Result.fail("店铺id不能为空") ;
        }

        //更新数据库
        updateById(shop) ;

        // 更新缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id) ;

        return Result.ok();
    }
}
