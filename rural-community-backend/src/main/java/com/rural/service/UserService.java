package com.rural.service;

import com.rural.dto.UserCreateRequest;
import com.rural.entity.SysUser;

public interface UserService {

    SysUser create(UserCreateRequest request);
}
