package com.serverManagement.server.management.controller.rma.depot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dto.rma.depot.DepotDispatchRequest;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.service.rma.depot.RmaDepotService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotOperationsController {

    @Autowired
    private RmaDepotService rmaDepotService;

    @Autowired
    private AdminUserDAO adminUserDAO;

    // 4) POST: mark as received at depot (Bangalore)
    @PostMapping("/depot/mark-received")
    public ResponseEntity<?> markAsReceived(HttpServletRequest request,
            @RequestBody DepotDispatchRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            rmaDepotService.markAsReceived(req.getItemIds(), loggedInUserEmail, loggedInUserName,
                    getClientIpAddress(request));
            return ResponseEntity.ok("Items marked as Received at Depot");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking received: " + e.getMessage());
        }
    }

    // 4.5) NEW: mark depot item as REPAIRED (Ready to send back to GGN)
    @PostMapping("/depot/mark-repaired")
    public ResponseEntity<?> markAsRepaired(HttpServletRequest request,
            @RequestBody DepotDispatchRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            rmaDepotService.markAsRepaired(req.getItemIds(), req.getRepairStatus(), loggedInUserEmail, loggedInUserName,
                    getClientIpAddress(request));
            return ResponseEntity.ok("Items marked as "
                    + (req.getRepairStatus() != null ? req.getRepairStatus() : "Repaired") + " at Depot");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking repaired: " + e.getMessage());
        }
    }

    // 6) NEW: mark repaired depot item received at Gurgaon
    @PostMapping("/depot/mark-received-gurgaon")
    public ResponseEntity<?> markReceivedAtGurgaon(HttpServletRequest request,
            @RequestBody DepotDispatchRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            rmaDepotService.markReceivedAtGurgaon(req.getItemIds(), loggedInUserEmail, loggedInUserName,
                    getClientIpAddress(request));
            return ResponseEntity.ok("Items marked as Received at Gurgaon");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking received at Gurgaon: " + e.getMessage());
        }
    }

    // 9) POST: Mark item as Returned Faulty and Create NEW RMA
    @PostMapping("/depot/faulty-new-rma")
    public ResponseEntity<?> markFaultyAndCreateResult(HttpServletRequest request,
            @RequestBody DepotDispatchRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        if (req.getItemIds() == null || req.getItemIds().isEmpty()) {
            return ResponseEntity.badRequest().body("No item ID provided");
        }
        Long itemId = req.getItemIds().get(0); // Take first one

        try {
            String newRequestNumber = rmaDepotService.markFaultyAndCreateNewRma(itemId, loggedInUserEmail,
                    loggedInUserName, getClientIpAddress(request));
            return ResponseEntity.ok("Old item closed as Faulty. New RMA Request " + newRequestNumber + " created.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating faulty return RMA: " + e.getMessage());
        }
    }

    // helper: IP
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
