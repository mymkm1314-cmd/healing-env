package com.mym.healingenv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ReportUpdateDTO;
import com.mym.healingenv.entity.Assignment;
import com.mym.healingenv.entity.Dimension;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Report;
import com.mym.healingenv.entity.Score;
import com.mym.healingenv.entity.Task;
import com.mym.healingenv.mapper.ReportMapper;
import com.mym.healingenv.service.IAssignmentService;
import com.mym.healingenv.service.IDimensionService;
import com.mym.healingenv.service.IIndicatorService;
import com.mym.healingenv.service.IReportService;
import com.mym.healingenv.service.IResultService;
import com.mym.healingenv.service.IScoreService;
import com.mym.healingenv.service.ITaskService;
import com.mym.healingenv.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价报告表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final BigDecimal BASIC_WEAK_RATE = new BigDecimal("60");

    @Autowired
    private ITaskService taskService;
    @Autowired
    private IResultService resultService;
    @Autowired
    private IScoreService scoreService;
    @Autowired
    private IIndicatorService indicatorService;
    @Autowired
    private IDimensionService dimensionService;
    @Autowired
    private IAssignmentService assignmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> generate(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canManageReport(task)) {
            return Result.error(403, "无权生成该任务报告");
        }
        if (task.getStatus() == null || task.getStatus() < 3) {
            return Result.error("任务尚未核算，无法生成报告");
        }
        com.mym.healingenv.entity.Result result = resultService.lambdaQuery()
                .eq(com.mym.healingenv.entity.Result::getTaskId, taskId)
                .one();
        if (result == null) {
            return Result.error("核算结果不存在");
        }

        List<Indicator> indicators = indicatorService.lambdaQuery()
                .eq(Indicator::getVersionId, task.getVersionId())
                .orderByAsc(Indicator::getDimensionId)
                .orderByAsc(Indicator::getSort)
                .list();
        if (indicators.isEmpty()) {
            return Result.error("任务使用的指标库没有指标");
        }
        List<Score> scores = scoreService.lambdaQuery()
                .eq(Score::getTaskId, taskId)
                .list();
        Map<Long, String> dimensionNames = loadDimensionNames(indicators);

        ReportContent generated;
        try {
            generated = buildReport(task, result, indicators, scores, dimensionNames);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }

        Report report = findReport(taskId);
        LocalDateTime now = LocalDateTime.now();
        if (report == null) {
            report = new Report();
            report.setTaskId(taskId);
            report.setCreateTime(now);
        }
        report.setTitle(generated.title());
        report.setContent(generated.content());
        report.setWeakItems(generated.weakItems());
        report.setSuggestions(generated.suggestions());
        report.setUpdateTime(now);
        if (report.getId() == null) {
            if (!save(report)) {
                throw new IllegalStateException("报告保存失败");
            }
        } else if (!updateById(report)) {
            throw new IllegalStateException("报告更新失败");
        }
        return Result.success(report);
    }

    @Override
    public Result<?> getByTask(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canViewReport(task)) {
            return Result.error(403, "无权查看该任务报告");
        }
        Report report = findReport(taskId);
        if (report == null) {
            return Result.error("报告尚未生成");
        }
        com.mym.healingenv.entity.Result result = resultService.lambdaQuery()
                .eq(com.mym.healingenv.entity.Result::getTaskId, taskId)
                .one();
        List<Indicator> indicators = indicatorService.lambdaQuery()
                .eq(Indicator::getVersionId, task.getVersionId())
                .list();
        List<Score> scores = scoreService.lambdaQuery()
                .eq(Score::getTaskId, taskId)
                .list();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report", report);
        data.put("task", task);
        data.put("result", result);
        data.put("dimensionSummaries",
                buildDimensionSummaries(indicators, scores, loadDimensionNames(indicators)));
        return Result.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> update(Long taskId, ReportUpdateDTO updateDTO) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canManageReport(task)) {
            return Result.error(403, "无权编辑该任务报告");
        }
        Report report = findReport(taskId);
        if (report == null) {
            return Result.error("报告尚未生成");
        }
        report.setTitle(updateDTO.getTitle());
        report.setContent(updateDTO.getContent());
        report.setWeakItems(updateDTO.getWeakItems());
        report.setSuggestions(updateDTO.getSuggestions());
        report.setUpdateTime(LocalDateTime.now());
        if (!updateById(report)) {
            throw new IllegalStateException("报告更新失败");
        }
        return Result.success(report);
    }

    static ReportContent buildReport(
            Task task,
            com.mym.healingenv.entity.Result result,
            List<Indicator> indicators,
            List<Score> scores,
            Map<Long, String> dimensionNames) {
        Map<Long, Score> scoreMap = new HashMap<>();
        for (Score score : scores) {
            if (scoreMap.putIfAbsent(score.getIndicatorId(), score) != null) {
                throw new IllegalArgumentException("存在重复评分，无法生成报告");
            }
        }
        for (Indicator indicator : indicators) {
            if (!scoreMap.containsKey(indicator.getId())) {
                throw new IllegalArgumentException(
                        "指标[" + indicator.getCode() + "]尚未评分，无法生成报告");
            }
        }

        List<Map<String, Object>> indicatorItems = new ArrayList<>();
        List<String> weakItems = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (Indicator indicator : indicators) {
            Score score = scoreMap.get(indicator.getId());
            String weaknessReason = weaknessReason(indicator, score);
            boolean weak = weaknessReason != null;
            if (weak) {
                weakItems.add(indicator.getCode() + "：" + indicator.getName());
                suggestions.add(buildSuggestion(indicator));
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("indicatorId", indicator.getId());
            item.put("code", indicator.getCode());
            item.put("name", indicator.getName());
            item.put("type", indicator.getType());
            item.put("dimensionId", indicator.getDimensionId());
            item.put("dimensionName", dimensionNames.getOrDefault(
                    indicator.getDimensionId(), "维度" + indicator.getDimensionId()));
            item.put("maxScore", indicator.getMaxScore());
            item.put("score", score.getScore());
            item.put("grade", score.getGrade());
            item.put("isPass", score.getIsPass());
            item.put("weak", weak);
            item.put("weaknessReason", weaknessReason);
            item.put("basis", indicator.getBasis());
            item.put("evidenceUrl", score.getEvidenceUrl());
            indicatorItems.add(item);
        }

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("task", taskSummary(task));
        content.put("result", resultSummary(result));
        content.put("dimensionSummaries",
                buildDimensionSummaries(indicators, scores, dimensionNames));
        content.put("indicators", indicatorItems);
        content.put("weakItems", weakItems);
        content.put("suggestions", suggestions);

        String contentJson;
        try {
            contentJson = OBJECT_MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("报告内容生成失败", e);
        }
        return new ReportContent(
                task.getSpaceName() + "疗愈环境评价报告",
                contentJson,
                limitText(String.join("\n", weakItems)),
                limitText(String.join("\n", suggestions)));
    }

    private static List<Map<String, Object>> buildDimensionSummaries(
            List<Indicator> indicators,
            List<Score> scores,
            Map<Long, String> dimensionNames) {
        Map<Long, Score> scoreMap = scores.stream()
                .collect(Collectors.toMap(Score::getIndicatorId, score -> score, (first, second) -> first));
        Map<Long, DimensionAccumulator> accumulators = new LinkedHashMap<>();
        for (Indicator indicator : indicators) {
            DimensionAccumulator accumulator = accumulators.computeIfAbsent(
                    indicator.getDimensionId(),
                    dimensionId -> new DimensionAccumulator(dimensionId,
                            dimensionNames.getOrDefault(dimensionId, "维度" + dimensionId)));
            Score score = scoreMap.get(indicator.getId());
            accumulator.add(indicator, score);
        }
        return accumulators.values().stream()
                .map(DimensionAccumulator::toMap)
                .toList();
    }

    private static Map<String, Object> taskSummary(Task task) {
        Map<String, Object> taskSummary = new LinkedHashMap<>();
        taskSummary.put("id", task.getId());
        taskSummary.put("spaceName", task.getSpaceName());
        taskSummary.put("evalDate", Objects.toString(task.getEvalDate(), null));
        taskSummary.put("purpose", task.getPurpose());
        taskSummary.put("status", task.getStatus());
        return taskSummary;
    }

    private static Map<String, Object> resultSummary(com.mym.healingenv.entity.Result result) {
        Map<String, Object> resultSummary = new LinkedHashMap<>();
        resultSummary.put("baseScore", result.getBaseScore());
        resultSummary.put("bonusScore", result.getBonusScore());
        resultSummary.put("totalScore", result.getTotalScore());
        resultSummary.put("fullScore", result.getFullScore());
        resultSummary.put("ratio", result.getRatio());
        resultSummary.put("level", result.getLevel());
        resultSummary.put("controlPass", Integer.valueOf(1).equals(
                result.getControlPass() == null ? null : result.getControlPass().intValue()));
        return resultSummary;
    }

    private static String weaknessReason(Indicator indicator, Score score) {
        if (Integer.valueOf(1).equals(indicator.getType())) {
            return score.getIsPass() == null || score.getIsPass() == 0
                    ? "控制项未达标"
                    : null;
        }
        if (Integer.valueOf(2).equals(indicator.getType())) {
            BigDecimal maxScore = indicator.getMaxScore();
            if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) <= 0 || score.getScore() == null) {
                return "基本项缺少有效得分";
            }
            BigDecimal rate = score.getScore()
                    .multiply(new BigDecimal("100"))
                    .divide(maxScore, 2, RoundingMode.HALF_UP);
            return rate.compareTo(BASIC_WEAK_RATE) < 0 ? "得分率低于60%" : null;
        }
        return null;
    }

    private static String buildSuggestion(Indicator indicator) {
        String basis = indicator.getBasis() == null || indicator.getBasis().isBlank()
                ? "评价依据"
                : indicator.getBasis();
        return indicator.getCode() + "：建议按照“" + basis + "”完成整改并补充取证材料。";
    }

    private static String limitText(String value) {
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    private Map<Long, String> loadDimensionNames(List<Indicator> indicators) {
        Set<Long> dimensionIds = indicators.stream()
                .map(Indicator::getDimensionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (dimensionIds.isEmpty()) {
            return Map.of();
        }
        return dimensionService.listByIds(dimensionIds).stream()
                .collect(Collectors.toMap(Dimension::getId, Dimension::getName));
    }

    private Report findReport(Long taskId) {
        return getOne(new LambdaQueryWrapper<Report>().eq(Report::getTaskId, taskId));
    }

    private boolean canManageReport(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN) {
            return true;
        }
        return role == UserRole.PROJECT_MANAGER
                && UserHolder.getUserId() != null
                && UserHolder.getUserId().equals(task.getCreatorId());
    }

    private boolean canViewReport(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN || role == UserRole.VIEWER) {
            return true;
        }
        if (role == UserRole.PROJECT_MANAGER) {
            return UserHolder.getUserId() != null
                    && UserHolder.getUserId().equals(task.getCreatorId());
        }
        return role == UserRole.EVALUATOR
                && assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, task.getId())
                .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                .count() > 0;
    }

    record ReportContent(String title, String content, String weakItems, String suggestions) {
    }

    private static final class DimensionAccumulator {
        private final Long dimensionId;
        private final String dimensionName;
        private BigDecimal basicScore = BigDecimal.ZERO;
        private BigDecimal basicFullScore = BigDecimal.ZERO;
        private BigDecimal bonusScore = BigDecimal.ZERO;
        private BigDecimal bonusFullScore = BigDecimal.ZERO;
        private int controlTotal;
        private int controlPassCount;
        private int weakItemCount;

        private DimensionAccumulator(Long dimensionId, String dimensionName) {
            this.dimensionId = dimensionId;
            this.dimensionName = dimensionName;
        }

        private void add(Indicator indicator, Score score) {
            if (score == null) {
                return;
            }
            if (Integer.valueOf(1).equals(indicator.getType())) {
                controlTotal++;
                if (Integer.valueOf(1).equals(
                        score.getIsPass() == null ? null : score.getIsPass().intValue())) {
                    controlPassCount++;
                } else {
                    weakItemCount++;
                }
            } else if (Integer.valueOf(2).equals(indicator.getType())) {
                if (score.getScore() != null) {
                    basicScore = basicScore.add(score.getScore());
                }
                if (indicator.getMaxScore() != null) {
                    basicFullScore = basicFullScore.add(indicator.getMaxScore());
                }
                if (weaknessReason(indicator, score) != null) {
                    weakItemCount++;
                }
            } else if (Integer.valueOf(3).equals(indicator.getType())) {
                if (indicator.getMaxScore() != null) {
                    bonusFullScore = bonusFullScore.add(indicator.getMaxScore());
                }
                bonusScore = bonusScore.add(calculateBonus(indicator, score));
            }
        }

        private Map<String, Object> toMap() {
            BigDecimal basicRate = basicFullScore.compareTo(BigDecimal.ZERO) > 0
                    ? basicScore.multiply(new BigDecimal("100"))
                    .divide(basicFullScore, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("dimensionId", dimensionId);
            summary.put("dimensionName", dimensionName);
            summary.put("basicScore", basicScore);
            summary.put("basicFullScore", basicFullScore);
            summary.put("basicRate", basicRate);
            summary.put("bonusScore", bonusScore);
            summary.put("bonusFullScore", bonusFullScore);
            summary.put("controlTotal", controlTotal);
            summary.put("controlPassCount", controlPassCount);
            summary.put("controlPass", controlTotal == 0 || controlTotal == controlPassCount);
            summary.put("weakItemCount", weakItemCount);
            return summary;
        }

        private static BigDecimal calculateBonus(Indicator indicator, Score score) {
            if (score.getGrade() == null || indicator.getMaxScore() == null) {
                return BigDecimal.ZERO;
            }
            BigDecimal ratio = switch (score.getGrade()) {
                case "一档" -> new BigDecimal("1.00");
                case "二档" -> new BigDecimal("0.80");
                case "三档" -> new BigDecimal("0.50");
                default -> BigDecimal.ZERO;
            };
            return indicator.getMaxScore().multiply(ratio);
        }
    }
}
