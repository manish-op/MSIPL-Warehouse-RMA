package com.serverManagement.server.management.controller.rma;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.DepotDispatchItemDto;
import com.serverManagement.server.management.dto.rma.DepotDispatchRequest;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.DepotDispatchEntity;
import com.serverManagement.server.management.entity.rma.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotDispatchController {

    private final RmaItemDAO rmaItemDAO;
    private final RmaAuditLogDAO rmaAuditLogDAO;
    private final AdminUserDAO adminUserDAO;

    public RmaDepotDispatchController(RmaItemDAO rmaItemDAO, RmaAuditLogDAO rmaAuditLogDAO, AdminUserDAO adminUserDAO) {
        this.rmaItemDAO = rmaItemDAO;
        this.rmaAuditLogDAO = rmaAuditLogDAO;
        this.adminUserDAO = adminUserDAO;
    }

    // 1) GET: depot items waiting for first dispatch
    @GetMapping("/depot/ready-to-dispatch")
    public List<DepotDispatchItemDto> getDepotReadyToDispatch() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStage("DEPOT", "PENDING_DISPATCH_TO_DEPOT")
                .stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

    // 2) POST: mark as dispatched to Bangalore
    @PostMapping("/depot/dispatch-to-bangalore")
    public ResponseEntity<?> dispatchToBangalore(HttpServletRequest request, @RequestBody DepotDispatchRequest req) {
        // Get logged in user for audit
        String loggedInUserEmail = null;
        String loggedInUserName = null;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found for dispatch");
        }

        // Check if any items are already dispatched
        List<String> alreadyDispatchedSerials = items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsDispatched()))
                .map(RmaItemEntity::getSerialNo)
                .toList();

        if (!alreadyDispatchedSerials.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Items already dispatched (Serial No): " + alreadyDispatchedSerials
                            + ". Cannot dispatch again.");
        }

        // Create new Dispatch Entity
        DepotDispatchEntity dispatch = new DepotDispatchEntity();
        dispatch.setDcNo(req.getDcNo());
        dispatch.setEwayBillNo(req.getEwayBillNo());
        dispatch.setCourierName(req.getCourierName());
        dispatch.setTrackingNo(req.getTrackingNo());
        dispatch.setRemarks(req.getRemarks());

        if (req.getDispatchDate() != null) {
            dispatch.setDispatchDate(req.getDispatchDate().atStartOfDay(java.time.ZoneId.systemDefault()));
        } else {
            dispatch.setDispatchDate(java.time.ZonedDateTime.now());
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("IN_TRANSIT_TO_DEPOT");
            item.setDepotDispatch(dispatch);

            // Set dispatch tracking fields
            item.setIsDispatched(true);
            item.setDispatchedDate(java.time.ZonedDateTime.now());
            item.setDispatchedByEmail(loggedInUserEmail);
            item.setDispatchedByName(loggedInUserName);
            item.setRmaStatus("DISPATCHED");
            item.setDispatchTo("BANGALORE"); // Explicitly set dispatch_to

            // Create Audit Log for DC Generation / Dispatch
            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            // Use assigned RMA number if available, otherwise fallback to request number
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("DISPATCHED_TO_DEPOT");
            auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "PENDING"));
            auditLog.setNewValue("Stage: IN_TRANSIT_TO_DEPOT, DC No: " + req.getDcNo() +
                    ", Courier: " + req.getCourierName() + ", Tracking: " + req.getTrackingNo());
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("DC generated and item dispatched to Bangalore depot");

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Dispatched to Bangalore");
    }

    // 3) GET: depot items in transit or delivered (history)
    @GetMapping("/depot/in-transit")
    public List<DepotDispatchItemDto> getInTransitItems() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStageIn("DEPOT", List.of(
                        "IN_TRANSIT_TO_DEPOT",
                        "AT_DEPOT_RECEIVED",
                        "AT_DEPOT_UNREPAIRED",
                        "AT_DEPOT_REPAIRING",
                        "AT_DEPOT_REPAIRED"))
                .stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

    // 4) POST: mark as received at depot
    @PostMapping("/depot/mark-received")
    public ResponseEntity<?> markAsReceived(HttpServletRequest request, @RequestBody DepotDispatchRequest req) {
        // Get logged in user for audit
        String loggedInUserEmail = null;
        String loggedInUserName = null;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found");
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("AT_DEPOT_RECEIVED");

            // Create Audit Log for status change
            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            // Use assigned RMA number if available, otherwise fallback to request number
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("DEPOT_STATUS_CHANGED");
            auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
            auditLog.setNewValue("Stage: AT_DEPOT_RECEIVED");
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Item marked as received at Bangalore depot");

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Items marked as Received at Depot");
    }

    /**
     * Helper method to extract client IP address from request
     */
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

    // 5) POST: dispatch LOCAL repaired items to Customer
    @PostMapping("/dispatch-to-customer")
    public ResponseEntity<?> dispatchToCustomer(HttpServletRequest request, @RequestBody DepotDispatchRequest req) {
        // Get logged in user for audit
        String loggedInUserEmail = null;
        String loggedInUserName = null;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found for dispatch");
        }

        // Check if any items are already dispatched
        List<String> alreadyDispatchedSerials = items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsDispatched()))
                .map(RmaItemEntity::getSerialNo)
                .toList();

        if (!alreadyDispatchedSerials.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Items already dispatched (Serial No): " + alreadyDispatchedSerials
                            + ". Cannot dispatch again.");
        }

        for (RmaItemEntity item : items) {
            String oldStatus = item.getRepairStatus();
            String oldStage = item.getLocalStage();

            // Set dispatch tracking fields
            item.setIsDispatched(true);
            item.setDispatchedDate(java.time.ZonedDateTime.now());
            item.setDispatchedByEmail(loggedInUserEmail);
            item.setDispatchedByName(loggedInUserName);
            item.setRmaStatus("DISPATCHED");
            item.setLocalStage("DISPATCHED");
            item.setDispatchTo("CUSTOMER"); // Explicitly set dispatch_to

            // Create Audit Log for Dispatch to Customer
            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("DISPATCHED_TO_CUSTOMER");
            auditLog.setOldValue("Status: " + (oldStatus != null ? oldStatus : "REPAIRED") +
                    ", Stage: " + (oldStage != null ? oldStage : "N/A"));
            auditLog.setNewValue("Status: DISPATCHED, Stage: DISPATCHED, Remarks: " +
                    (req.getRemarks() != null ? req.getRemarks() : "Dispatched to Customer"));
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Item dispatched to customer");

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Items dispatched to customer successfully");
    }
}
