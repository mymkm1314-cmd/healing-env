package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.Indicator;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 指标项表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IIndicatorService extends IService<Indicator> {

    Result<?> listOfVersion(Long versionId, Long dimensionId);

    Result<?> add(Indicator indicator);

    Result<?> reset(Indicator indicator);

    Result<?> delete(Long id);

    Result<?> publish(String versionNo, String remark);
}
