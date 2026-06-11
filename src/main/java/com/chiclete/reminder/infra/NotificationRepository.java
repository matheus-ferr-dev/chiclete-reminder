package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    long countByUserId(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
