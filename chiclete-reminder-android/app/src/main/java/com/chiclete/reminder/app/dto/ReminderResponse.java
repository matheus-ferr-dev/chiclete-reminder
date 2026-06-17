package com.chiclete.reminder.app.dto;

public class ReminderResponse {
    public long id;
    public String title;
    public String description;
    public String scheduledAt;
    public String priority;
    public boolean chewing;
    public Integer intervalMinutes;
    public int ignoreCount;
    public boolean completed;
}
