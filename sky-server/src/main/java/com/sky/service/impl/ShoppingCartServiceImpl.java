package com.sky.service.impl;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: ShoppingCartServiceImpl
 * Package: com.sky.service.impl
 * Description:
 * Create: 2024/3/21 - 17:36
 */

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService
{
    @Autowired
    private ShoppingCartService shoppingCartService ;
    @Autowired
    private DishMapper dishMapper ;
    @Autowired
    private SetmealDishMapper setmealDishMapper ;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO)
    {
        // 判断当前是否已经存在商品
        ShoppingCart shoppingCart = new ShoppingCart() ;
        BeanUtils.copyProperties(shoppingCartDTO , shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = ShoppingCartMapper.list(shoppingCart) ;

        if(list != null && list.size() > 0)
        {
            shoppingCart cart = list.get(0) ;
            cart.setNumber(cart.getNumber() + 1) ;
            ShoppingCartMapper.updateNumberById(cart) ;
        }
        else
        {
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null)
            {
                // 添加购物车的时菜品
                Dish dish = dishMapper.getById(dishId) ;
                shoppingCart.setName(dish.getName());
            }
            else
            {
                // 添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
            }
        }
    }
}
