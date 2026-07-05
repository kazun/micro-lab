package com.peap.analytics.controller;

import com.peap.analytics.dto.EntitySummaryResponse;
import com.peap.analytics.dto.PlatformSummaryResponse;
import com.peap.analytics.service.AnalyticsService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kazun
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<PlatformSummaryResponse> getPlatformSummary() {
        return ResponseEntity.ok(analyticsService.getPlatformSummary());
    }

    @GetMapping("/entities/{entityId}")
    public ResponseEntity<EntitySummaryResponse> getEntitySummary(@PathVariable UUID entityId) {
        return ResponseEntity.ok(analyticsService.getEntitySummary(entityId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<EntitySummaryResponse>> getLeaderboard(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getLeaderboard(category, limit));
    }
}
