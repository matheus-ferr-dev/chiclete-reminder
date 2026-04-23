package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.infra.ReminderRepository;
import com.chiclete.reminder.infra.UserRepository;
import com.chiclete.reminder.service.exception.ReminderNotFoundException;
import com.chiclete.reminder.ui.dto.CreateReminderRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public ReminderService(ReminderRepository reminderRepository, UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    public Reminder create(CreateReminderRequest request, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));

        Reminder reminder = new Reminder();
        reminder.setTitle(request.title());
        reminder.setDescription(request.description());
        reminder.setScheduledAt(request.scheduledAt());
        reminder.setPriority(request.priority());
        reminder.setChewing(Boolean.TRUE.equals(request.chewing()));
        reminder.setIntervalMinutes(request.intervalMinutes());
        reminder.setCompleted(false);
        reminder.setUser(user);
        return reminderRepository.save(reminder);
    }

    public List<Reminder> findAllForUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));
        return reminderRepository.findByUserId(user.getId());
    }

    public Reminder complete(Long reminderId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));

        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, user.getId())
            .orElseThrow(() -> new ReminderNotFoundException("Lembrete nao encontrado."));
        reminder.setCompleted(true);
        return reminderRepository.save(reminder);
    }
}
