package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * ClassName: ShoppingCartService
 * Package: com.sky.service
 * Description:
 * Create: 2024/3/21 - 17:31
 */
public interface ShoppingCartService
{
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO) ;

}
