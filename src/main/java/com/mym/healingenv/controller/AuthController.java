package com.mym.healingenv.controller;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.LoginDTO;
import com.mym.healingenv.dto.UserDTO;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import com.mym.healingenv.utils.UserHolder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @GetMapping("/info")
    public Result<?> info() {
        UserDTO userDTO = UserHolder.get();
        System.out.println(userDTO);
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401,"info未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        user.setPasswordHash(null);
        return Result.success(user);
    }
}
