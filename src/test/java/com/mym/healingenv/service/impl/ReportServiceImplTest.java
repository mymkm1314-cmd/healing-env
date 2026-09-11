package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Result;
import com.mym.healingenv.entity.Score;
import com.mym.healingenv.entity.Task;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceImplTest {

    @Test
    void generatedReportContainsWeakItemsAndSuggestions() {
        Task task = task();
        Result result = result();
        Indicator control = indicator(1L, 1, "H-C01", "无障碍通道全覆盖", "0");
        Indicator basic = indicator(2L, 2, "H-B01", "诊室隐私隔断", "10");
        Score controlScore = score(control.getId(), null, 0);
        Score basicScore = score(basic.getId(), new BigDecimal("4"), null);

        ReportServiceImpl.ReportContent content = ReportServiceImpl.buildReport(
                task,
                result,
                List.of(control, basic),
                List.of(controlScore, basicScore),
                Map.of(1L, "人性化"));

        org.junit.jupiter.api.Assertions.assertEquals(
                "测试空间疗愈环境评价报告", content.title());
        assertTrue(content.weakItems().contains("H-C01"));
        assertTrue(content.weakItems().contains("H-B01"));
        assertTrue(content.suggestions().contains("H-C01"));
        assertTrue(content.suggestions().contains("H-B01"));
        assertTrue(content.content().contains("\"level\":\"基本合格\""));
        assertTrue(content.content().contains("\"dimensionName\":\"人性化\""));
    }

    @Test
    void bonusItemsAreNotReportedAsWeakItems() {
        Task task = task();
        Result result = result();
        Indicator bonus = indicator(3L, 3, "H-A01", "个性化病房装饰", "3");
        Score bonusScore = score(bonus.getId(), null, null);
        bonusScore.setGrade("三档");

        ReportServiceImpl.ReportContent content = ReportServiceImpl.buildReport(
                task,
                result,
                List.of(bonus),
                List.of(bonusScore),
                Map.of(1L, "人性化"));

        assertTrue(content.weakItems().isEmpty());
        assertTrue(content.suggestions().isEmpty());
    }

    private Task task() {
        Task task = new Task();
        task.setId(1L);
        task.setSpaceName("测试空间");
        task.setEvalDate(LocalDate.of(2026, 9, 10));
        task.setPurpose("验证报告生成");
        return task;
    }

    private Result result() {
        Result result = new Result();
        result.setTaskId(1L);
        result.setControlPass((byte) 0);
        result.setBaseScore(new BigDecimal("4"));
        result.setBonusScore(BigDecimal.ZERO);
        result.setTotalScore(new BigDecimal("4"));
        result.setFullScore(new BigDecimal("15"));
        result.setRatio(new BigDecimal("26.67"));
        result.setLevel("基本合格");
        return result;
    }

    private Indicator indicator(Long id, int type, String code, String name, String maxScore) {
        Indicator indicator = new Indicator();
        indicator.setId(id);
        indicator.setType(type);
        indicator.setCode(code);
        indicator.setName(name);
        indicator.setDimensionId(1L);
        indicator.setMaxScore(new BigDecimal(maxScore));
        indicator.setBasis("现场检查");
        return indicator;
    }

    private Score score(Long indicatorId, BigDecimal value, Integer isPass) {
        Score score = new Score();
        score.setTaskId(1L);
        score.setIndicatorId(indicatorId);
        score.setScore(value);
        score.setIsPass(isPass == null ? null : isPass.byteValue());
        return score;
    }
}
