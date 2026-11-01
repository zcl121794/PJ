package com.rural.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class VillagerImportDTO {

    @ExcelProperty("户号")
    private String householdNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("出生日期")
    private String birthDate;

    @ExcelProperty("民族")
    private String nation;

    @ExcelProperty("学历")
    private String education;

    @ExcelProperty("政治面貌")
    private String politicalStatus;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("健康状况")
    private String healthStatus;

    @ExcelProperty("就业状态")
    private String employmentStatus;

    @ExcelProperty("特殊群体")
    private String specialTag;

    @ExcelProperty("是否户主")
    private String isHouseholder;

    @ExcelProperty("与户主关系")
    private String relation;

    @ExcelProperty("备注")
    private String remark;
}
