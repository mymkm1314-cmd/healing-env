package com.mym.healingenv.service.impl;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.Dimension;
import com.mym.healingenv.mapper.DimensionMapper;
import com.mym.healingenv.service.IDimensionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 维度表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class DimensionServiceImpl extends ServiceImpl<DimensionMapper, Dimension> implements IDimensionService {

    @Override
    public Result<?> Dlist() {
        List<Dimension> list = lambdaQuery().orderByAsc(Dimension::getSort).list();
        return Result.success(list);
    }

    @Override
    public Result<?> add(Dimension dimension) {
        save(dimension);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> resetD(Dimension dimension) {
        updateById(dimension);
        return Result.success("修改成功");
    }

    @Override
    public Result<?> delete(Long id) {
        removeById(id);
        return Result.success("删除成功");
    }

}
