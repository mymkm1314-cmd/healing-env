package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.Dimension;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 维度表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IDimensionService extends IService<Dimension> {

    Result<?> Dlist();

    Result<?> add(Dimension dimension);

    Result<?> resetD(Dimension dimension);

    Result<?> delete(Long id);
}
