package com.chiclete.reminder;

import com.chiclete.reminder.domain.RecurrenceSupport;
import com.chiclete.reminder.domain.Reminder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurrenceSupportTest {

    @Test
    void dias_uteis_avancam_para_proxima_segunda() {
        Reminder reminder = recurring(RecurrenceSupport.WEEKDAYS, RecurrenceSupport.WEEKDAY_PATTERN);
        reminder.setScheduledAt(LocalDateTime.of(2026, 6, 6, 9, 0));

        LocalDateTime next = RecurrenceSupport.nextOccurrenceStrictlyAfter(
                LocalDateTime.of(2026, 6, 6, 10, 0),
                reminder
        );

        assertEquals(LocalDateTime.of(2026, 6, 8, 9, 0), next);
    }

    @Test
    void dias_personalizados_respeitam_selecao() {
        Reminder reminder = recurring(RecurrenceSupport.CUSTOM, "2,4");
        reminder.setScheduledAt(LocalDateTime.of(2026, 6, 10, 18, 30));

        LocalDateTime next = RecurrenceSupport.nextOccurrenceStrictlyAfter(
                LocalDateTime.of(2026, 6, 10, 19, 0),
                reminder
        );

        assertEquals(LocalDateTime.of(2026, 6, 11, 18, 30), next);
    }

    @Test
    void agenda_inicial_usa_primeira_ocorrencia_valida() {
        LocalDateTime aligned = RecurrenceSupport.alignInitialSchedule(
                LocalDateTime.of(2026, 6, 7, 8, 0),
                RecurrenceSupport.WEEKDAYS,
                RecurrenceSupport.WEEKDAY_PATTERN
        );

        assertTrue(RecurrenceSupport.resolveDays(RecurrenceSupport.WEEKDAYS, RecurrenceSupport.WEEKDAY_PATTERN)
                .contains(aligned.getDayOfWeek().getValue()));
        assertEquals(8, aligned.getHour());
    }

    private static Reminder recurring(String type, String days) {
        Reminder reminder = new Reminder();
        reminder.setRecurrenceType(type);
        reminder.setRecurrenceDays(days);
        reminder.setRecurringEnabled(true);
        return reminder;
    }
}
