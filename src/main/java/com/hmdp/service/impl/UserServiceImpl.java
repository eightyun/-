package com.hmdp.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexPatterns;
import com.hmdp.utils.RegexUtils;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session)
    {
        if (!RegexUtils.isPhoneInvalid(phone))
        {
            return Result.fail("手机格式错误") ;
        }

        String code = RandomUtil.randomNumbers(6) ;

        StringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone , code , LOGIN_CODE_TTL , TimeUnit.MINUTES);

        // 发送验证码功能 后续由服务器运营商代发
        log.debug("发送验证码成功");

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session)
    {
        String phone = loginForm.getPhone();
        if (!RegexUtils.isPhoneInvalid(phone))
        {
            return Result.fail("手机格式错误") ;
        }

        // 从session获取验证码并校验
        String cachecode = StringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone) ;
        String code = loginForm.getCode();
        if(cachecode == null || !cachecode.equals(code))
        {
            return Result.fail("验证码错误");
        }

        // 手机号跟验证码都通过检验  然后根据手机号查询用户  select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        if (user == null)
        {
            user.createWithPhone(phone) ;
        }

        // 保存用户信息到 redid
        //  随机生成token 作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //  将user对象转为hash存储
        UserDTO userDTO = BeanUtils.copyProperties(user , UserDTO.class);
        Map<String , Object> userMap = BeanUtils.beanToMap(userDTO , newhashMap<>() , CopyOptions.create()
            .setIgnoreNullvalue(true)
            .setFieldVlueEditor(fieName , fieldValue) -> fieldValue.toString()) ;
        //  存储
        String tokenKey = LOGIN_USER_KEY + token ;
        StringRedisTemplate.opsForValue().putAll(tokenKey , userMap) ;

        //设置token有效期
        StringRedisTemplate.expire(tokenKey , LOGIN_USER_TTL , TimeUnit.MINUTES) ;

        return Result.ok(token);
    }

    private User createWithPhone(String phone)
    {
        User user = new User();
        user.setPhone(phone) ;
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10)) ;
        save(user) ; // mybatiesplus
        return user;
    }
}
