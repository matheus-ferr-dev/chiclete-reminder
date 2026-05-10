package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            SELECT DISTINCT g FROM Group g
            JOIN g.members m
            WHERE m.id = :userId
            ORDER BY g.name ASC
            """)
    List<Group> findAllForMember(@Param("userId") Long userId);
}
