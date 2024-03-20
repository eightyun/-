package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * ClassName: DishController
 * Package: com.sky.controller.admin
 * Description:
 * Create: 2024/3/7 - 17:17
 *  菜品管理
 */

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController
{
    @Autowired
    private DishService dishService ;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO)
    {
        log.info("新增菜品: {}" , dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key) ;
        return Result.success() ;
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("菜品分页查询：{}" , dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO) ;
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids)
    {
        log.info("菜品批量删除: {}" , ids);
        dishService.deleteBatch(ids) ;

        // 将所有菜品缓存数据清理 所有开头为dish_的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys) ;
        return Result.success() ;
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getByIdWithFlavor(id) ;
        return Result.success(dishVO) ;
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品信息：{}" , dishDTO);
        dishService.updateWithFlavor(dishDTO) ;

        // 将所有菜品缓存数据清理 所有开头为dish_的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys) ;

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId)
    {
        // 构造redis中的key dish_分类id
        String key = "dish" + categoryId ;

        // 查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key) ;
        if(list != null && list.size() > 0)
        {
            return Result.success(list) ;
        }

        List<Dish> list = dishService.list(categoryId) ;
        return Result.success(list) ;
    }
}
