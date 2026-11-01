package com.rural.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AppealRequest {

    @NotBlank(message = "诉求分类不能为空")
    private String category;

    @NotBlank(message = "诉求标题不能为空")
    private String title;

    @NotBlank(message = "诉求内容不能为空")
    private String content;

    private String location;

    private Integer isAnonymous;
}
