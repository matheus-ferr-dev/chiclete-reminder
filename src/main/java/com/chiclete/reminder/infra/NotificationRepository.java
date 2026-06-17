package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            LEFT JOIN FETCH n.reminder
            LEFT JOIN FETCH n.groupInvite gi
            LEFT JOIN FETCH gi.group
            LEFT JOIN FETCH gi.invitedBy
            WHERE n.user.id = :userId AND n.read = false
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findUnreadWithDetails(@Param("userId") Long userId);

    @Query("""
            SELECT n FROM Notification n
            LEFT JOIN FETCH n.reminder
            LEFT JOIN FETCH n.groupInvite gi
            LEFT JOIN FETCH gi.group
            LEFT JOIN FETCH gi.invitedBy
            WHERE n.user.id = :userId
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findHistoryWithDetails(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    long countByUserId(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
