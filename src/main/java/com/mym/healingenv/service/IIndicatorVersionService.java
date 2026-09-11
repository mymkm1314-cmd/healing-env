package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.IndicatorVersion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 指标库版本表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IIndicatorVersionService extends IService<IndicatorVersion> {

    Result<?> ListAll();
}
