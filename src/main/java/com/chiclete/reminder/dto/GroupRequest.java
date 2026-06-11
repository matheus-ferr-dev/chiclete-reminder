package com.chiclete.reminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(@NotBlank @Size(max = 200) String name) {}
