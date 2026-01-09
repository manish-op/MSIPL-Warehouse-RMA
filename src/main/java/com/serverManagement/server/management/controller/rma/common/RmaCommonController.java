package com.serverManagement.server.management.controller.rma.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dto.rma.common.ProductModelDTO;
import com.serverManagement.server.management.service.rma.common.RmaCommonService;

@RestController
@RequestMapping("/api/rma")
public class RmaCommonController {

    @Autowired
    private RmaCommonService rmaCommonService;

    @GetMapping("/product-catalog")
    public ResponseEntity<?> getProductCatalog() {
        try {
            return rmaCommonService.getProductCatalog();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/product-rates")
    public ResponseEntity<?> getProductRates(@RequestBody List<ProductModelDTO> items) {
        try {
            return rmaCommonService.getProductRates(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch rates: " + e.getMessage());
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs() {
        try {
            return rmaCommonService.getAuditLogs();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/audit-logs/item/{itemId}")
    public ResponseEntity<?> getAuditLogsByItemId(@PathVariable("itemId") Long itemId) {
        try {
            return rmaCommonService.getAuditLogsByItemId(itemId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
