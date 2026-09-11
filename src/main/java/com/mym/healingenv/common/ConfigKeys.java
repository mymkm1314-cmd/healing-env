package com.mym.healingenv.common;

import java.util.Set;

public final class ConfigKeys {
    public static final String BONUS_SCORE_LIMIT = "bonus_score_limit";
    public static final String GRADE_EXCELLENT_MIN = "grade_excellent_min";
    public static final String GRADE_GOOD_MIN = "grade_good_min";
    public static final String GRADE_PASS_MIN = "grade_pass_min";
    public static final String GRADE_BASIC_PASS_MIN = "grade_basic_pass_min";

    public static final Set<String> ALLOWED_KEYS = Set.of(
            BONUS_SCORE_LIMIT,
            GRADE_EXCELLENT_MIN,
            GRADE_GOOD_MIN,
            GRADE_PASS_MIN,
            GRADE_BASIC_PASS_MIN);

    public static final Set<String> GRADE_THRESHOLD_KEYS = Set.of(
            GRADE_EXCELLENT_MIN,
            GRADE_GOOD_MIN,
            GRADE_PASS_MIN,
            GRADE_BASIC_PASS_MIN);

    private ConfigKeys() {
    }
}
