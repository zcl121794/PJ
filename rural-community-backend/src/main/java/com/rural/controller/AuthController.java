package com.rural.controller;

import com.rural.common.Result;
import com.rural.dto.LoginRequest;
import com.rural.dto.LoginResponse;
import com.rural.security.LoginUser;
import com.rural.security.SecurityUtils;
import com.rural.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/info")
    public Result<LoginUser> info() {
        return Result.success(SecurityUtils.getLoginUser());
    }
}
