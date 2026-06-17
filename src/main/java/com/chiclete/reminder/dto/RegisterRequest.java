package com.chiclete.reminder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 72) String password,
        String inviteToken
) {
    public RegisterRequest(String name, String email, String password) {
        this(name, email, password, null);
    }
}
