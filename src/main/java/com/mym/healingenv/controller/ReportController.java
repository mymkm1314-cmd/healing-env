package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ReportUpdateDTO;
import com.mym.healingenv.service.IReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 评价报告表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private IReportService reportService;

    @PostMapping("/generate/{taskId}")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> generate(@PathVariable Long taskId) {
        return reportService.generate(taskId);
    }

    @GetMapping("/{taskId}")
    public Result<?> getByTask(@PathVariable Long taskId) {
        return reportService.getByTask(taskId);
    }

    @PutMapping("/{taskId}")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> update(
            @PathVariable Long taskId,
            @Valid @RequestBody ReportUpdateDTO updateDTO) {
        return reportService.update(taskId, updateDTO);
    }
}
