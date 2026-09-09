package com.mym.healingenv.service.impl;

import com.mym.healingenv.entity.Task;
import com.mym.healingenv.mapper.TaskMapper;
import com.mym.healingenv.service.ITaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评价任务表 服务实现类
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements ITaskService {

}
