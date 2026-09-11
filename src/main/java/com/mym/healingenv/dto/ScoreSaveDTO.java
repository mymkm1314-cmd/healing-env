package com.mym.healingenv.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreSaveDTO {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "指标ID不能为空")
    private Long indicatorId;

    private Long dimensionId;
    private BigDecimal score;      // 得分（基本项）
    private String grade;          // 档位（加分项，如"一档/二档/三档"）
    private Byte isPass;           // 控制项是否达标 0否 1是
    private String evidenceUrl;    // 取证URL
    private String remark;         // 备注
}
