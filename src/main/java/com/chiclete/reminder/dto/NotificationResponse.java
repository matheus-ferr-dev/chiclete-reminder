package com.chiclete.reminder.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long reminderId,
        String reminderTitle,
        String message,
        String priorityAtSend,
        boolean chewing,
        boolean read,
        LocalDateTime createdAt,
        String whatsappLink
) {}
