package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.GroupResponse;
import com.chiclete.reminder.dto.InviteTokenPreviewResponse;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.GroupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteTokenController {

    private final GroupService groupService;
    private final CurrentUserService currentUserService;

    public InviteTokenController(GroupService groupService, CurrentUserService currentUserService) {
        this.groupService = groupService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/token/{token}")
    public InviteTokenPreviewResponse preview(@PathVariable String token) {
        return groupService.previewInviteToken(token);
    }

    @PostMapping("/token/{token}/accept")
    public GroupResponse accept(@PathVariable String token) {
        return groupService.acceptInviteByToken(token, currentUserService.requireCurrentUser());
    }
}
