package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Result;
import com.mym.healingenv.mapper.ResultMapper;
import com.mym.healingenv.service.IResultService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评定结果表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class ResultServiceImpl extends ServiceImpl<ResultMapper, Result> implements IResultService {

}
