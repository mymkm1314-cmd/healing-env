package com.mym.healingenv.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskCreateDTO {
    @NotBlank(message = "空间名称不能为空")
    private String spaceName;

    private String purpose;
    private LocalDate evalDate;

    @NotNull(message = "指标库版本不能为空")
    private Long versionId;

    @Valid
    @NotEmpty(message = "至少需要一条任务指派")
    private List<AssignDTO> assigns;
}
