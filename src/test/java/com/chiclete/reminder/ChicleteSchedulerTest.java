package com.chiclete.reminder;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.service.ChicleteScheduler;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChicleteSchedulerTest {

    @Test
    void primeira_notificacao_quando_horario_passou() {
        Reminder r = reminder(true, 30, null);
        LocalDateTime now = LocalDateTime.of(2026, 6, 9, 18, 0);
        r.setScheduledAt(now.minusMinutes(5));
        assertTrue(ChicleteScheduler.isDueForNotification(r, now));
    }

    @Test
    void chiclete_repete_apos_intervalo() {
        Reminder r = reminder(true, 15, LocalDateTime.of(2026, 6, 9, 17, 0));
        r.setScheduledAt(LocalDateTime.of(2026, 6, 9, 16, 0));
        assertTrue(ChicleteScheduler.isDueForNotification(r, LocalDateTime.of(2026, 6, 9, 17, 15)));
        assertFalse(ChicleteScheduler.isDueForNotification(r, LocalDateTime.of(2026, 6, 9, 17, 10)));
    }

    @Test
    void snooze_bloqueia_alerta() {
        Reminder r = reminder(true, 15, LocalDateTime.of(2026, 6, 9, 17, 0));
        r.setScheduledAt(LocalDateTime.of(2026, 6, 9, 16, 0));
        r.setSnoozedUntil(LocalDateTime.of(2026, 6, 9, 17, 30));
        assertFalse(ChicleteScheduler.isDueForNotification(r, LocalDateTime.of(2026, 6, 9, 17, 15)));
        assertTrue(ChicleteScheduler.isDueForNotification(r, LocalDateTime.of(2026, 6, 9, 17, 31)));
    }

    @Test
    void sem_chewing_nao_repete() {
        Reminder r = reminder(false, null, LocalDateTime.of(2026, 6, 9, 10, 0));
        r.setScheduledAt(LocalDateTime.of(2026, 6, 9, 9, 0));
        assertFalse(ChicleteScheduler.isDueForNotification(r, LocalDateTime.of(2026, 6, 9, 11, 0)));
    }

    private static Reminder reminder(boolean chewing, Integer interval, LocalDateTime lastNotified) {
        Reminder r = new Reminder();
        r.setChewing(chewing);
        r.setIntervalMinutes(interval);
        r.setLastNotifiedAt(lastNotified);
        r.setCompleted(false);
        return r;
    }
}
