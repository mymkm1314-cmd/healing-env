package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.TaskCreateDTO;
import com.mym.healingenv.service.ITaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评价任务表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;
    @PostMapping
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> create(@Valid @RequestBody TaskCreateDTO taskCreateDTO) {
        return taskService.createTask(taskCreateDTO);
    }

    @PostMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return taskService.pageTasks(current, size, keyword, status);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return taskService.getDetail(id);
    }

    @PutMapping("/status/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return taskService.updateStatus(id, status);
    }

}
