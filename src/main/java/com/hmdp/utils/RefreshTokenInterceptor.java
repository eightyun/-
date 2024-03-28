package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: LoginInterceptor
 * Package: com.hmdp.utils
 * Description:
 * Create: 2024/3/27 - 16:18
 */
public class RefreshTokenInterceptor implements HandlerInterceptor
{
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        UserHolder.removeUser();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        // 获取请求头
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token))
        {
            return true ;
        }

        // 基于token获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object , Object> usermap = StringRedisTemplate.opsForhash().entries(key);

        // 判断用户是否存在
        if (usermap.isEmpty())
        {
            return true ;
        }

        // 查询到的hash转换成userdto
        UserDTO userDTO = BeanUtils.fillBeanWithMap(usermap , new UserDTO() , false ) ;

        // 保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);

        //刷新有效期
        StringRedisTemplate.expire(key , RedisConstants.LOGIN_USER_TTL , TimeUnit.MINUTES) ;

        return true ;
    }
}
