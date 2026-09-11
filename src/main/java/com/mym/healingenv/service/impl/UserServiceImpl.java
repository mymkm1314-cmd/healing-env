package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.LoginDTO;
import com.mym.healingenv.dto.UserDTO;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.mapper.UserMapper;
import com.mym.healingenv.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.utils.JwtUtils;
import com.mym.healingenv.utils.PasswordUtils;
import com.mym.healingenv.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtils jwtUtils;
    @Override
    public Result<?> login(LoginDTO loginDTO) {
        //        //查找用户
        User user = lambdaQuery().eq(User::getUsername,loginDTO.getUsername()).one();
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
        user.setPasswordHash(null);
        Map<String,Object> data = new HashMap<>();
        data.put("token",token);
        data.put("userInfo",user);
        return Result.success(data);
    }

    @Override
    public Result<?> add(User user) {
        long count = lambdaQuery().eq(User::getUsername,user.getUsername()).count();
        if(count>0){
            return Result.error("用户已经存在");
        }
        user.setPasswordHash(PasswordUtils.encode("admin123"));
        if (user.getStatus()==null) {
            user.setStatus(1);
        }
        save(user);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> delete(Long id) {
        if (id==1L){
            return Result.error("不能删除管理员");
        }
        removeById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<?> resetPassword(Long id) {
        User user = new User();
        user.setId(id);
        user.setPasswordHash(PasswordUtils.encode("admin123"));
        updateById(user);
        return Result.success("密码已经重置");
    }
}
