package com.mym.healingenv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.common.ConfigKeys;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ConfigUpdateDTO;
import com.mym.healingenv.entity.Config;
import com.mym.healingenv.mapper.ConfigMapper;
import com.mym.healingenv.service.IConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final Map<String, String> DEFAULT_VALUES = Map.of(
            ConfigKeys.BONUS_SCORE_LIMIT, "5",
            ConfigKeys.GRADE_EXCELLENT_MIN, "90",
            ConfigKeys.GRADE_GOOD_MIN, "80",
            ConfigKeys.GRADE_PASS_MIN, "70",
            ConfigKeys.GRADE_BASIC_PASS_MIN, "60");

    @Override
    public Result<?> listConfigs() {
        return Result.success(list(new LambdaQueryWrapper<Config>()
                .orderByAsc(Config::getConfigKey)));
    }

    @Override
    public Result<?> getConfig(String configKey) {
        Result<?> keyError = validateConfigKey(configKey);
        if (keyError != null) {
            return keyError;
        }
        Config config = findConfig(configKey);
        if (config == null) {
            return Result.error("配置不存在");
        }
        return Result.success(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateConfig(String configKey, ConfigUpdateDTO updateDTO) {
        if (updateDTO == null) {
            return Result.error("配置内容不能为空");
        }
        updateDTO.setConfigKey(configKey);
        Result<?> validationResult = validateUpdates(List.of(updateDTO));
        if (validationResult != null) {
            return validationResult;
        }
        return persistUpdates(List.of(updateDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateConfigs(List<ConfigUpdateDTO> updateDTOs) {
        Result<?> validationResult = validateUpdates(updateDTOs);
        if (validationResult != null) {
            return validationResult;
        }
        return persistUpdates(updateDTOs);
    }

    @Override
    public Map<String, String> getValues(Collection<String> configKeys) {
        if (configKeys == null || configKeys.isEmpty()) {
            return Map.of();
        }
        Set<String> allowedKeys = configKeys.stream()
                .filter(ConfigKeys.ALLOWED_KEYS::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (allowedKeys.isEmpty()) {
            return Map.of();
        }
        return list(new LambdaQueryWrapper<Config>()
                .in(Config::getConfigKey, allowedKeys))
                .stream()
                .filter(config -> config.getConfigKey() != null && config.getConfigValue() != null)
                .collect(Collectors.toMap(
                        Config::getConfigKey,
                        Config::getConfigValue,
                        (first, second) -> second));
    }

    private Result<?> validateUpdates(List<ConfigUpdateDTO> updateDTOs) {
        if (updateDTOs == null || updateDTOs.isEmpty()) {
            return Result.error("至少需要一条配置更新");
        }

        Set<String> updateKeys = new LinkedHashSet<>();
        for (ConfigUpdateDTO updateDTO : updateDTOs) {
            if (updateDTO == null) {
                return Result.error("配置内容不能为空");
            }
            Result<?> keyError = validateConfigKey(updateDTO.getConfigKey());
            if (keyError != null) {
                return keyError;
            }
            if (!updateKeys.add(updateDTO.getConfigKey())) {
                return Result.error("批量更新中存在重复配置键");
            }
            Result<?> valueError = validateConfigValue(
                    updateDTO.getConfigKey(), updateDTO.getConfigValue());
            if (valueError != null) {
                return valueError;
            }
        }

        // 将数据库当前值和本次更新合并后再校验，避免单键更新破坏阈值顺序。
        Map<String, String> effectiveValues = new HashMap<>(DEFAULT_VALUES);
        effectiveValues.putAll(list(new LambdaQueryWrapper<Config>()).stream()
                .collect(Collectors.toMap(
                        Config::getConfigKey,
                        Config::getConfigValue,
                        (first, second) -> second)));
        for (ConfigUpdateDTO updateDTO : updateDTOs) {
            effectiveValues.put(updateDTO.getConfigKey(), updateDTO.getConfigValue());
        }
        return validateThresholdOrder(effectiveValues);
    }

    private Result<?> persistUpdates(List<ConfigUpdateDTO> updateDTOs) {
        Map<String, Config> existingConfigs = list(new LambdaQueryWrapper<Config>()).stream()
                .collect(Collectors.toMap(
                        Config::getConfigKey,
                        config -> config,
                        (first, second) -> second));
        List<Config> inserts = new ArrayList<>();
        List<Config> updates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (ConfigUpdateDTO updateDTO : updateDTOs) {
            Config config = existingConfigs.get(updateDTO.getConfigKey());
            if (config == null) {
                config = new Config();
                config.setConfigKey(updateDTO.getConfigKey());
                config.setConfigValue(updateDTO.getConfigValue());
                config.setRemark(updateDTO.getRemark());
                config.setCreateTime(now);
                config.setUpdateTime(now);
                inserts.add(config);
            } else {
                config.setConfigValue(updateDTO.getConfigValue());
                if (updateDTO.getRemark() != null) {
                    config.setRemark(updateDTO.getRemark());
                }
                config.setUpdateTime(now);
                updates.add(config);
            }
        }

        for (Config config : inserts) {
            if (!save(config)) {
                throw new IllegalStateException("配置创建失败");
            }
        }
        for (Config config : updates) {
            if (!updateById(config)) {
                throw new IllegalStateException("配置更新失败");
            }
        }
        return Result.success("配置更新成功");
    }

    private Result<?> validateConfigKey(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return Result.error("配置键不能为空");
        }
        if (!ConfigKeys.ALLOWED_KEYS.contains(configKey)) {
            return Result.error("不支持的配置键：" + configKey);
        }
        return null;
    }

    private Result<?> validateConfigValue(String configKey, String configValue) {
        if (configValue == null || configValue.isBlank()) {
            return Result.error("配置值不能为空");
        }
        BigDecimal numericValue;
        try {
            numericValue = new BigDecimal(configValue);
        } catch (NumberFormatException e) {
            return Result.error("配置值必须是有效数字");
        }
        if (ConfigKeys.BONUS_SCORE_LIMIT.equals(configKey)
                && numericValue.compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("加分上限不能为负数");
        }
        if (ConfigKeys.GRADE_THRESHOLD_KEYS.contains(configKey)
                && (numericValue.compareTo(BigDecimal.ZERO) < 0
                || numericValue.compareTo(new BigDecimal("100")) > 0)) {
            return Result.error("等级阈值必须在 0 到 100 之间");
        }
        return null;
    }

    private Result<?> validateThresholdOrder(Map<String, String> values) {
        BigDecimal excellent = new BigDecimal(values.get(ConfigKeys.GRADE_EXCELLENT_MIN));
        BigDecimal good = new BigDecimal(values.get(ConfigKeys.GRADE_GOOD_MIN));
        BigDecimal pass = new BigDecimal(values.get(ConfigKeys.GRADE_PASS_MIN));
        BigDecimal basicPass = new BigDecimal(values.get(ConfigKeys.GRADE_BASIC_PASS_MIN));
        if (excellent.compareTo(good) <= 0
                || good.compareTo(pass) <= 0
                || pass.compareTo(basicPass) <= 0) {
            return Result.error("等级阈值必须按优秀、良好、合格、基本合格的顺序递减");
        }
        return null;
    }

    private Config findConfig(String configKey) {
        return getOne(new LambdaQueryWrapper<Config>()
                .eq(Config::getConfigKey, configKey));
    }
}
