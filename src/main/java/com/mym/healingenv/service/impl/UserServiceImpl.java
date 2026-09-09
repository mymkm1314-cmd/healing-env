package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.User;
import com.mym.healingenv.mapper.UserMapper;
import com.mym.healingenv.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
