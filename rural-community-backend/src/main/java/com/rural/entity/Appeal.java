package com.rural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rural.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appeal")
public class Appeal extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String appealNo;

    private Long villagerId;

    private String category;

    private String title;

    private String content;

    private String location;

    private Integer isAnonymous;

    private Integer status;

    private Long handlerId;

    private Long processorId;

    private String processResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;

    private Integer isOvertime;

    private Integer satisfaction;

    private String satisfactionComment;

    private String attachment;
}
