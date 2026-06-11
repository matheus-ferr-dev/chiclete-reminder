package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.domain.WhatsappSimulation;
import com.chiclete.reminder.dto.WhatsappSimulationResponse;
import com.chiclete.reminder.infra.WhatsappSimulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WhatsappSimulationService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappSimulationService.class);
    private static final int HISTORY_LIMIT = 50;

    private final WhatsappSimulationRepository repository;
    private final boolean enabled;

    public WhatsappSimulationService(
            WhatsappSimulationRepository repository,
            @Value("${app.whatsapp.simulation.enabled:true}") boolean enabled
    ) {
        this.repository = repository;
        this.enabled = enabled;
    }

    @Transactional
    public void simulateSend(User user, Reminder reminder, String message, LocalDateTime now) {
        if (!enabled || !user.isNotifyWhatsapp()) {
            return;
        }
        String phone = ProfileService.normalizeWhatsapp(user.getWhatsapp());
        if (phone == null) {
            return;
        }
        WhatsappSimulation sim = new WhatsappSimulation();
        sim.setUser(user);
        sim.setReminder(reminder);
        sim.setPhone(phone);
        sim.setMessage(message);
        sim.setCreatedAt(now);
        repository.save(sim);
        log.info("[WhatsApp SIM] → {} | {}", phone, message);
    }

    @Transactional(readOnly = true)
    public List<WhatsappSimulationResponse> listRecent(User user) {
        return repository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, HISTORY_LIMIT))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count(User user) {
        return repository.countByUserId(user.getId());
    }

    private WhatsappSimulationResponse toResponse(WhatsappSimulation s) {
        return new WhatsappSimulationResponse(
                s.getId(),
                s.getReminder().getId(),
                s.getReminder().getTitle(),
                s.getPhone(),
                s.getMessage(),
                s.getCreatedAt()
        );
    }
}
