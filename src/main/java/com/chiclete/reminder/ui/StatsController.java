package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.ChicleteStatsResponse;
import com.chiclete.reminder.service.ChicleteStatsService;
import com.chiclete.reminder.service.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ChicleteStatsService chicleteStatsService;
    private final CurrentUserService currentUserService;

    public StatsController(ChicleteStatsService chicleteStatsService, CurrentUserService currentUserService) {
        this.chicleteStatsService = chicleteStatsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/chiclete")
    public ChicleteStatsResponse chiclete() {
        return chicleteStatsService.getStats(currentUserService.requireCurrentUser());
    }
}
