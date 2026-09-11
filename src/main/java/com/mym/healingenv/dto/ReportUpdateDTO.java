package com.mym.healingenv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportUpdateDTO {
    @NotBlank(message = "报告标题不能为空")
    @Size(max = 200, message = "报告标题不能超过200个字符")
    private String title;

    @NotBlank(message = "报告内容不能为空")
    private String content;

    @Size(max = 1000, message = "弱项清单不能超过1000个字符")
    private String weakItems;

    @Size(max = 1000, message = "改进建议不能超过1000个字符")
    private String suggestions;
}
