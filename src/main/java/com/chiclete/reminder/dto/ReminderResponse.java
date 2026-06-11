package com.chiclete.reminder.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime scheduledAt,
        boolean chewing,
        Integer intervalMinutes,
        int ignoreCount,
        boolean completed,
        String priority,
        String recurrenceType,
        String recurrenceDays,
        boolean recurringEnabled,
        Long ownerId,
        List<String> sharedWithEmails
) {}
