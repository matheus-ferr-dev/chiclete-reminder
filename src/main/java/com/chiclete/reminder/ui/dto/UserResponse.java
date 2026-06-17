package com.chiclete.reminder.ui.dto;

public record UserResponse(
    Long id,
    String name,
    String email,
    String role
) {}
