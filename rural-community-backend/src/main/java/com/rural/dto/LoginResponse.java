package com.rural.dto;

import com.rural.entity.SysUser;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private SysUser user;
}
