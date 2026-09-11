package com.mym.healingenv.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDTO {
    @NotNull(message = "维度ID不能为空")
    private Long dimensionId;

    @NotNull(message = "评价员ID不能为空")
    private Long evaluatorId;
}
