package com.mym.healingenv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.service.IIndicatorService;
import com.mym.healingenv.service.IIndicatorVersionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 指标项表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/api/indicator")
public class IndicatorController {

    @Autowired
    private IIndicatorService indicatorService;
    @Autowired
    private IIndicatorVersionService versionService;

    /**
     * 分页查询指标
     * GET /api/indicator/page?current=1&size=10&versionId=1&dimensionId=1&type=1&keyword=
     */

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long versionId,
            @RequestParam(required = false) Long dimensionId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {

        Page<Indicator> page = new Page<>(current, size);
        LambdaQueryWrapper<Indicator> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(versionId != null, Indicator::getVersionId, versionId)
                .eq(dimensionId != null, Indicator::getDimensionId, dimensionId)
                .eq(type != null, Indicator::getType, type)
                .and(keyword != null && !keyword.isEmpty(), w ->
                        w.like(Indicator::getCode, keyword)
                                .or().like(Indicator::getName, keyword))
                .orderByAsc(Indicator::getDimensionId)
                .orderByAsc(Indicator::getSort);
        return Result.success(indicatorService.page(page, wrapper));
    }

    /**
     * 查询某版本下所有指标（不分页，用于打分页）
     * GET /api/indicator/list?versionId=1&dimensionId=1
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam Long versionId,
            @RequestParam(required = false) Long dimensionId){

        return indicatorService.listOfVersion(versionId,dimensionId);
    }
    /**
     * 新增指标
     */
    @PostMapping
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> add(@Valid @RequestBody Indicator indicator){
        return indicatorService.add(indicator);
    }
    /**
     * 修改指标
     */
    @PutMapping
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> update(@Valid @RequestBody Indicator indicator){
        return indicatorService.reset(indicator);
    }
    /**
     * 删除指标
     */
    @DeleteMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> delete(@PathVariable Long id){
        return indicatorService.delete(id);
    }
    /**
     * 发布新版本（复制当前已发布版本的所有指标为新版本）
     * POST /api/indicator/publish?versionNo=v1.1&remark=调整艺术疗愈指标
     */
    @PostMapping("publish")
    @RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
    public Result<?> publishVersion(@RequestParam String versionNo, @RequestParam(required = false) String remark){
        return indicatorService.publish(versionNo,remark);
    }
    /**
     * 查询所有版本
     * GET /api/indicator/versions
     */
    @GetMapping("/versions")
    public Result<?> versions(){
        return versionService.ListAll();
    }

}
