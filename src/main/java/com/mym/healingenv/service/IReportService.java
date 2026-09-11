package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.ReportUpdateDTO;
import com.mym.healingenv.entity.Report;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评价报告表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface IReportService extends IService<Report> {

    Result<?> generate(Long taskId);

    Result<?> getByTask(Long taskId);

    Result<?> update(Long taskId, ReportUpdateDTO updateDTO);
}
