package com.mym.healingenv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private IUserService userService;

    /**
     * 分页查询用户
     * GET /api/user/page?current=1&size=10&keyword=
     */

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role
    ){
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null&&!keyword.isBlank()){
            queryWrapper.like(User::getUsername, keyword)
            .or().like(User::getRealName, keyword)
            .or().like(User::getPhone, keyword);
        }
        if (role != null){
            queryWrapper.eq(User::getRole, role);
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        //隐藏密码hash
        return Result.success(userService.page(page, queryWrapper).convert(user -> {
            user.setPasswordHash(null);
            return user;
        }));
    }
}
