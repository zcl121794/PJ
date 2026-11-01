package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ServiceApplyRequest {

    @NotNull(message = "请选择办事事项")
    private Long itemId;

    private String applyContent;
}
