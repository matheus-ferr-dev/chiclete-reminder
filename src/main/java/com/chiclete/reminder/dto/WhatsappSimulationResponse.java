package com.chiclete.reminder.dto;

import java.time.LocalDateTime;

public record WhatsappSimulationResponse(
        Long id,
        Long reminderId,
        String reminderTitle,
        String phone,
        String message,
        LocalDateTime createdAt
) {}
