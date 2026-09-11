package com.mym.healingenv.controller;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.UserRole;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 任务指派表 前端控制器
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/assignment")
@RequireRole({UserRole.ADMIN, UserRole.PROJECT_MANAGER})
public class AssignmentController {

}
