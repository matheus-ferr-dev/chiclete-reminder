package com.chiclete.reminder.ui.dto;

import com.chiclete.reminder.domain.ReminderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateReminderRequest(
    @NotBlank String title,
    String description,
    @NotNull LocalDateTime scheduledAt,
    @NotNull ReminderPriority priority,
    @NotNull Boolean chewing,
    Integer intervalMinutes
) {}
