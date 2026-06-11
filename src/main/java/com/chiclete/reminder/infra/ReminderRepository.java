package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByCompletedFalseAndScheduledAtLessThanEqual(LocalDateTime now);

    @Query("""
            SELECT DISTINCT r FROM Reminder r
            LEFT JOIN r.sharedWith s
            WHERE r.owner.id = :userId OR s.id = :userId
            ORDER BY r.scheduledAt ASC
            """)
    List<Reminder> findAllVisibleToUser(@Param("userId") Long userId);
}
