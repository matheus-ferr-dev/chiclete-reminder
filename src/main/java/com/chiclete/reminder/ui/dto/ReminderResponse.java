package com.chiclete.reminder.ui.dto;

import com.chiclete.reminder.domain.ReminderPriority;
import java.time.LocalDateTime;

public record ReminderResponse(
    Long id,
    String title,
    String description,
    LocalDateTime scheduledAt,
    ReminderPriority priority,
    boolean chewing,
    Integer intervalMinutes,
    int ignoreCount,
    boolean completed
) {}
