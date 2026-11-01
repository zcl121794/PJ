package com.rural.service.impl;

import com.rural.dto.LoginRequest;
import com.rural.dto.LoginResponse;
import com.rural.security.JwtUtil;
import com.rural.security.LoginUser;
import com.rural.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String token = jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(loginUser.getUser());
        return response;
    }
}
