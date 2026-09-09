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
 * 评分记录表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("score")
public class Score implements Serializable {

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
     * 指标ID
     */
    @TableField("indicator_id")
    private Long indicatorId;

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
     * 得分
     */
    @TableField("score")
    private BigDecimal score;

    /**
     * 档位
     */
    @TableField("grade")
    private String grade;

    /**
     * 控制项是否达标 0否 1是(基本/加分项NULL)
     */
    @TableField("is_pass")
    private Byte isPass;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 取证图片URL
     */
    @TableField("evidence_url")
    private String evidenceUrl;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
