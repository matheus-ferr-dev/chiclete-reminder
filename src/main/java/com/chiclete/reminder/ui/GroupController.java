package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.GroupInviteResponse;
import com.chiclete.reminder.dto.GroupRequest;
import com.chiclete.reminder.dto.GroupResponse;
import com.chiclete.reminder.dto.InviteTokenPreviewResponse;
import com.chiclete.reminder.dto.ShareRequest;
import com.chiclete.reminder.dto.TransferOwnerRequest;
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

    @GetMapping("/invites")
    public List<GroupInviteResponse> listInvites() {
        return groupService.listPendingInvites(currentUserService.requireCurrentUser());
    }

    @PostMapping("/invites/{inviteId}/accept")
    public GroupResponse acceptInvite(@PathVariable Long inviteId) {
        return groupService.acceptInvite(inviteId, currentUserService.requireCurrentUser());
    }

    @PostMapping("/invites/{inviteId}/reject")
    public GroupInviteResponse rejectInvite(@PathVariable Long inviteId) {
        return groupService.rejectInvite(inviteId, currentUserService.requireCurrentUser());
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

    @GetMapping("/{id}/invites/sent")
    public List<GroupInviteResponse> listSentInvites(@PathVariable Long id) {
        return groupService.listSentInvites(id, currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}/invites/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelInvite(@PathVariable Long id, @PathVariable Long inviteId) {
        groupService.cancelInvite(id, inviteId, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/invites/{inviteId}/resend")
    public GroupInviteResponse resendInvite(@PathVariable Long id, @PathVariable Long inviteId) {
        return groupService.resendInvite(id, inviteId, currentUserService.requireCurrentUser());
    }

    @PatchMapping("/{id}")
    public GroupResponse rename(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return groupService.rename(id, request, currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        groupService.deleteGroup(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/leave")
    public GroupResponse leave(@PathVariable Long id) {
        return groupService.leaveGroup(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/transfer-owner")
    public GroupResponse transferOwner(@PathVariable Long id, @Valid @RequestBody TransferOwnerRequest request) {
        return groupService.transferOwner(id, request, currentUserService.requireCurrentUser());
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupInviteResponse inviteMember(@PathVariable Long id, @Valid @RequestBody ShareRequest request) {
        return groupService.inviteMember(id, request.email(), currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}/members/{email}")
    public GroupResponse removeMember(@PathVariable Long id, @PathVariable String email) {
        return groupService.removeMember(id, email, currentUserService.requireCurrentUser());
    }
}
