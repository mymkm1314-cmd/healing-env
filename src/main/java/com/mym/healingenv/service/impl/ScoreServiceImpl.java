package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Score;
import com.mym.healingenv.mapper.ScoreMapper;
import com.mym.healingenv.service.IScoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评分记录表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements IScoreService {

}
