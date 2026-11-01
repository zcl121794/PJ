package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppealHandleRequest {

    @NotBlank(message = "请填写处理结果")
    private String processResult;
}
