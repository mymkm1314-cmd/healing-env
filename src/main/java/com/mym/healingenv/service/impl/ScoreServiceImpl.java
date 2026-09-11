package com.mym.healingenv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ScoreSaveDTO;
import com.mym.healingenv.entity.Assignment;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Score;
import com.mym.healingenv.entity.Task;
import com.mym.healingenv.mapper.ScoreMapper;
import com.mym.healingenv.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 评分记录表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements IScoreService {

    @Autowired
    private ITaskService taskService;
    @Autowired
    private IIndicatorService indicatorService;
    @Autowired
    private IAssignmentService assignmentService;

    /**
     * 查询任务下某维度的指标+评分
     */
    @Override
    public Result<?> listByTaskAndDimension(Long taskId, Long dimensionId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canViewTask(task)) {
            return Result.error(403, "无权查看该任务评分");
        }
        LambdaQueryWrapper<Indicator> indWrapper = new LambdaQueryWrapper<>();
        indWrapper.eq(Indicator::getVersionId, task.getVersionId());
        if (dimensionId != null) {
            indWrapper.eq(Indicator::getDimensionId, dimensionId);
        } else if (UserRole.fromCode(UserHolder.getRole()) == UserRole.EVALUATOR) {
            List<Long> assignedDimensionIds = assignmentService.lambdaQuery()
                    .eq(Assignment::getTaskId, taskId)
                    .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                    .list()
                    .stream()
                    .map(Assignment::getDimensionId)
                    .distinct()
                    .toList();
            if (assignedDimensionIds.isEmpty()) {
                return Result.error(403, "当前用户未被指派该任务");
            }
            indWrapper.in(Indicator::getDimensionId, assignedDimensionIds);
        }
        indWrapper.orderByAsc(Indicator::getSort);
        List<Indicator> indicatorList = indicatorService.list(indWrapper);

        LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(Score::getTaskId, task.getId());
        if (dimensionId != null) {
            scoreWrapper.eq(Score::getDimensionId, dimensionId);
        }
        if (UserRole.fromCode(UserHolder.getRole()) == UserRole.EVALUATOR) {
            scoreWrapper.eq(Score::getEvaluatorId, UserHolder.getUserId());
        }
        List<Score> scores = this.list(scoreWrapper);
        Map<Long, Score> scoreMap = scores.stream()
                .collect(Collectors.toMap(Score::getIndicatorId, s ->s,(a,b)-> a));

        // 组装返回
        List<Map<String, Object>> result = new ArrayList<>();
        for (Indicator ind : indicatorList) {
            Map<String, Object> item = new HashMap<>();
            item.put("indicator", ind);
            item.put("score", scoreMap.get(ind.getId()));
            result.add(item);
        }
        return Result.success(result);
    }
    /**
     * 保存单项评分（新增或更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> saveScore(ScoreSaveDTO dto) {
        Task task = taskService.getById(dto.getTaskId());
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!isEditableStatus(task)) {
            return Result.error("任务当前状态不允许评分");
        }
        Indicator indicator = indicatorService.getById(dto.getIndicatorId());
        if (indicator == null) {
            return Result.error("指标不存在");
        }
        if (!Objects.equals(task.getVersionId(), indicator.getVersionId())) {
            return Result.error("指标不属于该任务使用的版本");
        }
        Long dimensionId = indicator.getDimensionId();
        if (dto.getDimensionId() != null && !dto.getDimensionId().equals(dimensionId)) {
            return Result.error("指标与维度不匹配");
        }
        Long evaluatorId = UserHolder.getUserId();
        Assignment assignment = findAssignment(task.getId(), dimensionId, evaluatorId);
        if (assignment == null) {
            return Result.error(403, "当前用户未被指派该维度");
        }
        if (Integer.valueOf(1).equals(assignment.getSubmitStatus())) {
            return Result.error("该维度已提交，不能继续修改评分");
        }

        Integer type = indicator.getType();
        if (Integer.valueOf(1).equals(type)) {
            // 控制项：必须有 isPass（0 或 1）
            if (dto.getIsPass() == null) {
                return Result.error("控制项必须选择达标/不达标");
            }
            if (dto.getIsPass() != 0 && dto.getIsPass() != 1) {
                return Result.error("达标状态只能为 0 或 1");
            }
        } else if (Integer.valueOf(2).equals(type)) {
            // 基本项：必须有 score，且 0 ≤ score ≤ maxScore
            if (dto.getScore() == null) {
                return Result.error("基本项必须录入分值");
            }
            BigDecimal max = indicator.getMaxScore();
            if (max != null && dto.getScore().compareTo(max) > 0) {
                return Result.error("分值不能超过满分 " + max);
            }
            if (dto.getScore().compareTo(BigDecimal.ZERO) < 0) {
                return Result.error("分值不能为负数");
            }
        } else if (Integer.valueOf(3).equals(type)) {
            // 加分项：必须有 grade，且只能是一/二/三档
            if (dto.getGrade() == null || dto.getGrade().isEmpty()) {
                return Result.error("加分项必须选择档位");
            }
            if (!dto.getGrade().equals("一档")
                    && !dto.getGrade().equals("二档")
                    && !dto.getGrade().equals("三档")) {
                return Result.error("档位只能为：一档/二档/三档");
            }
        } else {
            return Result.error("指标类型不正确");
        }

        // 查是否已有评分
        List<Score> existingScores = this.lambdaQuery()
                .eq(Score::getTaskId, dto.getTaskId())
                .eq(Score::getIndicatorId, dto.getIndicatorId())
                .eq(Score::getEvaluatorId, evaluatorId)
                .list();
        if (existingScores.size() > 1) {
            return Result.error("存在重复评分记录，请联系管理员处理");
        }
        Score exist = existingScores.isEmpty() ? null : existingScores.get(0);
        if (exist != null) {
            // 更新
            exist.setScore(dto.getScore());
            exist.setGrade(dto.getGrade());
            exist.setIsPass(dto.getIsPass());
            exist.setEvidenceUrl(dto.getEvidenceUrl());
            exist.setRemark(dto.getRemark());
            if (!this.updateById(exist)) {
                throw new IllegalStateException("评分更新失败");
            }
        } else {
            // 新增
            Score score = new Score();
            score.setTaskId(dto.getTaskId());
            score.setIndicatorId(dto.getIndicatorId());
            score.setDimensionId(dimensionId);
            score.setEvaluatorId(evaluatorId);
            score.setScore(dto.getScore());
            score.setGrade(dto.getGrade());
            score.setIsPass(dto.getIsPass());
            score.setEvidenceUrl(dto.getEvidenceUrl());
            score.setRemark(dto.getRemark());
            if (!this.save(score)) {
                throw new IllegalStateException("评分保存失败");
            }
        }
        return Result.success("保存成功");
    }

    /**
     * 提交某维度的评分（更新 assignment.submitStatus=1）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> submitDimension(Long taskId, Long dimensionId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!isEditableStatus(task)) {
            return Result.error("任务当前状态不允许提交评分");
        }
        Assignment assignment = findAssignment(taskId, dimensionId, UserHolder.getUserId());
        if (assignment == null) {
            return Result.error(403, "当前用户未被指派该维度");
        }
        if (Integer.valueOf(1).equals(assignment.getSubmitStatus())) {
            return Result.error("该维度已经提交");
        }
        // 查该维度下所有指标
        List<Indicator> indicators = indicatorService.lambdaQuery()
                .eq(Indicator::getVersionId, task.getVersionId())
                .eq(Indicator::getDimensionId, dimensionId)
                .list();
        if (indicators.isEmpty()) {
            return Result.error("该维度没有可提交的指标");
        }
        List<Long> indicatorIds = indicators.stream()
                .map(Indicator::getId)
                .collect(Collectors.toList());
        Set<Long> scoredIndicatorIds = this.lambdaQuery()
                .eq(Score::getTaskId, taskId)
                .eq(Score::getEvaluatorId, UserHolder.getUserId())
                .in(Score::getIndicatorId, indicatorIds)
                .list()
                .stream()
                .map(Score::getIndicatorId)
                .collect(Collectors.toCollection(HashSet::new));
        if (scoredIndicatorIds.size() < indicators.size()) {
            return Result.error("还有 " + (indicators.size() - scoredIndicatorIds.size()) + " 项未评分，无法提交");
        }
        // 只更新当前评价员的指派记录
        boolean updated = assignmentService.lambdaUpdate()
                .eq(Assignment::getId, assignment.getId())
                .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                .set(Assignment::getSubmitStatus, (byte) 1)
                .set(Assignment::getSubmitTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return Result.error("未找到该维度的指派记录");
        }
        // 若所有维度都已提交，更新任务状态为"待核算"
        long totalAssignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, taskId)
                .count();
        long submittedAssignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, taskId)
                .eq(Assignment::getSubmitStatus, (byte) 1)
                .count();
        if (totalAssignments > 0 && totalAssignments == submittedAssignments && task.getStatus() < 2) {
            task.setStatus((byte) 2); // 待核算
            if (!taskService.updateById(task)) {
                throw new IllegalStateException("任务状态更新失败");
            }
        }
        return Result.success("维度评分提交成功");
    }

    private Assignment findAssignment(Long taskId, Long dimensionId, Long evaluatorId) {
        if (dimensionId == null || evaluatorId == null) {
            return null;
        }
        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, taskId)
                .eq(Assignment::getDimensionId, dimensionId)
                .eq(Assignment::getEvaluatorId, evaluatorId)
                .list();
        return assignments.size() == 1 ? assignments.get(0) : null;
    }

    private boolean canViewTask(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN) {
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

    private boolean isEditableStatus(Task task) {
        int status = task.getStatus() == null ? 0 : task.getStatus();
        return status == 0 || status == 1;
    }
}
