package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.NotificationResponse;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationService notificationService, CurrentUserService currentUserService) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<NotificationResponse> listUnread() {
        return notificationService.listUnread(currentUserService.requireCurrentUser());
    }

    @GetMapping("/count")
    public Map<String, Long> countUnread() {
        long count = notificationService.countUnread(currentUserService.requireCurrentUser());
        return Map.of("unread", count);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/dismiss")
    public NotificationResponse dismiss(@PathVariable Long id) {
        return notificationService.dismiss(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/snooze")
    public NotificationResponse snooze(@PathVariable Long id) {
        return notificationService.snooze(id, currentUserService.requireCurrentUser());
    }

    @GetMapping("/history")
    public List<NotificationResponse> history() {
        return notificationService.listHistory(currentUserService.requireCurrentUser());
    }
}
