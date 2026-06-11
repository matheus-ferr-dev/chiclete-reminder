package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.ChicleteStatsResponse;
import com.chiclete.reminder.infra.NotificationRepository;
import com.chiclete.reminder.infra.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChicleteStatsService {

    private final ReminderRepository reminderRepository;
    private final NotificationRepository notificationRepository;

    public ChicleteStatsService(ReminderRepository reminderRepository, NotificationRepository notificationRepository) {
        this.reminderRepository = reminderRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public ChicleteStatsResponse getStats(User user) {
        List<Reminder> visible = reminderRepository.findAllVisibleToUser(user.getId());
        long active = visible.stream().filter(r -> r.isChewing() && !r.isCompleted()).count();
        long completed = visible.stream().filter(r -> r.isChewing() && r.isCompleted()).count();
        long totalIgnores = visible.stream().mapToLong(Reminder::getIgnoreCount).sum();
        long totalAlerts = notificationRepository.countByUserId(user.getId());
        return new ChicleteStatsResponse(active, completed, totalIgnores, totalAlerts);
    }
}
