package com.mym.healingenv.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 任务指派表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("assignment")
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 维度ID
     */
    @TableField("dimension_id")
    private Long dimensionId;

    /**
     * 评价员ID
     */
    @TableField("evaluator_id")
    private Long evaluatorId;

    /**
     * 0未提交 1已提交
     */
    @TableField("submit_status")
    private Byte submitStatus;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
