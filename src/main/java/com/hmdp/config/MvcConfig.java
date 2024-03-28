package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * ClassName: MvcConfig
 * Package: com.hmdp.config
 * Description:
 * Create: 2024/3/27 - 16:29
 */
public class MvcConfig implements WebMvcConfigurer
{
    @Resource
    private StringRedisTemplate stringRedisTemplate ;
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        "/shop/**" ,
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**",
                        "blog/hot",
                        "/user/code",
                        "/user/login"
                );
    }
}
