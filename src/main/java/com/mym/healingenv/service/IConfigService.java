package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ConfigUpdateDTO;
import com.mym.healingenv.entity.Config;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统配置表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IConfigService extends IService<Config> {

    Result<?> listConfigs();

    Result<?> getConfig(String configKey);

    Result<?> updateConfig(String configKey, ConfigUpdateDTO updateDTO);

    Result<?> updateConfigs(List<ConfigUpdateDTO> updateDTOs);

    Map<String, String> getValues(Collection<String> configKeys);
}
