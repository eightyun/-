package com.sky.service;

import com.sky.dto.UserLoginDTO;

/**
 * ClassName: UserService
 * Package: com.sky.service
 * Description:
 * Create: 2024/3/20 - 10:56
 */
public interface UserService
{
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO) ;
}
