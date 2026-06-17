package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.GroupInvite;
import com.chiclete.reminder.domain.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    @Query("""
            SELECT i FROM GroupInvite i
            JOIN FETCH i.group g
            JOIN FETCH i.invitedBy
            LEFT JOIN FETCH i.invitedUser
            WHERE i.invitedUser.id = :invitedUserId AND i.status = :status
            ORDER BY i.createdAt DESC
            """)
    List<GroupInvite> findPendingForUserWithDetails(
            @Param("invitedUserId") Long invitedUserId,
            @Param("status") GroupInviteStatus status
    );

    @Query("""
            SELECT i FROM GroupInvite i
            JOIN FETCH i.group g
            JOIN FETCH i.invitedBy
            LEFT JOIN FETCH i.invitedUser
            WHERE g.id = :groupId AND i.status = :status
            ORDER BY i.createdAt DESC
            """)
    List<GroupInvite> findByGroupIdAndStatusWithDetails(
            @Param("groupId") Long groupId,
            @Param("status") GroupInviteStatus status
    );

    @Query("""
            SELECT i FROM GroupInvite i
            JOIN FETCH i.group g
            JOIN FETCH i.invitedBy
            LEFT JOIN FETCH i.invitedUser
            WHERE i.id = :id AND i.invitedUser.id = :invitedUserId
            """)
    Optional<GroupInvite> findByIdAndInvitedUserIdWithDetails(
            @Param("id") Long id,
            @Param("invitedUserId") Long invitedUserId
    );

    @Query("""
            SELECT i FROM GroupInvite i
            JOIN FETCH i.group g
            JOIN FETCH i.invitedBy
            LEFT JOIN FETCH i.invitedUser
            WHERE i.inviteToken = :token
            """)
    Optional<GroupInvite> findByInviteTokenWithDetails(@Param("token") String token);

    Optional<GroupInvite> findByGroupIdAndInvitedUserId(Long groupId, Long invitedUserId);

    Optional<GroupInvite> findByGroupIdAndInvitedEmailIgnoreCaseAndStatus(
            Long groupId, String invitedEmail, GroupInviteStatus status
    );

    Optional<GroupInvite> findByIdAndGroupId(Long id, Long groupId);
}
