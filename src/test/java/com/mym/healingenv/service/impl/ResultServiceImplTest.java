package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultServiceImplTest {

    @Test
    void summaryUsesAllBasicIndicatorsAndCapsBonus() {
        Indicator control = indicator(1L, 1, "0");
        Indicator basicOne = indicator(2L, 2, "10");
        Indicator basicTwo = indicator(3L, 2, "20");
        Indicator bonus = indicator(4L, 3, "5");

        Score controlScore = score(control.getId(), null, null, 1);
        Score basicOneScore = score(basicOne.getId(), new BigDecimal("10"), null, null);
        Score basicTwoScore = score(basicTwo.getId(), new BigDecimal("5"), null, null);
        Score bonusScore = score(bonus.getId(), null, "一档", null);

        ResultServiceImpl.CalculationSummary summary = ResultServiceImpl.calculateSummary(
                List.of(control, basicOne, basicTwo, bonus),
                Map.of(
                        control.getId(), controlScore,
                        basicOne.getId(), basicOneScore,
                        basicTwo.getId(), basicTwoScore,
                        bonus.getId(), bonusScore),
                new BigDecimal("5"),
                ResultServiceImpl.defaultThresholds());

        assertDecimalEquals("15", summary.baseScore());
        assertDecimalEquals("5", summary.bonusScore());
        assertDecimalEquals("20", summary.totalScore());
        assertDecimalEquals("35", summary.fullScore());
        assertTrue(summary.controlPass());
    }

    @Test
    void failedControlItemMakesResultUnqualified() {
        Indicator control = indicator(1L, 1, "0");
        Score controlScore = score(control.getId(), null, null, 0);

        ResultServiceImpl.CalculationSummary summary = ResultServiceImpl.calculateSummary(
                List.of(control),
                Map.of(control.getId(), controlScore),
                new BigDecimal("5"),
                ResultServiceImpl.defaultThresholds());

        assertFalse(summary.controlPass());
        assertEquals("不合格", summary.level());
    }

    @Test
    void configuredThresholdCanChangeLevel() {
        Indicator basic = indicator(2L, 2, "100");
        Score basicScore = score(basic.getId(), new BigDecimal("75"), null, null);
        ResultServiceImpl.LevelThresholds thresholds =
                new ResultServiceImpl.LevelThresholds(
                        new BigDecimal("70"),
                        new BigDecimal("60"),
                        new BigDecimal("50"),
                        new BigDecimal("40"));

        ResultServiceImpl.CalculationSummary summary = ResultServiceImpl.calculateSummary(
                List.of(basic),
                Map.of(basic.getId(), basicScore),
                BigDecimal.ZERO,
                thresholds);

        assertEquals("优秀", summary.level());
    }

    private Indicator indicator(Long id, int type, String maxScore) {
        Indicator indicator = new Indicator();
        indicator.setId(id);
        indicator.setType(type);
        indicator.setMaxScore(new BigDecimal(maxScore));
        return indicator;
    }

    private Score score(Long indicatorId, BigDecimal value, String grade, Integer isPass) {
        Score score = new Score();
        score.setIndicatorId(indicatorId);
        score.setScore(value);
        score.setGrade(grade);
        score.setIsPass(isPass == null ? null : isPass.byteValue());
        return score;
    }

    private void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
