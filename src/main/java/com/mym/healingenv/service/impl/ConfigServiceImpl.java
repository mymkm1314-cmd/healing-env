package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Config;
import com.mym.healingenv.mapper.ConfigMapper;
import com.mym.healingenv.service.IConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements IConfigService {

}
