package com.mym.healingenv.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 评价任务表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被评空间名称
     */
    @TableField("space_name")
    private String spaceName;

    /**
     * 评价日期
     */
    @TableField("eval_date")
    private LocalDate evalDate;

    /**
     * 评价目的
     */
    @TableField("purpose")
    private String purpose;

    /**
     * 0待打分 1打分中 2待核算 3已核算 4已发布
     */
    @TableField("status")
    private Byte status;

    /**
     * 指标库版本ID
     */
    @TableField("version_id")
    private Long versionId;

    /**
     * 创建人(项目负责人)
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 总分(核算后)
     */
    @TableField("total_score")
    private BigDecimal totalScore;

    /**
     * 等级(核算后)
     */
    @TableField("level")
    private String level;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
