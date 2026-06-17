package com.chiclete.reminder.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_invites")
public class GroupInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;

    @Column(length = 255)
    private String invitedEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupInviteStatus status = GroupInviteStatus.PENDING;

    @Column(nullable = false, unique = true, length = 64)
    private String inviteToken;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public GroupInvite() {}

    public Long getId() { return id; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public User getInvitedUser() { return invitedUser; }
    public void setInvitedUser(User invitedUser) { this.invitedUser = invitedUser; }

    public String getInvitedEmail() { return invitedEmail; }
    public void setInvitedEmail(String invitedEmail) { this.invitedEmail = invitedEmail; }

    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }

    public GroupInviteStatus getStatus() { return status; }
    public void setStatus(GroupInviteStatus status) { this.status = status; }

    public String getInviteToken() { return inviteToken; }
    public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String resolveInvitedEmail() {
        if (invitedUser != null) {
            return invitedUser.getEmail();
        }
        return invitedEmail;
    }
}
