package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppealDissatisfiedRequest {

    /** 不满意理由（必填） */
    @NotBlank(message = "请填写不满意理由")
    private String reason;
}
