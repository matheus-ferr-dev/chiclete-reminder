package com.chiclete.reminder.ui;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.service.ReminderService;
import com.chiclete.reminder.ui.dto.CreateReminderRequest;
import com.chiclete.reminder.ui.dto.ReminderResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> create(
        @RequestBody @Valid CreateReminderRequest request,
        Principal principal
    ) {
        Reminder created = reminderService.create(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> findAll(Principal principal) {
        List<ReminderResponse> response = reminderService.findAllForUser(principal.getName())
            .stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReminderResponse> complete(@PathVariable Long id, Principal principal) {
        Reminder updated = reminderService.complete(id, principal.getName());
        return ResponseEntity.ok(toResponse(updated));
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return new ReminderResponse(
            reminder.getId(),
            reminder.getTitle(),
            reminder.getDescription(),
            reminder.getScheduledAt(),
            reminder.getPriority(),
            reminder.isChewing(),
            reminder.getIntervalMinutes(),
            reminder.getIgnoreCount(),
            reminder.isCompleted()
        );
    }
}
