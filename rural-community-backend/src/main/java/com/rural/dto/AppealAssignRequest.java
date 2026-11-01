package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppealAssignRequest {

    @NotNull(message = "请指定处理人")
    private Long processorId;
}
