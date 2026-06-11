package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.RecurrenceSupport;
import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.infra.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ChicleteScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChicleteScheduler.class);

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;

    public ChicleteScheduler(ReminderRepository reminderRepository, NotificationService notificationService) {
        this.reminderRepository = reminderRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${app.notifications.poll-interval-ms:60000}")
    @Transactional
    public void dispatchDueAlerts() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> candidates = reminderRepository.findByCompletedFalseAndScheduledAtLessThanEqual(now);

        int sent = 0;
        for (Reminder reminder : candidates) {
            RecurrenceSupport.advanceStaleSchedule(reminder, now);
            if (isDueForNotification(reminder, now)) {
                notificationService.createAlertsForReminder(reminder, now);
                if (RecurrenceSupport.isRecurring(reminder) && !reminder.isChewing()) {
                    reminder.setScheduledAt(RecurrenceSupport.nextOccurrenceStrictlyAfter(now, reminder));
                    reminder.setLastNotifiedAt(null);
                }
                sent++;
            }
        }
        if (sent > 0) {
            log.info("Modo Chiclete: {} lembrete(s) com alerta disparado", sent);
        }
    }

    public static boolean isDueForNotification(Reminder reminder, LocalDateTime now) {
        if (reminder.isCompleted() || reminder.getScheduledAt().isAfter(now)) {
            return false;
        }
        if (RecurrenceSupport.isRecurringType(reminder.getRecurrenceType()) && !reminder.isRecurringEnabled()) {
            return false;
        }
        if (reminder.getSnoozedUntil() != null && reminder.getSnoozedUntil().isAfter(now)) {
            return false;
        }
        LocalDateTime last = reminder.getLastNotifiedAt();
        if (last == null) {
            return true;
        }
        if (!reminder.isChewing()) {
            return false;
        }
        Integer interval = reminder.getIntervalMinutes();
        if (interval == null || interval <= 0) {
            interval = 30;
        }
        return !last.plusMinutes(interval).isAfter(now);
    }
}
