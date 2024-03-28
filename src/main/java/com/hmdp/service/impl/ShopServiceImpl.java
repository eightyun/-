package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;


@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService
{
    @Resource
    private StringRedisTemplate stringRedisTemplate ;

    @Override
    public Result queryById(Long id)
    {
        String key = CACHE_SHOP_KEY + id ;
        // 从redis查询缓存
        String shopjson = stringRedisTemplate.opsForValue().get(key);

        // 判断是否命中
        if (StrUtil.isNotBlank(shopjson))
        {
            // 命中 返回
            Shop shop = JSONUtil.toBean(shopjson, Shop.class);
            return Result.ok(shop) ;
        }

        // 未命中 根据id查询数据库
        Shop shop = getById(id);

        // 不存在 返回错误
        if (shop == null)
        {
            return Result.fail("店铺不存在") ;
        }

        // 存在 写入redis 返回
        stringRedisTemplate.opsForValue().set(key , JSONUtil.toJsonStr(shop));

        return null;
    }
}
