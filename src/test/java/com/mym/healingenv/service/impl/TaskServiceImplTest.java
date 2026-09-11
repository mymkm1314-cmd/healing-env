package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.AssignDTO;
import com.mym.healingenv.dto.TaskCreateDTO;
import com.mym.healingenv.dto.UserDTO;
import com.mym.healingenv.entity.Assignment;
import com.mym.healingenv.entity.Dimension;
import com.mym.healingenv.entity.IndicatorVersion;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Task;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.mapper.TaskMapper;
import com.mym.healingenv.service.IAssignmentService;
import com.mym.healingenv.service.IDimensionService;
import com.mym.healingenv.service.IIndicatorService;
import com.mym.healingenv.service.IIndicatorVersionService;
import com.mym.healingenv.service.IUserService;
import com.mym.healingenv.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;
    @Mock
    private IIndicatorVersionService versionService;
    @Mock
    private IAssignmentService assignmentService;
    @Mock
    private IUserService userService;
    @Mock
    private IDimensionService dimensionService;
    @Mock
    private IIndicatorService indicatorService;
    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskService, "baseMapper", taskMapper);
        UserDTO user = new UserDTO();
        user.setUserId(100L);
        user.setRole(UserRole.PROJECT_MANAGER.getCode());
        UserHolder.saveUser(user);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    @Test
    void createTaskPersistsEveryAssignment() {
        IndicatorVersion version = new IndicatorVersion();
        version.setId(3L);
        version.setStatus(1);
        when(versionService.getById(3L)).thenReturn(version);

        Dimension dimensionOne = new Dimension();
        dimensionOne.setId(11L);
        Dimension dimensionTwo = new Dimension();
        dimensionTwo.setId(12L);
        when(dimensionService.listByIds(anyCollection()))
                .thenReturn(List.of(dimensionOne, dimensionTwo));
        Indicator indicatorOne = new Indicator();
        indicatorOne.setVersionId(3L);
        indicatorOne.setDimensionId(11L);
        Indicator indicatorTwo = new Indicator();
        indicatorTwo.setVersionId(3L);
        indicatorTwo.setDimensionId(12L);
        when(indicatorService.list(org.mockito.ArgumentMatchers.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(indicatorOne, indicatorTwo));

        User evaluator = new User();
        evaluator.setId(20L);
        evaluator.setRole(UserRole.EVALUATOR.getCode());
        evaluator.setStatus(1);
        when(userService.listByIds(anyCollection())).thenReturn(List.of(evaluator));

        when(taskMapper.insert(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(99L);
            return 1;
        });
        when(assignmentService.saveBatch(anyList())).thenReturn(true);

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setSpaceName("测试空间");
        dto.setVersionId(3L);
        dto.setAssigns(List.of(assign(11L, 20L), assign(12L, 20L)));

        Result<?> response = taskService.createTask(dto);

        assertEquals(200, response.getCode());
        assertEquals(99L, ((Map<?, ?>) response.getData()).get("taskId"));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Assignment>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(assignmentService).saveBatch(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    private AssignDTO assign(Long dimensionId, Long evaluatorId) {
        AssignDTO dto = new AssignDTO();
        dto.setDimensionId(dimensionId);
        dto.setEvaluatorId(evaluatorId);
        return dto;
    }
}
