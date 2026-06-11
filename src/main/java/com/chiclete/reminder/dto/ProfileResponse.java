package com.chiclete.reminder.dto;

public record ProfileResponse(
        Long id,
        String name,
        String email,
        String whatsapp,
        boolean notifyEmail,
        boolean notifyWhatsapp,
        boolean notifyInApp
) {}
