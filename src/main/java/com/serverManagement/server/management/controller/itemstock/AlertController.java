package com.serverManagement.server.management.controller.itemstock;

import com.serverManagement.server.management.service.itemstock.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * This endpoint gets all active low-stock alerts for the
     * currently logged-in user (admin or manager).
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAlerts(Principal principal) {
        List<java.util.Map<String, String>> alerts = alertService.getActiveAlerts(principal);
        Map<String, Object> response = new HashMap<>();
        response.put("count", alerts.size());
        response.put("messages", alerts);
        return ResponseEntity.ok(response);
    }
}