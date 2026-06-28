package com.irs1099.service;

import com.irs1099.dto.request.LoginRequest;
import com.irs1099.dto.request.RegisterRequest;
import com.irs1099.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    AuthResponse refreshToken(String refreshToken);
}
