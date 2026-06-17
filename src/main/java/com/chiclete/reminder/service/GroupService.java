package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.*;
import com.chiclete.reminder.dto.*;
import com.chiclete.reminder.infra.GroupInviteRepository;
import com.chiclete.reminder.infra.GroupRepository;
import com.chiclete.reminder.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private static final int INVITE_VALID_DAYS = 7;

    private final GroupRepository groupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GroupService(
            GroupRepository groupRepository,
            GroupInviteRepository groupInviteRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.groupRepository = groupRepository;
        this.groupInviteRepository = groupInviteRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public GroupResponse create(GroupRequest request, User creator) {
        Group group = new Group();
        group.setName(request.name());
        group.setOwner(creator);
        group.getMembers().add(creator);
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listFor(User user) {
        return groupRepository.findAllForMember(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupInviteResponse> listPendingInvites(User user) {
        return groupInviteRepository
                .findPendingForUserWithDetails(user.getId(), GroupInviteStatus.PENDING)
                .stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupInviteResponse> listSentInvites(Long groupId, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        return groupInviteRepository
                .findByGroupIdAndStatusWithDetails(groupId, GroupInviteStatus.PENDING)
                .stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InviteTokenPreviewResponse previewInviteToken(String token) {
        GroupInvite invite = getValidPendingInviteByToken(token);
        return new InviteTokenPreviewResponse(
                invite.getGroup().getName(),
                invite.getInvitedBy().getName(),
                invite.getStatus(),
                invite.getInvitedUser() == null
        );
    }

    @Transactional
    public GroupInviteResponse inviteMember(Long groupId, String memberEmail, User actor) {
        Group group = getGroupForMember(groupId, actor);
        String email = memberEmail.trim().toLowerCase();
        if (email.equals(actor.getEmail().toLowerCase())) {
            throw new DomainRuleException("Não podes convidar a ti mesmo");
        }
        if (group.getMembers().stream().anyMatch(m -> m.getEmail().equalsIgnoreCase(email))) {
            throw new DomainRuleException("Este utilizador já é membro do grupo");
        }

        User invited = userRepository.findByEmail(email).orElse(null);
        GroupInvite invite = findExistingInvite(groupId, invited, email).orElseGet(GroupInvite::new);
        if (invite.getId() != null && invite.getStatus() == GroupInviteStatus.PENDING) {
            throw new DomainRuleException("Já existe um convite pendente para este e-mail");
        }

        if (invite.getId() == null) {
            invite.setGroup(group);
        }
        invite.setInvitedUser(invited);
        invite.setInvitedEmail(invited == null ? email : null);
        invite.setInvitedBy(actor);
        invite.setStatus(GroupInviteStatus.PENDING);
        invite.setInviteToken(generateToken());
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(INVITE_VALID_DAYS));
        groupInviteRepository.save(invite);

        if (invited != null) {
            notificationService.createGroupInviteNotification(invite);
        }
        return toInviteResponse(invite);
    }

    @Transactional
    public GroupResponse acceptInvite(Long inviteId, User user) {
        GroupInvite invite = getPendingInviteForUser(inviteId, user);
        return finalizeAccept(invite, user);
    }

    @Transactional
    public GroupResponse acceptInviteByToken(String token, User user) {
        GroupInvite invite = getValidPendingInviteByToken(token);
        assertInviteTarget(invite, user);
        if (invite.getInvitedUser() == null) {
            invite.setInvitedUser(user);
            groupInviteRepository.save(invite);
        }
        return finalizeAccept(invite, user);
    }

    @Transactional
    public void acceptInviteAfterRegistration(String token, User user) {
        if (token == null || token.isBlank()) {
            return;
        }
        acceptInviteByToken(token.trim(), user);
    }

    @Transactional
    public GroupInviteResponse rejectInvite(Long inviteId, User user) {
        GroupInvite invite = getPendingInviteForUser(inviteId, user);
        invite.setStatus(GroupInviteStatus.REJECTED);
        groupInviteRepository.save(invite);
        notificationService.markGroupInviteNotificationsRead(invite.getId(), user);
        return toInviteResponse(invite);
    }

    @Transactional
    public void cancelInvite(Long groupId, Long inviteId, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        GroupInvite invite = groupInviteRepository.findByIdAndGroupId(inviteId, groupId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new DomainRuleException("Este convite já foi respondido");
        }
        invite.setStatus(GroupInviteStatus.REJECTED);
        groupInviteRepository.save(invite);
    }

    @Transactional
    public GroupInviteResponse resendInvite(Long groupId, Long inviteId, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        GroupInvite invite = groupInviteRepository.findByIdAndGroupId(inviteId, groupId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new DomainRuleException("Só convites pendentes podem ser reenviados");
        }
        invite.setInviteToken(generateToken());
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(INVITE_VALID_DAYS));
        groupInviteRepository.save(invite);
        if (invite.getInvitedUser() != null) {
            notificationService.createGroupInviteNotification(invite);
        }
        return toInviteResponse(invite);
    }

    @Transactional
    public GroupResponse rename(Long groupId, GroupRequest request, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        group.setName(request.name());
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(Long groupId, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        groupRepository.delete(group);
    }

    @Transactional
    public GroupResponse leaveGroup(Long groupId, User actor) {
        Group group = getGroupForMember(groupId, actor);
        if (group.getOwner() != null && group.getOwner().getId().equals(actor.getId()) && group.getMembers().size() > 1) {
            throw new DomainRuleException("Transfere a administração ou remove os membros antes de sair");
        }
        group.getMembers().removeIf(m -> m.getId().equals(actor.getId()));
        if (group.getMembers().isEmpty()) {
            groupRepository.delete(group);
            return null;
        }
        if (group.getOwner() != null && group.getOwner().getId().equals(actor.getId())) {
            group.setOwner(group.getMembers().getFirst());
        }
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(Long groupId, String memberEmail, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        User target = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado"));
        if (target.getId().equals(actor.getId())) {
            throw new DomainRuleException("Usa «Sair do grupo» para te removeres");
        }
        if (group.getMembers().stream().noneMatch(m -> m.getId().equals(target.getId()))) {
            throw new DomainRuleException("Este utilizador não é membro do grupo");
        }
        group.getMembers().removeIf(m -> m.getId().equals(target.getId()));
        groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse transferOwner(Long groupId, TransferOwnerRequest request, User actor) {
        Group group = getGroupForMember(groupId, actor);
        requireOwner(group, actor);
        User target = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado"));
        if (group.getMembers().stream().noneMatch(m -> m.getId().equals(target.getId()))) {
            throw new DomainRuleException("O novo admin precisa ser membro do grupo");
        }
        group.setOwner(target);
        groupRepository.save(group);
        return toResponse(group);
    }

    private GroupResponse finalizeAccept(GroupInvite invite, User user) {
        Group group = invite.getGroup();
        if (group.getMembers().stream().noneMatch(m -> m.getId().equals(user.getId()))) {
            group.getMembers().add(user);
            groupRepository.save(group);
        }
        invite.setStatus(GroupInviteStatus.ACCEPTED);
        groupInviteRepository.save(invite);
        notificationService.markGroupInviteNotificationsRead(invite.getId(), user);
        return toResponse(group);
    }

    private java.util.Optional<GroupInvite> findExistingInvite(Long groupId, User invited, String email) {
        if (invited != null) {
            return groupInviteRepository.findByGroupIdAndInvitedUserId(groupId, invited.getId());
        }
        return groupInviteRepository.findByGroupIdAndInvitedEmailIgnoreCaseAndStatus(
                groupId, email, GroupInviteStatus.PENDING
        ).or(() -> groupInviteRepository.findByGroupIdAndInvitedEmailIgnoreCaseAndStatus(
                groupId, email, GroupInviteStatus.REJECTED
        ));
    }

    private Group getGroupForMember(Long groupId, User actor) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (group.getMembers().stream().noneMatch(m -> m.getId().equals(actor.getId()))) {
            throw new ForbiddenException("Apenas membros do grupo podem realizar esta operação");
        }
        return group;
    }

    private GroupInvite getPendingInviteForUser(Long inviteId, User user) {
        GroupInvite invite = groupInviteRepository.findByIdAndInvitedUserIdWithDetails(inviteId, user.getId())
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new DomainRuleException("Este convite já foi respondido");
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainRuleException("Este convite expirou");
        }
        return invite;
    }

    private GroupInvite getValidPendingInviteByToken(String token) {
        GroupInvite invite = groupInviteRepository.findByInviteTokenWithDetails(token)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new DomainRuleException("Este convite já foi respondido");
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainRuleException("Este convite expirou");
        }
        return invite;
    }

    private void assertInviteTarget(GroupInvite invite, User user) {
        if (invite.getInvitedUser() != null && !invite.getInvitedUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Este convite pertence a outro utilizador");
        }
        if (invite.getInvitedEmail() != null
                && !invite.getInvitedEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ForbiddenException("Este convite foi enviado para outro e-mail");
        }
    }

    private void requireOwner(Group group, User actor) {
        if (group.getOwner() == null || !group.getOwner().getId().equals(actor.getId())) {
            throw new ForbiddenException("Apenas o admin do grupo pode realizar esta operação");
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private GroupResponse toResponse(Group g) {
        String ownerEmail = g.getOwner() != null ? g.getOwner().getEmail() : null;
        List<GroupMemberResponse> members = g.getMembers().stream()
                .sorted(Comparator.comparing(User::getEmail))
                .map(u -> new GroupMemberResponse(
                        u.getEmail(),
                        u.getName(),
                        ownerEmail != null && ownerEmail.equalsIgnoreCase(u.getEmail())
                ))
                .toList();
        List<String> emails = members.stream().map(GroupMemberResponse::email).toList();
        return new GroupResponse(g.getId(), g.getName(), ownerEmail, emails, members);
    }

    private GroupInviteResponse toInviteResponse(GroupInvite invite) {
        return new GroupInviteResponse(
                invite.getId(),
                invite.getGroup().getId(),
                invite.getGroup().getName(),
                invite.resolveInvitedEmail(),
                invite.getInvitedBy().getEmail(),
                invite.getInvitedBy().getName(),
                invite.getStatus(),
                invite.getInviteToken(),
                invite.getCreatedAt(),
                invite.getExpiresAt()
        );
    }
}
