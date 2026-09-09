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
 * 评价报告表
 * </p>
 *
 * @author mym
 * @since 2026-09-08
 */
@Getter
@Setter
@TableName("report")
public class Report implements Serializable {

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
     * 报告标题
     */
    @TableField("title")
    private String title;

    /**
     * 报告内容(JSON/富文本)
     */
    @TableField("content")
    private String content;

    /**
     * 弱项清单
     */
    @TableField("weak_items")
    private String weakItems;

    /**
     * 改进建议
     */
    @TableField("suggestions")
    private String suggestions;

    /**
     * 导出文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
