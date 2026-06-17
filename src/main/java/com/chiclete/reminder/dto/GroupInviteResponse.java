package com.chiclete.reminder.dto;

import com.chiclete.reminder.domain.GroupInviteStatus;

import java.time.LocalDateTime;

public record GroupInviteResponse(
        Long id,
        Long groupId,
        String groupName,
        String invitedEmail,
        String invitedByEmail,
        String invitedByName,
        GroupInviteStatus status,
        String inviteToken,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}
