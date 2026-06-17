package com.chiclete.reminder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransferOwnerRequest(@NotBlank @Email String email) {}
