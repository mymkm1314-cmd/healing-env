package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.ConfigKeys;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ConfigUpdateDTO;
import com.mym.healingenv.entity.Config;
import com.mym.healingenv.mapper.ConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigMapper configMapper;
    @InjectMocks
    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(configService, "baseMapper", configMapper);
    }

    @Test
    void updateCreatesMissingAllowedConfig() {
        when(configMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(configMapper.insert(any(Config.class))).thenAnswer(invocation -> {
            Config config = invocation.getArgument(0);
            config.setId(1L);
            return 1;
        });

        ConfigUpdateDTO dto = new ConfigUpdateDTO();
        dto.setConfigValue("8.5");
        dto.setRemark("加分上限");

        Result<?> response = configService.updateConfig(ConfigKeys.BONUS_SCORE_LIMIT, dto);

        assertEquals(200, response.getCode());
        ArgumentCaptor<Config> captor = ArgumentCaptor.forClass(Config.class);
        verify(configMapper).insert(captor.capture());
        assertEquals(ConfigKeys.BONUS_SCORE_LIMIT, captor.getValue().getConfigKey());
        assertEquals("8.5", captor.getValue().getConfigValue());
    }

    @Test
    void updateRejectsNegativeBonusLimit() {
        ConfigUpdateDTO dto = new ConfigUpdateDTO();
        dto.setConfigValue("-1");

        Result<?> response = configService.updateConfig(ConfigKeys.BONUS_SCORE_LIMIT, dto);

        assertEquals(500, response.getCode());
        assertTrue(response.getMessage().contains("不能为负数"));
        verify(configMapper, never()).insert(any(Config.class));
    }

    @Test
    void batchUpdateRejectsThresholdsOutOfOrder() {
        ConfigUpdateDTO excellent = configDto(ConfigKeys.GRADE_EXCELLENT_MIN, "80");
        ConfigUpdateDTO good = configDto(ConfigKeys.GRADE_GOOD_MIN, "90");

        Result<?> response = configService.updateConfigs(List.of(excellent, good));

        assertEquals(500, response.getCode());
        assertTrue(response.getMessage().contains("递减"));
        verify(configMapper, never()).insert(any(Config.class));
    }

    @Test
    void updateExistingConfigChangesValue() {
        Config existing = new Config();
        existing.setId(10L);
        existing.setConfigKey(ConfigKeys.BONUS_SCORE_LIMIT);
        existing.setConfigValue("5");
        when(configMapper.selectList(any())).thenReturn(List.of(existing));
        when(configMapper.updateById(any(Config.class))).thenReturn(1);

        ConfigUpdateDTO dto = new ConfigUpdateDTO();
        dto.setConfigValue("6");

        Result<?> response = configService.updateConfig(ConfigKeys.BONUS_SCORE_LIMIT, dto);

        assertEquals(200, response.getCode());
        ArgumentCaptor<Config> captor = ArgumentCaptor.forClass(Config.class);
        verify(configMapper).updateById(captor.capture());
        assertEquals("6", captor.getValue().getConfigValue());
    }

    @Test
    void getValuesReturnsStoredAllowedValues() {
        Config bonus = new Config();
        bonus.setConfigKey(ConfigKeys.BONUS_SCORE_LIMIT);
        bonus.setConfigValue("7");
        when(configMapper.selectList(any())).thenReturn(List.of(bonus));

        Map<String, String> values =
                configService.getValues(List.of(ConfigKeys.BONUS_SCORE_LIMIT));

        assertEquals("7", values.get(ConfigKeys.BONUS_SCORE_LIMIT));
    }

    private ConfigUpdateDTO configDto(String key, String value) {
        ConfigUpdateDTO dto = new ConfigUpdateDTO();
        dto.setConfigKey(key);
        dto.setConfigValue(value);
        return dto;
    }
}
