package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName: SetmealDishMapper
 * Package: com.sky.mapper
 * Description:
 * Create: 2024/3/18 - 14:48
 */
@Mapper
public interface SetmealDishMapper
{
    /**
     * 根据菜品id查询相应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds) ;
}
