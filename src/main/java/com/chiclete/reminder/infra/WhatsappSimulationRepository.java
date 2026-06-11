package com.chiclete.reminder.infra;

import com.chiclete.reminder.domain.WhatsappSimulation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhatsappSimulationRepository extends JpaRepository<WhatsappSimulation, Long> {

    List<WhatsappSimulation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
