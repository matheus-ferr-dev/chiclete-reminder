package com.chiclete.reminder.dto;

public record ChicleteStatsResponse(
        long chewingActive,
        long chewingCompleted,
        long totalIgnores,
        long totalAlerts
) {}
