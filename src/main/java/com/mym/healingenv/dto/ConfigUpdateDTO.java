package com.mym.healingenv.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateDTO {
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String remark;
}
