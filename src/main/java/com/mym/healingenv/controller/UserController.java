package com.mym.healingenv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequireRole(UserRole.ADMIN)
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

    /**
     * 新增用户
     * POST /api/user
     */
    @PostMapping
    public Result<?> add(@Valid @RequestBody User user){
        return userService.add(user);
    }
    /**
     * 删除用户（逻辑删除）
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        return userService.delete(id);
    }

    /**
     * 重置密码
     * PUT /api/user/reset/{id}
     */
    @PutMapping("/reset/{id}")
    public Result<?> reset(@PathVariable Long id){
        return userService.resetPassword(id);
    }
}
