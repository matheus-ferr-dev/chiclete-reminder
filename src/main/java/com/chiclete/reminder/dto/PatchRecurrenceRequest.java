package com.chiclete.reminder.dto;

import jakarta.validation.constraints.NotNull;

public record PatchRecurrenceRequest(@NotNull Boolean enabled) {}
