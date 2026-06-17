package com.chiclete.reminder.domain;

import com.chiclete.reminder.service.DomainRuleException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecurrenceSupport {

    public static final String NONE = "NONE";
    public static final String WEEKDAYS = "WEEKDAYS";
    public static final String CUSTOM = "CUSTOM";
    public static final String WEEKDAY_PATTERN = "1,2,3,4,5";

    private RecurrenceSupport() {}

    public static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return NONE;
        }
        return type.trim().toUpperCase();
    }

    public static boolean isRecurring(Reminder reminder) {
        return isRecurringType(reminder.getRecurrenceType()) && reminder.isRecurringEnabled();
    }

    public static boolean isRecurringType(String type) {
        String normalized = normalizeType(type);
        return WEEKDAYS.equals(normalized) || CUSTOM.equals(normalized);
    }

    public static String normalizeDaysForType(String type, String recurrenceDays) {
        String normalized = normalizeType(type);
        if (NONE.equals(normalized)) {
            return null;
        }
        if (WEEKDAYS.equals(normalized)) {
            return WEEKDAY_PATTERN;
        }
        Set<Integer> days = parseDaySet(recurrenceDays);
        if (days.isEmpty()) {
            throw new DomainRuleException("Seleciona pelo menos um dia para a repetição personalizada");
        }
        return days.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static Set<Integer> resolveDays(Reminder reminder) {
        return resolveDays(reminder.getRecurrenceType(), reminder.getRecurrenceDays());
    }

    public static Set<Integer> resolveDays(String type, String recurrenceDays) {
        String normalized = normalizeType(type);
        if (WEEKDAYS.equals(normalized)) {
            return parseDaySet(WEEKDAY_PATTERN);
        }
        if (CUSTOM.equals(normalized)) {
            return parseDaySet(recurrenceDays);
        }
        return Set.of();
    }

    public static LocalDateTime alignInitialSchedule(LocalDateTime preferred, String type, String recurrenceDays) {
        String normalized = normalizeType(type);
        if (NONE.equals(normalized)) {
            return preferred;
        }
        Set<Integer> days = resolveDays(normalized, normalizeDaysForType(normalized, recurrenceDays));
        LocalTime time = preferred.toLocalTime();
        LocalDateTime candidate = LocalDateTime.of(preferred.toLocalDate(), time);
        if (days.contains(preferred.getDayOfWeek().getValue()) && !candidate.isBefore(LocalDateTime.now())) {
            return candidate;
        }
        return nextOccurrenceStrictlyAfter(LocalDateTime.now().minusSeconds(1), days, time);
    }

    public static LocalDateTime nextOccurrenceStrictlyAfter(LocalDateTime from, Reminder reminder) {
        if (!isRecurringType(reminder.getRecurrenceType())) {
            return reminder.getScheduledAt();
        }
        return nextOccurrenceStrictlyAfter(from, resolveDays(reminder), reminder.getScheduledAt().toLocalTime());
    }

    public static LocalDateTime nextOccurrenceStrictlyAfter(LocalDateTime from, Set<Integer> days, LocalTime time) {
        if (days.isEmpty()) {
            return from;
        }
        LocalDate start = from.toLocalDate();
        for (int offset = 0; offset < 370; offset++) {
            LocalDate date = start.plusDays(offset);
            if (!days.contains(date.getDayOfWeek().getValue())) {
                continue;
            }
            LocalDateTime candidate = LocalDateTime.of(date, time);
            if (candidate.isAfter(from)) {
                return candidate;
            }
        }
        throw new DomainRuleException("Não foi possível calcular a próxima ocorrência do lembrete");
    }

    public static void advanceStaleSchedule(Reminder reminder, LocalDateTime now) {
        if (!isRecurring(reminder) || reminder.isCompleted()) {
            return;
        }
        if (reminder.getScheduledAt() == null || !reminder.getScheduledAt().isBefore(now)) {
            return;
        }
        reminder.setScheduledAt(nextOccurrenceStrictlyAfter(now.minusSeconds(1), reminder));
        reminder.setLastNotifiedAt(null);
        reminder.setSnoozedUntil(null);
    }

    private static Set<Integer> parseDaySet(String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(recurrenceDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .filter(day -> day >= 1 && day <= 7)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
