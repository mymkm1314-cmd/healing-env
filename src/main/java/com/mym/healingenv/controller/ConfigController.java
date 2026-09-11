package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.ConfigUpdateDTO;
import com.mym.healingenv.service.IConfigService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 系统配置表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/api/config")
@RequireRole(UserRole.ADMIN)
public class ConfigController {

    @Autowired
    private IConfigService configService;

    @GetMapping("/list")
    public Result<?> list() {
        return configService.listConfigs();
    }

    @GetMapping("/{key}")
    public Result<?> get(@PathVariable String key) {
        return configService.getConfig(key);
    }

    @PutMapping("/{key}")
    public Result<?> update(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateDTO updateDTO) {
        return configService.updateConfig(key, updateDTO);
    }

    @PutMapping("/batch")
    public Result<?> updateBatch(@Valid @RequestBody List<@Valid ConfigUpdateDTO> updateDTOs) {
        return configService.updateConfigs(updateDTOs);
    }
}
