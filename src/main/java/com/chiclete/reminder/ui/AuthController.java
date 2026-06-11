package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.ForgotPasswordRequest;
import com.chiclete.reminder.dto.LoginRequest;
import com.chiclete.reminder.dto.MessageResponse;
import com.chiclete.reminder.dto.RegisterRequest;
import com.chiclete.reminder.dto.TokenResponse;
import com.chiclete.reminder.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resetPassword(request.email(), request.newPassword());
        return new MessageResponse("Senha redefinida. Já podes entrar.");
    }
}
