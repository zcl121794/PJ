package com.rural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rural.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
public class ServiceItem extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String itemName;

    private String category;

    private String conditionDesc;

    private String materialDesc;

    private String processDesc;

    private Integer timeLimit;

    private String location;

    private String contactPhone;

    private Integer needGridAudit;

    private Integer status;

    private Integer sort;
}
