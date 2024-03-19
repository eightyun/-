package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ShopController
 * Package: com.sky.controller.admin
 * Description:
 * Create: 2024/3/19 - 18:02
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("tag = 店铺相关接口")
@Slf4j
public class ShopController
{
    public static final String KEY = "SHOP_STATUS" ;


    @Autowired
    private RedisTemplate redisTemplate ;

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus()
    {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY) ;
        log.info("查询店铺状态为：{}" , status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
