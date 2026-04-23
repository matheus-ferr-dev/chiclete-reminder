package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Reminder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserId(Long userId);
    Optional<Reminder> findByIdAndUserId(Long id, Long userId);
}
