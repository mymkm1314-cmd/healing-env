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
 * 指标项表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("indicator")
public class Indicator implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标库版本ID
     */
    @TableField("version_id")
    private Long versionId;

    /**
     * 维度ID
     */
    @TableField("dimension_id")
    private Long dimensionId;

    /**
     * 指标编号
     */
    @TableField("code")
    private String code;

    /**
     * 指标名称
     */
    @TableField("name")
    private String name;

    /**
     * 1控制项 2基本项 3加分项
     */
    @TableField("type")
    private Integer type;

    /**
     * 满分(控制项为0)
     */
    @TableField("max_score")
    private BigDecimal maxScore;

    /**
     * 计分方式 1达标/不达标 2分档 3录入分值
     */
    @TableField("grade_mode")
    private Byte gradeMode;

    /**
     * 评判依据
     */
    @TableField("basis")
    private String basis;

    /**
     * 取证方式
     */
    @TableField("evidence_type")
    private String evidenceType;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    @TableField("create_time")
    private LocalDateTime createTime;
}
