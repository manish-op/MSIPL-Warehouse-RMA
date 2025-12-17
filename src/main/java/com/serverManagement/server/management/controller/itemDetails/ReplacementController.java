package com.serverManagement.server.management.controller.itemDetails;

import com.serverManagement.server.management.request.itemDetails.ReplacementRequest;
import com.serverManagement.server.management.service.itemDetails.ReplacementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({ "/api/replacement", "/replacement" })
@CrossOrigin(origins = "*")
public class ReplacementController {

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO statusRepo;

    @PostMapping("/process")
    public ResponseEntity<?> processReplacement(java.security.Principal principal,
            @RequestBody ReplacementRequest request) {
        try {
            String userEmail = principal != null ? principal.getName() : "system_admin"; // Fallback if no auth
            String newSerial = replacementService.processReplacement(request, userEmail);
            // Return JSON object with success message and new serial
            return ResponseEntity.ok(Map.of("message", "Success", "newSerial", newSerial));
        } catch (RuntimeException e) {
            // Handle known "Expected" errors
            if ("OUT_OF_STOCK".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(Map.of("error", "OUT_OF_STOCK"));
            }
            // Handle unexpected errors safely (avoid null message crash)
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown Internal Error";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", errorMsg));
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<?> listStatuses() {
        // Assuming statusRepo.findAll() returns a collection of statuses
        return ResponseEntity.ok(statusRepo.findAll());
    }
}
