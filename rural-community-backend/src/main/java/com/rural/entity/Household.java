package com.rural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rural.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("household")
public class Household extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String householdNo;

    private Long houseHolderId;

    private String houseType;

    private Integer registerType;

    private String address;

    private Long gridId;

    private String contactPhone;

    private Integer memberCount;

    private String remark;
}
