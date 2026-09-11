package com.mym.healingenv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.TaskCreateDTO;
import com.mym.healingenv.entity.Assignment;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.IndicatorVersion;
import com.mym.healingenv.entity.Task;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.mapper.TaskMapper;
import com.mym.healingenv.service.IAssignmentService;
import com.mym.healingenv.service.IDimensionService;
import com.mym.healingenv.service.IIndicatorService;
import com.mym.healingenv.service.IIndicatorVersionService;
import com.mym.healingenv.service.ITaskService;
import com.mym.healingenv.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价任务表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements ITaskService {

    @Autowired
    private IIndicatorVersionService versionService;
    @Autowired
    private IAssignmentService assignmentService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IDimensionService dimensionService;
    @Autowired
    private IIndicatorService indicatorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> createTask(TaskCreateDTO taskCreateDTO) {
        if (!hasManagementRole()) {
            return Result.error(403, "无权创建任务");
        }
        if (taskCreateDTO.getVersionId() == null) {
            return Result.error("指标库版本不能为空");
        }
        IndicatorVersion version = versionService.getById(taskCreateDTO.getVersionId());
        if (version == null || !Integer.valueOf(1).equals(version.getStatus())) {
            return Result.error("指标库不存在或者未发布");
        }
        if (taskCreateDTO.getAssigns() == null || taskCreateDTO.getAssigns().isEmpty()) {
            return Result.error("至少需要一条任务指派");
        }
        Set<Long> assignedDimensions = new HashSet<>();
        if (taskCreateDTO.getAssigns().stream()
                .anyMatch(assign -> !assignedDimensions.add(assign.getDimensionId()))) {
            return Result.error("同一任务中每个维度只能指派给一名评价员");
        }
        if (dimensionService.listByIds(assignedDimensions).size() != assignedDimensions.size()) {
            return Result.error("存在无效的评价维度");
        }
        LambdaQueryWrapper<Indicator> versionDimensionWrapper = new LambdaQueryWrapper<>();
        versionDimensionWrapper.eq(Indicator::getVersionId, version.getId())
                .in(Indicator::getDimensionId, assignedDimensions);
        long versionDimensionCount = indicatorService.list(versionDimensionWrapper)
                .stream()
                .map(Indicator::getDimensionId)
                .distinct()
                .count();
        if (versionDimensionCount != assignedDimensions.size()) {
            return Result.error("任务指派包含该指标版本中不存在的维度");
        }
        Set<Long> evaluatorIds = taskCreateDTO.getAssigns().stream()
                .map(assign -> assign.getEvaluatorId())
                .collect(Collectors.toSet());
        Map<Long, User> evaluatorMap = userService.listByIds(evaluatorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        for (Long evaluatorId : evaluatorIds) {
            User evaluator = evaluatorMap.get(evaluatorId);
            if (evaluator == null
                    || !Integer.valueOf(1).equals(evaluator.getStatus())
                    || !Integer.valueOf(UserRole.EVALUATOR.getCode()).equals(evaluator.getRole())) {
                return Result.error("评价员不存在、已禁用或角色不正确");
            }
        }
        Task task = new Task();
        task.setSpaceName(taskCreateDTO.getSpaceName());
        task.setPurpose(taskCreateDTO.getPurpose());
        task.setEvalDate(taskCreateDTO.getEvalDate() != null?taskCreateDTO.getEvalDate() : LocalDate.now());
        task.setVersionId(taskCreateDTO.getVersionId());
        task.setCreatorId(UserHolder.getUserId());
        task.setStatus((byte)0);
        if (!this.save(task)) {
            throw new IllegalStateException("任务创建失败");
        }

        List<Assignment> assignments = taskCreateDTO.getAssigns().stream().map(a -> {
            Assignment assignment = new Assignment();
            assignment.setTaskId(task.getId());
            assignment.setDimensionId(a.getDimensionId());
            assignment.setEvaluatorId(a.getEvaluatorId());
            assignment.setSubmitStatus((byte) 0);
            return assignment;
        }).collect(Collectors.toList());
        if (!assignmentService.saveBatch(assignments)) {
            throw new IllegalStateException("任务指派创建失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getId());
        data.put("status", task.getStatus());
        return Result.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateStatus(Long taskId,Integer status){
        Task task = this.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        Result<?> accessResult = checkTaskManagementAccess(task);
        if (accessResult != null) {
            return accessResult;
        }
        if (status == null || status < 0 || status > 4) {
            return Result.error("任务状态不合法");
        }
        int currentStatus = task.getStatus() == null ? 0 : task.getStatus();
        if (!isAllowedTransition(currentStatus, status)) {
            return Result.error("状态流转不合法：当前状态 " + currentStatus);
        }
        if (currentStatus == 1 && status == 2) {
            long totalAssignments = assignmentService.lambdaQuery()
                    .eq(Assignment::getTaskId, taskId)
                    .count();
            long submittedAssignments = assignmentService.lambdaQuery()
                    .eq(Assignment::getTaskId, taskId)
                    .eq(Assignment::getSubmitStatus, (byte) 1)
                    .count();
            if (totalAssignments == 0 || totalAssignments != submittedAssignments) {
                return Result.error("仍有维度未提交，无法进入待核算状态");
            }
        }
        task.setStatus(status.byteValue());
        if (!this.updateById(task)) {
            throw new IllegalStateException("任务状态更新失败");
        }
        return Result.success("状态更新为：" + getStatusName(status));
    }

    @Override
    public Result<?> pageTasks(Integer current, Integer size, String keyword, Integer status) {
        Page<Task> page = new Page<>(current, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.PROJECT_MANAGER) {
            wrapper.eq(Task::getCreatorId, UserHolder.getUserId());
        } else if (role == UserRole.EVALUATOR) {
            List<Long> taskIds = assignmentService.lambdaQuery()
                    .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                    .list()
                    .stream()
                    .map(Assignment::getTaskId)
                    .distinct()
                    .toList();
            if (taskIds.isEmpty()) {
                return Result.success(page);
            }
            wrapper.in(Task::getId, taskIds);
        }
        wrapper.like(keyword != null && !keyword.isEmpty(), Task::getSpaceName, keyword)
                .eq(status != null, Task::getStatus, status)
                .orderByDesc(Task::getCreateTime);
        return Result.success(this.page(page, wrapper));
    }

    @Override
    public Result<?> getDetail(Long id) {
        Task task = this.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canViewTask(task)) {
            return Result.error(403, "无权查看该任务");
        }
        // 查指派信息
        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, id)
                .list();
        Map<String, Object> data = new HashMap<>();
        data.put("task", task);
        data.put("assignments", assignments);
        return Result.success(data);
    }

    private boolean canViewTask(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN || role == UserRole.VIEWER) {
            return true;
        }
        if (role == UserRole.PROJECT_MANAGER) {
            return UserHolder.getUserId() != null
                    && UserHolder.getUserId().equals(task.getCreatorId());
        }
        if (role == UserRole.EVALUATOR) {
            return assignmentService.lambdaQuery()
                    .eq(Assignment::getTaskId, task.getId())
                    .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                    .count() > 0;
        }
        return false;
    }

    private Result<?> checkTaskManagementAccess(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN) {
            return null;
        }
        if (role == UserRole.PROJECT_MANAGER
                && UserHolder.getUserId() != null
                && UserHolder.getUserId().equals(task.getCreatorId())) {
            return null;
        }
        return Result.error(403, "无权管理该任务");
    }

    private boolean hasManagementRole() {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        return role == UserRole.ADMIN || role == UserRole.PROJECT_MANAGER;
    }

    private boolean isAllowedTransition(int currentStatus, int targetStatus) {
        return (currentStatus == 0 && targetStatus == 1)
                || (currentStatus == 1 && targetStatus == 2)
                || (currentStatus == 3 && targetStatus == 4);
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待打分";
            case 1 -> "打分中";
            case 2 -> "待核算";
            case 3 -> "已核算";
            case 4 -> "已发布";
            default -> "未知";
        };
    }
}
