package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.*;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final CurrentUserService currentUserService;

    public ReminderController(ReminderService reminderService, CurrentUserService currentUserService) {
        this.reminderService = reminderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ReminderResponse> list() {
        return reminderService.listVisible(currentUserService.requireCurrentUser());
    }

    @GetMapping("/{id}")
    public ReminderResponse get(@PathVariable Long id) {
        return reminderService.get(id, currentUserService.requireCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderResponse create(@Valid @RequestBody ReminderRequest request) {
        return reminderService.create(request, currentUserService.requireCurrentUser());
    }

    @PutMapping("/{id}")
    public ReminderResponse update(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) {
        return reminderService.update(id, request, currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reminderService.delete(id, currentUserService.requireCurrentUser());
    }

    @PatchMapping("/{id}/complete")
    public ReminderResponse patchComplete(@PathVariable Long id, @Valid @RequestBody PatchCompletedRequest body) {
        return reminderService.setCompleted(id, body.completed(), currentUserService.requireCurrentUser());
    }

    @PatchMapping("/{id}/chewing")
    public ReminderResponse patchChewing(@PathVariable Long id, @Valid @RequestBody PatchChewingRequest body) {
        return reminderService.setChewing(id, body.chewing(), currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/chewing/ignore")
    public ReminderResponse chewingIgnored(@PathVariable Long id) {
        return reminderService.recordChewingIgnored(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/share")
    public ReminderResponse share(@PathVariable Long id, @Valid @RequestBody ShareRequest request) {
        return reminderService.shareWithEmail(id, request.email(), currentUserService.requireCurrentUser());
    }
}
