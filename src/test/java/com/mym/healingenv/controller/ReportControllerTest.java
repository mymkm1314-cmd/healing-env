package com.mym.healingenv.controller;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ReportUpdateDTO;
import com.mym.healingenv.service.IReportService;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private IReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void generateUsesTaskRoute() throws Exception {
        doReturn(Result.success()).when(reportService).generate(1L);

        mockMvc.perform(post("/api/report/generate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(reportService).generate(1L);
    }

    @Test
    void getUsesTaskRoute() throws Exception {
        doReturn(Result.success()).when(reportService).getByTask(1L);

        mockMvc.perform(get("/api/report/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(reportService).getByTask(1L);
    }

    @Test
    void updateUsesTaskRoute() throws Exception {
        doReturn(Result.success())
                .when(reportService)
                .update(eq(1L), any(ReportUpdateDTO.class));

        mockMvc.perform(put("/api/report/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"测试报告\",\"content\":\"内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(reportService).update(eq(1L), any(ReportUpdateDTO.class));
    }
}
