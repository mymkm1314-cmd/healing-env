package com.mym.healingenv.controller;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.LoginDTO;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import com.mym.healingenv.utils.JwtUtils;
import com.mym.healingenv.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        //查找用户
        User user = userService.lambdaQuery().eq(User::getUsername,loginDTO.getUsername()).one();
        if (user == null) {
            return Result.error("账号不存在");
        }
        if(user.getStatus()==0){
            return Result.error("账号已经禁用");
        }
        if(!PasswordUtils.matches(loginDTO.getPassword(),user.getPasswordHash())){
            return Result.error("密码错误");
        }
        //生成token
        String token = jwtUtils.generateToken(user.getId(),user.getUsername(),user.getRole());
        Map<String,Object> data = new HashMap<>();
        data.put("token",token);
        data.put("userInfo",user);
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtils.getUserId(token);
            User user = userService.getById(userId);
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(401, "token无效");
        }
    }

}
