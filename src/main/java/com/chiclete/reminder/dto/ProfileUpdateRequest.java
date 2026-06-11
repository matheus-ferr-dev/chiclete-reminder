package com.chiclete.reminder.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 120) String name,
        @Size(max = 20) String whatsapp,
        Boolean notifyEmail,
        Boolean notifyWhatsapp,
        Boolean notifyInApp
) {}
