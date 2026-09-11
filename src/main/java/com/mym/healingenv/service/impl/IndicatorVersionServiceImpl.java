package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.IndicatorVersion;
import com.mym.healingenv.mapper.IndicatorVersionMapper;
import com.mym.healingenv.service.IIndicatorVersionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 指标库版本表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class IndicatorVersionServiceImpl extends ServiceImpl<IndicatorVersionMapper, IndicatorVersion> implements IIndicatorVersionService {

    @Override
    public Result<?> ListAll() {
        List<IndicatorVersion> list = lambdaQuery()
                .orderByDesc(IndicatorVersion::getCreateTime)
                .list();
        return Result.success(list);
    }
}
