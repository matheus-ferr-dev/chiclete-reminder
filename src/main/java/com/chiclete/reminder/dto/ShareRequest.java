package com.chiclete.reminder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ShareRequest(@NotBlank @Email String email) {}
