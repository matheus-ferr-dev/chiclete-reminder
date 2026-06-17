package com.chiclete.reminder.ui.dto;

public record LoginResponse(
    String token,
    String type
) {}
