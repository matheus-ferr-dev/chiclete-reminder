package com.chiclete.reminder.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        Long reminderId,
        String reminderTitle,
        Long groupInviteId,
        Long groupId,
        String groupName,
        String invitedByEmail,
        String message,
        String priorityAtSend,
        boolean chewing,
        boolean read,
        LocalDateTime createdAt,
        String whatsappLink
) {}
