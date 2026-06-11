package com.chiclete.reminder.dto;

import java.util.List;

public record GroupResponse(Long id, String name, List<String> memberEmails) {}
