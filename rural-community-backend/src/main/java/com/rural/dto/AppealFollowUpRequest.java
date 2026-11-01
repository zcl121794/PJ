package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppealFollowUpRequest {

    /** 追问问题（必填） */
    @NotBlank(message = "请填写追问问题")
    private String question;
}
