package com.rural.service;

import com.rural.dto.LoginRequest;
import com.rural.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
