package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ScoreSaveDTO;
import com.mym.healingenv.service.IScoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评分记录表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/score")
public class ScoreController {
    @Autowired
    private IScoreService scoreService;

    @GetMapping("/list")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER, UserRole.EVALUATOR})
    public Result<?> list(@RequestParam Long taskId,
                          @RequestParam(required = false) Long dimensionId){
        return scoreService.listByTaskAndDimension(taskId,dimensionId);
    }

    @PostMapping
    @RequireRole({UserRole.ADMIN, UserRole.EVALUATOR})
    public Result<?> save(@Valid @RequestBody ScoreSaveDTO scoreDTO){
        return scoreService.saveScore(scoreDTO);
    }

    @PutMapping("/submit")
    @RequireRole({UserRole.ADMIN, UserRole.EVALUATOR})
    public Result<?> submit(@RequestParam Long taskId,@RequestParam Long dimensionId){
        return scoreService.submitDimension(taskId,dimensionId);
    }

}
