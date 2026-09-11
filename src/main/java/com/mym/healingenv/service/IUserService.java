package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.LoginDTO;
import com.mym.healingenv.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IUserService extends IService<User> {

    Result<?> login(LoginDTO loginDTO);

    Result<?> add(User user);

    Result<?> delete(Long id);

    Result<?> resetPassword(Long id);
}
