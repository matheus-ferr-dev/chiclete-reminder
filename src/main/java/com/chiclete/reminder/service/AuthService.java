package com.chiclete.reminder.service;

import com.chiclete.reminder.ui.dto.LoginRequest;
import com.chiclete.reminder.ui.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtService.generateToken(request.email());
        return new LoginResponse(token, "Bearer");
    }
}
