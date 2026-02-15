package com.openesign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openesign.dto.request.LoginRequest;
import com.openesign.dto.request.RegisterRequest;
import com.openesign.dto.response.AuthResponse;
import com.openesign.dto.response.UserResponse;
import com.openesign.entity.User;
import com.openesign.mapper.UserMapper;
import com.openesign.service.AuthService;
import com.openesign.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if user exists
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setCompany(request.getCompany());
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // Generate token
        String token = jwtTokenUtil.generateToken(user.getId());
        
        return new AuthResponse(token, toUserResponse(user));
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        String token = jwtTokenUtil.generateToken(user.getId());
        
        return new AuthResponse(token, toUserResponse(user));
    }
    
    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return toUserResponse(user);
    }
    
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setCompany(user.getCompany());
        response.setEmailVerified(user.getEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
