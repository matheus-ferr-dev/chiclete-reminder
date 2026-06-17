package com.chiclete.reminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReminderRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 500) String description,
        @NotNull LocalDateTime scheduledAt,
        boolean chewing,
        Integer intervalMinutes,
        @NotBlank @Size(max = 50) String priority,
        String recurrenceType,
        String recurrenceDays,
        Boolean recurringEnabled,
        Long groupId,
        String assigneeEmail
) {}
