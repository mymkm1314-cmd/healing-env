package com.mym.healingenv.controller;

import com.mym.healingenv.common.ConfigKeys;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ConfigUpdateDTO;
import com.mym.healingenv.service.IConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    @Mock
    private IConfigService configService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ConfigController controller = new ConfigController();
        ReflectionTestUtils.setField(controller, "configService", configService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listReturnsConfigValues() throws Exception {
        doReturn(Result.success()).when(configService).listConfigs();

        mockMvc.perform(get("/api/config/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(configService).listConfigs();
    }

    @Test
    void updateUsesPathKey() throws Exception {
        doReturn(Result.success())
                .when(configService)
                .updateConfig(eq(ConfigKeys.BONUS_SCORE_LIMIT), any(ConfigUpdateDTO.class));

        mockMvc.perform(put("/api/config/{key}", ConfigKeys.BONUS_SCORE_LIMIT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configValue\":\"8\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(configService).updateConfig(eq(ConfigKeys.BONUS_SCORE_LIMIT), any(ConfigUpdateDTO.class));
    }

    @Test
    void batchUpdateUsesBatchRoute() throws Exception {
        doReturn(Result.success())
                .when(configService)
                .updateConfigs(anyList());

        mockMvc.perform(put("/api/config/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"configKey\":\"bonus_score_limit\",\"configValue\":\"8\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(configService).updateConfigs(anyList());
    }
}
