package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexPatterns;
import com.hmdp.utils.RegexUtils;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.security.SecureRandom;

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

        session.setAttribute("code" , code);

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

        Object cachecode = session.getAttribute("code");
        String code = loginForm.getCode();
        if(cachecode == null || !cachecode.toString().equals(code))
        {
            return Result.fail("验证码错误");
        }

        // 手机号跟验证码都通过检验  然后根据手机号查询用户  select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        if (user == null)
        {
            user.createWithPhone(phone) ;
        }

        session.setAttribute("user" , user);

        return Result.ok();
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
