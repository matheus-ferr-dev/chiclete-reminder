package com.chiclete.reminder.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type = NotificationType.REMINDER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reminder_id")
    private Reminder reminder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_invite_id")
    private GroupInvite groupInvite;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String priorityAtSend;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Reminder getReminder() { return reminder; }
    public void setReminder(Reminder reminder) { this.reminder = reminder; }

    public GroupInvite getGroupInvite() { return groupInvite; }
    public void setGroupInvite(GroupInvite groupInvite) { this.groupInvite = groupInvite; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPriorityAtSend() { return priorityAtSend; }
    public void setPriorityAtSend(String priorityAtSend) { this.priorityAtSend = priorityAtSend; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
