package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.GroupInvite;
import com.chiclete.reminder.domain.Notification;
import com.chiclete.reminder.domain.NotificationType;
import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.NotificationResponse;
import com.chiclete.reminder.infra.NotificationRepository;
import com.chiclete.reminder.infra.ReminderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private static final int DEFAULT_SNOOZE_MINUTES = 10;
    private static final int HISTORY_LIMIT = 50;

    private final NotificationRepository notificationRepository;
    private final ReminderRepository reminderRepository;
    private final ReminderService reminderService;
    private final EmailNotificationService emailNotificationService;
    private final WhatsappSimulationService whatsappSimulationService;

    public NotificationService(
            NotificationRepository notificationRepository,
            ReminderRepository reminderRepository,
            ReminderService reminderService,
            EmailNotificationService emailNotificationService,
            WhatsappSimulationService whatsappSimulationService
    ) {
        this.notificationRepository = notificationRepository;
        this.reminderRepository = reminderRepository;
        this.reminderService = reminderService;
        this.emailNotificationService = emailNotificationService;
        this.whatsappSimulationService = whatsappSimulationService;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listUnread(User user) {
        return notificationRepository.findUnreadWithDetails(user.getId()).stream()
                .map(n -> toResponse(n, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listHistory(User user) {
        return notificationRepository
                .findHistoryWithDetails(user.getId(), PageRequest.of(0, HISTORY_LIMIT))
                .stream()
                .map(n -> toResponse(n, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id, User user) {
        Notification n = getOwnedNotification(id, user);
        n.setRead(true);
        notificationRepository.save(n);
        return toResponse(n, user);
    }

    @Transactional
    public NotificationResponse dismiss(Long id, User user) {
        Notification n = getOwnedNotification(id, user);
        n.setRead(true);
        notificationRepository.save(n);
        if (n.getType() == NotificationType.REMINDER
                && n.getReminder() != null
                && n.getReminder().isChewing()
                && !n.getReminder().isCompleted()) {
            reminderService.recordChewingIgnored(n.getReminder().getId(), user);
        }
        return toResponse(n, user);
    }

    @Transactional
    public NotificationResponse snooze(Long id, User user) {
        Notification n = getOwnedNotification(id, user);
        if (n.getType() != NotificationType.REMINDER || n.getReminder() == null) {
            throw new DomainRuleException("Só alertas de lembrete podem ser adiados");
        }
        n.setRead(true);
        notificationRepository.save(n);
        reminderService.snooze(n.getReminder().getId(), DEFAULT_SNOOZE_MINUTES, user);
        return toResponse(n, user);
    }

    @Transactional
    public void createGroupInviteNotification(GroupInvite invite) {
        User recipient = invite.getInvitedUser();
        if (!recipient.isNotifyInApp()) {
            return;
        }
        String message = invite.getInvitedBy().getName() + " convidou-te para o grupo \"" + invite.getGroup().getName() + "\"";
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(NotificationType.GROUP_INVITE);
        notification.setGroupInvite(invite);
        notification.setMessage(message);
        notification.setPriorityAtSend("INFO");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markGroupInviteNotificationsRead(Long inviteId, User user) {
        notificationRepository.findUnreadWithDetails(user.getId()).stream()
                .filter(n -> n.getType() == NotificationType.GROUP_INVITE)
                .filter(n -> n.getGroupInvite() != null && inviteId.equals(n.getGroupInvite().getId()))
                .forEach(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    @Transactional
    public void createAlertsForReminder(Reminder reminder, LocalDateTime now) {
        String message = buildMessage(reminder);
        Set<User> recipients = collectRecipients(reminder);

        for (User recipient : recipients) {
            if (recipient.isNotifyInApp()) {
                Notification notification = new Notification();
                notification.setUser(recipient);
                notification.setType(NotificationType.REMINDER);
                notification.setReminder(reminder);
                notification.setMessage(message);
                notification.setPriorityAtSend(reminder.getPriority());
                notification.setRead(false);
                notification.setCreatedAt(now);
                notificationRepository.save(notification);
            }
            emailNotificationService.sendReminderAlert(recipient, reminder, message);
            whatsappSimulationService.simulateSend(recipient, reminder, message, now);
        }

        reminder.setLastNotifiedAt(now);
        reminderRepository.save(reminder);
    }

    private Notification getOwnedNotification(Long id, User user) {
        return notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Notificação não encontrada"));
    }

    private Set<User> collectRecipients(Reminder reminder) {
        Set<User> recipients = new LinkedHashSet<>();
        recipients.add(reminder.getOwner());
        recipients.addAll(reminder.getSharedWith());
        return recipients;
    }

    private String buildMessage(Reminder reminder) {
        String chewingTag = reminder.isChewing() ? " 🫧 Chiclete" : "";
        String ignoreTag = reminder.getIgnoreCount() > 0 ? " (ignorado " + reminder.getIgnoreCount() + "×)" : "";
        return reminder.getTitle() + chewingTag + ignoreTag;
    }

    private NotificationResponse toResponse(Notification n, User viewer) {
        if (n.getType() == NotificationType.GROUP_INVITE) {
            GroupInvite invite = n.getGroupInvite();
            if (invite == null) {
                return new NotificationResponse(
                        n.getId(), NotificationType.GROUP_INVITE.name(),
                        null, null, null, null, null, null,
                        n.getMessage(), n.getPriorityAtSend(), false, n.isRead(), n.getCreatedAt(), null
                );
            }
            return new NotificationResponse(
                    n.getId(),
                    NotificationType.GROUP_INVITE.name(),
                    null,
                    null,
                    invite.getId(),
                    invite.getGroup().getId(),
                    invite.getGroup().getName(),
                    invite.getInvitedBy().getEmail(),
                    n.getMessage(),
                    n.getPriorityAtSend(),
                    false,
                    n.isRead(),
                    n.getCreatedAt(),
                    null
            );
        }

        Reminder reminder = n.getReminder();
        String link = null;
        if (viewer.isNotifyWhatsapp() && viewer.getWhatsapp() != null && reminder != null) {
            link = ProfileService.buildWhatsappLink(viewer.getWhatsapp(), n.getMessage());
        }
        return new NotificationResponse(
                n.getId(),
                NotificationType.REMINDER.name(),
                reminder != null ? reminder.getId() : null,
                reminder != null ? reminder.getTitle() : null,
                null,
                null,
                null,
                null,
                n.getMessage(),
                n.getPriorityAtSend(),
                reminder != null && reminder.isChewing(),
                n.isRead(),
                n.getCreatedAt(),
                link
        );
    }
}
