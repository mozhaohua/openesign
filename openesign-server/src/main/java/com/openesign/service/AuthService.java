package com.openesign.service;

import com.openesign.dto.request.LoginRequest;
import com.openesign.dto.request.RegisterRequest;
import com.openesign.dto.response.AuthResponse;
import com.openesign.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(Long userId);
}
