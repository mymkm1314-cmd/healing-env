package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.service.IResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评定结果表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/result")
public class ResultController {
    @Autowired
    private IResultService resultService;

    @PostMapping("/calculate/{taskId}")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> calculate(@PathVariable Long taskId){
        return resultService.calculate(taskId);
    }

    @GetMapping("/{taskId}")
    public Result<?> getByTask(@PathVariable Long taskId){
        return resultService.getByTask(taskId);
    }

}
