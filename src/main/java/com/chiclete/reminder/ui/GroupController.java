package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.GroupRequest;
import com.chiclete.reminder.dto.GroupResponse;
import com.chiclete.reminder.dto.ShareRequest;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final CurrentUserService currentUserService;

    public GroupController(GroupService groupService, CurrentUserService currentUserService) {
        this.groupService = groupService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<GroupResponse> list() {
        return groupService.listFor(currentUserService.requireCurrentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/members")
    public GroupResponse addMember(@PathVariable Long id, @Valid @RequestBody ShareRequest request) {
        return groupService.addMember(id, request.email(), currentUserService.requireCurrentUser());
    }
}
