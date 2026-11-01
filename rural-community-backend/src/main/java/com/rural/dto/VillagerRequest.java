package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class VillagerRequest {

    @NotNull(message = "请选择所属户")
    private Long householdId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    private Integer gender;

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

    private String remark;
}
