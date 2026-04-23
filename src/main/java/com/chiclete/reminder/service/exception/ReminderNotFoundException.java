package com.chiclete.reminder.service.exception;

public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException(String message) {
        super(message);
    }
}
