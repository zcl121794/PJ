package com.rural.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rural.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("villager")
public class Villager extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long householdId;

    private String name;

    private String idCard;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String nation;

    private String education;

    private String politicalStatus;

    private String phone;

    private String healthStatus;

    private String employmentStatus;

    private String specialTag;

    private Integer isHouseholder;

    private String relation;

    private Long userId;

    private String remark;
}
