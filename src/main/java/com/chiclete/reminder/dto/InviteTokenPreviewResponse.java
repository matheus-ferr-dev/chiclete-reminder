package com.chiclete.reminder.dto;

import com.chiclete.reminder.domain.GroupInviteStatus;

public record InviteTokenPreviewResponse(
        String groupName,
        String invitedByName,
        GroupInviteStatus status,
        boolean requiresRegistration
) {}
