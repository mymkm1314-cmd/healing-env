package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ScoreSaveDTO;
import com.mym.healingenv.dto.UserDTO;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Task;
import com.mym.healingenv.service.IAssignmentService;
import com.mym.healingenv.service.IIndicatorService;
import com.mym.healingenv.service.ITaskService;
import com.mym.healingenv.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest {

    @Mock
    private ITaskService taskService;
    @Mock
    private IIndicatorService indicatorService;
    @Mock
    private IAssignmentService assignmentService;
    @InjectMocks
    private ScoreServiceImpl scoreService;

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    @Test
    void scoreCannotReferenceIndicatorFromAnotherVersion() {
        UserDTO evaluator = new UserDTO();
        evaluator.setUserId(7L);
        evaluator.setRole(UserRole.EVALUATOR.getCode());
        UserHolder.saveUser(evaluator);

        Task task = new Task();
        task.setId(1L);
        task.setVersionId(10L);
        task.setStatus((byte) 1);
        when(taskService.getById(1L)).thenReturn(task);

        Indicator indicator = new Indicator();
        indicator.setId(2L);
        indicator.setVersionId(11L);
        when(indicatorService.getById(2L)).thenReturn(indicator);

        ScoreSaveDTO dto = new ScoreSaveDTO();
        dto.setTaskId(1L);
        dto.setIndicatorId(2L);

        Result<?> response = scoreService.saveScore(dto);

        assertEquals(500, response.getCode());
        assertTrue(response.getMessage().contains("版本"));
    }
}
