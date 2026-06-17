package com.chiclete.reminder.dto;

import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        String ownerEmail,
        List<String> memberEmails,
        List<GroupMemberResponse> members
) {}
