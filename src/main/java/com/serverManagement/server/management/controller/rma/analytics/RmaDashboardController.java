package com.serverManagement.server.management.controller.rma.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.analytics.RmaDashboardService;

@RestController
@RequestMapping("/api/rma")
public class RmaDashboardController {

    @Autowired
    private RmaDashboardService rmaDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getRmaDashboardStats() {
        try {
            return rmaDashboardService.getRmaDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/tat-compliance-report")
    public ResponseEntity<?> getTatComplianceReport() {
        try {
            return rmaDashboardService.getTatComplianceReport();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
