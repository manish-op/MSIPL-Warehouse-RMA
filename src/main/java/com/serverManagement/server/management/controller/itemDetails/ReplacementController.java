package com.serverManagement.server.management.controller.itemDetails;

import com.serverManagement.server.management.request.itemDetails.ReplacementRequest;
import com.serverManagement.server.management.service.itemDetails.ReplacementService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> processReplacement(@RequestBody ReplacementRequest request,
            @RequestHeader(value = "Authorization", required = false) String token, HttpServletRequest servletRequest) {
        try {
            String userEmail = null;
            if (servletRequest.getUserPrincipal() != null) {
                userEmail = servletRequest.getUserPrincipal().getName();
            }

            String newSerial = replacementService.processReplacement(request, userEmail);
            // Return JSON response
            return ResponseEntity.ok().body("{\"message\": \"Success\", \"newSerial\": \"" + newSerial + "\"}");
        } catch (RuntimeException e) {
            e.printStackTrace();
            if ("OUT_OF_STOCK".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "OUT_OF_STOCK"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchReplacementItems(@RequestParam("query") String query) {
        try {
            return ResponseEntity.ok(replacementService.searchReplacementItems(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<?> listStatuses() {
        return ResponseEntity.ok(statusRepo.findAll());
    }
}
