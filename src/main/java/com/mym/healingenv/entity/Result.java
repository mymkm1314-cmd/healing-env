package com.mym.healingenv.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 评定结果表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("result")
public class Result implements Serializable {

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
     * 控制项是否全部通过 0否 1是
     */
    @TableField("control_pass")
    private Byte controlPass;

    /**
     * 基本分
     */
    @TableField("base_score")
    private BigDecimal baseScore;

    /**
     * 加分分(封顶后)
     */
    @TableField("bonus_score")
    private BigDecimal bonusScore;

    /**
     * 总分
     */
    @TableField("total_score")
    private BigDecimal totalScore;

    /**
     * 满分
     */
    @TableField("full_score")
    private BigDecimal fullScore;

    /**
     * 占比%
     */
    @TableField("ratio")
    private BigDecimal ratio;

    /**
     * 等级
     */
    @TableField("level")
    private String level;

    /**
     * 核算时间
     */
    @TableField("calc_time")
    private LocalDateTime calcTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
