package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ScoreSaveDTO;
import com.mym.healingenv.entity.Score;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评分记录表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IScoreService extends IService<Score> {

    Result<?> listByTaskAndDimension(Long taskId, Long dimensionId);

    Result<?> saveScore(ScoreSaveDTO scoreDTO);

    Result<?> submitDimension(Long taskId, Long dimensionId);
}
