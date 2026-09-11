package com.mym.healingenv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.mym.healingenv.common.Result;
import com.mym.healingenv.entity.Indicator;
import com.mym.healingenv.entity.IndicatorVersion;
import com.mym.healingenv.mapper.IndicatorMapper;
import com.mym.healingenv.service.IIndicatorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mym.healingenv.service.IIndicatorVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 指标项表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class IndicatorServiceImpl extends ServiceImpl<IndicatorMapper, Indicator> implements IIndicatorService {

    @Autowired
    private IIndicatorVersionService versionService;
    @Override
    public Result<?> listOfVersion(Long versionId, Long dimensionId) {
        LambdaQueryWrapper<Indicator> queryWrapper = new LambdaQueryWrapper<Indicator>();
        queryWrapper.eq(Indicator::getVersionId,versionId)
                .eq(dimensionId!=null,Indicator::getDimensionId,dimensionId)
                .orderByAsc(Indicator::getDimensionId)
                .orderByAsc(Indicator::getSort);
        return Result.success(list(queryWrapper));
    }

    @Override
    public Result<?> add(Indicator indicator) {
        save(indicator);
        return Result.success("新增成功");
    }

    @Override
    public Result<?> reset(Indicator indicator) {
        updateById(indicator);
        return Result.success("修改成功");
    }

    @Override
    public Result<?> delete(Long id) {
        removeById(id);
        return Result.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> publish(String versionNo, String remark) {
        if (versionNo == null || versionNo.isBlank()) {
            return Result.error("版本号不能为空");
        }
        Long count = versionService.lambdaQuery().eq(IndicatorVersion::getVersionNo, versionNo).count();
        if (count>0){
            return Result.error("版本已经存在");
        }
        //创建新版本
        IndicatorVersion newVersion = new IndicatorVersion();
        newVersion.setVersionNo(versionNo);
        newVersion.setRemark(remark);
        newVersion.setStatus(1);
        if (!versionService.save(newVersion)) {
            throw new IllegalStateException("版本创建失败");
        }
        //复制当前最新的版本指标
        IndicatorVersion last = versionService.lambdaQuery()
                .eq(IndicatorVersion::getStatus, 1)
                .ne(IndicatorVersion::getId, newVersion.getId())
                .orderByDesc(IndicatorVersion::getId)
                .last("limit 1")
                .one();
        if (last != null) {
            List<Indicator> oldIndicators = lambdaQuery()
                    .eq(Indicator::getVersionId, last.getId())
                    .list();
            for (Indicator oldIndicator : oldIndicators) {
            oldIndicator.setId(null);
            oldIndicator.setVersionId(newVersion.getId());
            }
            if (!oldIndicators.isEmpty()) {
                if (!saveBatch(oldIndicators)) {
                    throw new IllegalStateException("指标复制失败");
                }
            }
        }
        return Result.success("版本" + versionNo + "发布成功");
    }


}
