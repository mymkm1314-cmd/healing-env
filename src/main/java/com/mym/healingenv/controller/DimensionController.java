package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.entity.Dimension;
import com.mym.healingenv.service.IDimensionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 维度表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/api/dimension")
public class DimensionController {

    @Autowired
    private IDimensionService dimensionService;

    /**
     * 查询所有维度（按排序）
     * GET /api/dimension/list
     */
    @GetMapping("/list")
    public Result<?> list() {
        return dimensionService.Dlist();

    }

    /**
     * 新增维度
     */
    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public Result<?> add(@Valid @RequestBody Dimension dimension) {
        return dimensionService.add(dimension);
    }
    /**
     * 修改维度
     */
    @PutMapping
    @RequireRole(UserRole.ADMIN)
    public Result<?> update(@Valid @RequestBody Dimension dimension) {
        return dimensionService.resetD(dimension);
    }
    /**
     * 删除维度
     */
    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public Result<?> delete(@PathVariable Long id) {
        return dimensionService.delete(id);
    }
}
