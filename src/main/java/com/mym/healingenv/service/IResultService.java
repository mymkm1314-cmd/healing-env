package com.mym.healingenv.service;

import com.mym.healingenv.entity.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评定结果表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IResultService extends IService<Result> {

    com.mym.healingenv.common.Result<?> calculate(Long taskId);

    com.mym.healingenv.common.Result<?> getByTask(Long taskId);
}
