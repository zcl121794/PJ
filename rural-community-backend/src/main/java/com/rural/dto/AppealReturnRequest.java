package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppealReturnRequest {

    /** 退回理由（必填） */
    @NotBlank(message = "请填写退回理由")
    private String reason;
}
