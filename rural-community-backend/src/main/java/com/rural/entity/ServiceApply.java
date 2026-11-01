package com.rural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rural.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_apply")
public class ServiceApply extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String applyNo;

    private Long itemId;

    private Long villagerId;

    private Integer status;

    private String applyContent;

    private String attachment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    private Long gridAuditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gridAuditTime;

    private String gridAuditComment;

    private Long handleBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;

    private Integer isOvertime;

    private String resultContent;

    /** 是否已重新提交（同一事项存在更新的申请记录） */
    @TableField(exist = false)
    private Boolean resubmitted;
}
