package com.mym.healingenv.service;

import com.mym.healingenv.common.Result;
import com.mym.healingenv.dto.TaskCreateDTO;
import com.mym.healingenv.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评价任务表 服务类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
public interface ITaskService extends IService<Task> {

    Result<?> createTask(TaskCreateDTO taskCreateDTO);
    Result<?> updateStatus(Long taskId,Integer status);

    Result<?> pageTasks(Integer current, Integer size, String keyword, Integer status);

    Result<?> getDetail(Long id);
}
