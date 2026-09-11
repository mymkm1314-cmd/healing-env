package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.ConfigKeys;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.entity.*;
import com.mym.healingenv.mapper.ResultMapper;
import com.mym.healingenv.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 评定结果表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ResultServiceImpl extends ServiceImpl<ResultMapper, com.mym.healingenv.entity.Result>
        implements IResultService {

    @Autowired
    private ITaskService taskService;
    @Autowired
    private IScoreService scoreService;
    @Autowired
    private IIndicatorService indicatorService;
    @Autowired
    private IConfigService configService;
    @Autowired
    private IAssignmentService assignmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> calculate(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canManageTask(task)) {
            return Result.error(403, "无权核算该任务");
        }
        if (task.getStatus() == null || task.getStatus() != 2) {
            return Result.error("任务必须处于待核算状态");
        }
        long totalAssignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, taskId)
                .count();
        long submittedAssignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, taskId)
                .eq(Assignment::getSubmitStatus, (byte) 1)
                .count();
        if (totalAssignments == 0 || totalAssignments != submittedAssignments) {
            return Result.error("仍有维度未提交，无法核算");
        }

        // 查所有指标
        List<Indicator> indicators = indicatorService.lambdaQuery()
                .eq(Indicator::getVersionId, task.getVersionId())
                .list();
        if (indicators.isEmpty()) {
            return Result.error("任务使用的指标库没有指标");
        }
        Map<Long, Indicator> indicatorMap = indicators.stream()
                .collect(Collectors.toMap(Indicator::getId, i -> i));

        // 查所有评分
        List<Score> scores = scoreService.lambdaQuery()
                .eq(Score::getTaskId, taskId)
                .list();
        Map<Long, Score> scoreMap = new HashMap<>();
        for (Score score : scores) {
            if (!indicatorMap.containsKey(score.getIndicatorId())) {
                return Result.error("存在不属于当前指标版本的评分");
            }
            if (scoreMap.putIfAbsent(score.getIndicatorId(), score) != null) {
                return Result.error("存在重复评分，无法核算");
            }
        }
        for (Indicator indicator : indicators) {
            if (!scoreMap.containsKey(indicator.getId())) {
                return Result.error("指标[" + indicator.getCode() + "]尚未评分，无法核算");
            }
        }

        // 一次读取核算相关配置，缺失或非法值统一回退到系统默认值。
        Map<String, String> configValues = configService.getValues(ConfigKeys.ALLOWED_KEYS);
        BigDecimal bonusLimit = getConfigDecimal(
                configValues, ConfigKeys.BONUS_SCORE_LIMIT, new BigDecimal("5"));
        LevelThresholds thresholds = new LevelThresholds(
                getConfigDecimal(configValues, ConfigKeys.GRADE_EXCELLENT_MIN, new BigDecimal("90")),
                getConfigDecimal(configValues, ConfigKeys.GRADE_GOOD_MIN, new BigDecimal("80")),
                getConfigDecimal(configValues, ConfigKeys.GRADE_PASS_MIN, new BigDecimal("70")),
                getConfigDecimal(configValues, ConfigKeys.GRADE_BASIC_PASS_MIN, new BigDecimal("60")));
        CalculationSummary summary;
        try {
            summary = calculateSummary(indicators, scoreMap, bonusLimit, thresholds);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }

        // 删除旧 result（如有）
        this.lambdaUpdate()
                .eq(com.mym.healingenv.entity.Result::getTaskId, taskId)
                .remove();

        // 插入新 result
        com.mym.healingenv.entity.Result result = new com.mym.healingenv.entity.Result();
        result.setTaskId(taskId);
        result.setControlPass(summary.controlPass() ? (byte) 1 : (byte) 0);
        result.setBaseScore(summary.baseScore());
        result.setBonusScore(summary.bonusScore());
        result.setTotalScore(summary.totalScore());
        result.setFullScore(summary.fullScore());
        result.setRatio(summary.ratio());
        result.setLevel(summary.level());
        result.setCalcTime(LocalDateTime.now());
        if (!this.save(result)) {
            throw new IllegalStateException("核算结果保存失败");
        }

        // 回写 task
        task.setTotalScore(summary.totalScore());
        task.setLevel(summary.level());
        task.setStatus((byte) 3); // 已核算
        if (!taskService.updateById(task)) {
            throw new IllegalStateException("任务核算状态更新失败");
        }

        // 返回核算结果
        Map<String, Object> data = new HashMap<>();
        data.put("baseScore", summary.baseScore());
        data.put("bonusScore", summary.bonusScore());
        data.put("totalScore", summary.totalScore());
        data.put("fullScore", summary.fullScore());
        data.put("ratio", summary.ratio());
        data.put("level", summary.level());
        data.put("controlPass", summary.controlPass());
        return Result.success(data);
    }

    @Override
    public Result<?> getByTask(Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (!canViewTask(task)) {
            return Result.error(403, "无权查看该任务结果");
        }
        return Result.success(this.lambdaQuery()
                .eq(com.mym.healingenv.entity.Result::getTaskId, taskId)
                .list());
    }

    private boolean canManageTask(Task task) {
        UserRole role = UserRole.fromCode(UserHolder.getRole());
        if (role == UserRole.ADMIN) {
            return true;
        }
        return role == UserRole.PROJECT_MANAGER
                && UserHolder.getUserId() != null
                && UserHolder.getUserId().equals(task.getCreatorId());
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
        return role == UserRole.EVALUATOR
                && assignmentService.lambdaQuery()
                .eq(Assignment::getTaskId, task.getId())
                .eq(Assignment::getEvaluatorId, UserHolder.getUserId())
                .count() > 0;
    }

    static CalculationSummary calculateSummary(
            List<Indicator> indicators,
            Map<Long, Score> scoreMap,
            BigDecimal bonusLimit,
            LevelThresholds thresholds) {
        BigDecimal baseScore = BigDecimal.ZERO;
        BigDecimal basicFullScore = BigDecimal.ZERO;
        BigDecimal bonusScore = BigDecimal.ZERO;
        boolean allControlPass = true;

        for (Indicator indicator : indicators) {
            Score score = scoreMap.get(indicator.getId());
            if (score == null) {
                throw new IllegalArgumentException("存在未评分指标，无法核算");
            }
            Integer type = indicator.getType();
            if (Integer.valueOf(1).equals(type)) {
                if (score.getIsPass() == null || score.getIsPass() == 0) {
                    allControlPass = false;
                }
            } else if (Integer.valueOf(2).equals(type)) {
                if (score.getScore() != null) {
                    baseScore = baseScore.add(score.getScore());
                }
                if (indicator.getMaxScore() != null) {
                    basicFullScore = basicFullScore.add(indicator.getMaxScore());
                }
            } else if (Integer.valueOf(3).equals(type)) {
                bonusScore = bonusScore.add(calcBonusByGrade(score.getGrade(), indicator.getMaxScore()));
            } else {
                throw new IllegalArgumentException("指标类型不正确，无法核算");
            }
        }

        if (bonusLimit == null || bonusLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("加分上限配置不正确");
        }
        if (bonusScore.compareTo(bonusLimit) > 0) {
            bonusScore = bonusLimit;
        }
        BigDecimal fullScore = basicFullScore.add(bonusLimit);
        BigDecimal totalScore = baseScore.add(bonusScore);
        BigDecimal ratio = fullScore.compareTo(BigDecimal.ZERO) > 0
                ? totalScore.multiply(new BigDecimal(100)).divide(fullScore, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String level = allControlPass ? getLevelByRatio(ratio, thresholds) : "不合格";
        return new CalculationSummary(baseScore, bonusScore, totalScore, fullScore, ratio, level, allControlPass);
    }

    static LevelThresholds defaultThresholds() {
        return new LevelThresholds(
                new BigDecimal("90"),
                new BigDecimal("80"),
                new BigDecimal("70"),
                new BigDecimal("60"));
    }

    /**
     * 加分项按档位折算（可按实际业务调整）
     * 假设档位字符串："一档"=100%, "二档"=80%, "三档"=50%
     */
    private static BigDecimal calcBonusByGrade(String grade, BigDecimal maxScore) {
        if (grade == null || maxScore == null) return BigDecimal.ZERO;
        BigDecimal ratio;
        switch (grade) {
            case "一档": ratio = new BigDecimal("1.00"); break;
            case "二档": ratio = new BigDecimal("0.80"); break;
            case "三档": ratio = new BigDecimal("0.50"); break;
            default: ratio = BigDecimal.ZERO;
        }
        return maxScore.multiply(ratio);
    }

    /**
     * 按占比评定等级（阈值可从 config 读取）
     */
    private static String getLevelByRatio(BigDecimal ratio, LevelThresholds thresholds) {
        if (ratio.compareTo(thresholds.excellentMin()) >= 0) return "优秀";
        if (ratio.compareTo(thresholds.goodMin()) >= 0) return "良好";
        if (ratio.compareTo(thresholds.passMin()) >= 0) return "合格";
        if (ratio.compareTo(thresholds.basicPassMin()) >= 0) return "基本合格";
        return "不合格";
    }

    private static BigDecimal getConfigDecimal(
            Map<String, String> values, String key, BigDecimal defaultValue) {
        String configValue = values.get(key);
        if (configValue == null || configValue.isBlank()) return defaultValue;
        try {
            return new BigDecimal(configValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    record CalculationSummary(
            BigDecimal baseScore,
            BigDecimal bonusScore,
            BigDecimal totalScore,
            BigDecimal fullScore,
            BigDecimal ratio,
            String level,
            boolean controlPass) {
    }

    record LevelThresholds(
            BigDecimal excellentMin,
            BigDecimal goodMin,
            BigDecimal passMin,
            BigDecimal basicPassMin) {
        LevelThresholds {
            if (excellentMin == null || goodMin == null || passMin == null || basicPassMin == null
                    || excellentMin.compareTo(goodMin) <= 0
                    || goodMin.compareTo(passMin) <= 0
                    || passMin.compareTo(basicPassMin) <= 0) {
                throw new IllegalArgumentException("等级阈值配置不正确");
            }
        }
    }
}
