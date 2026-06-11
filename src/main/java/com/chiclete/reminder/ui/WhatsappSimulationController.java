package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.WhatsappSimulationResponse;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.WhatsappSimulationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappSimulationController {

    private final WhatsappSimulationService whatsappSimulationService;
    private final CurrentUserService currentUserService;

    public WhatsappSimulationController(
            WhatsappSimulationService whatsappSimulationService,
            CurrentUserService currentUserService
    ) {
        this.whatsappSimulationService = whatsappSimulationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/simulations")
    public List<WhatsappSimulationResponse> list() {
        return whatsappSimulationService.listRecent(currentUserService.requireCurrentUser());
    }

    @GetMapping("/simulations/count")
    public Map<String, Long> count() {
        long total = whatsappSimulationService.count(currentUserService.requireCurrentUser());
        return Map.of("total", total);
    }
}
