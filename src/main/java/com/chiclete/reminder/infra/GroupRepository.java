package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            SELECT g FROM Group g
            LEFT JOIN FETCH g.owner
            LEFT JOIN FETCH g.members
            WHERE g.id = :id
            """)
    Optional<Group> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT g FROM Group g
            JOIN g.members m
            WHERE m.id = :userId
            ORDER BY g.name ASC
            """)
    List<Group> findAllForMember(@Param("userId") Long userId);
}
