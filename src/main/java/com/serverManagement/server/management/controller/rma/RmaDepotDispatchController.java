package com.serverManagement.server.management.controller.rma;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.DepotDispatchItemDto;
import com.serverManagement.server.management.dto.rma.DepotDispatchRequest;
import com.serverManagement.server.management.dto.rma.ProofOfDeliveryRequest;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.DepotDispatchEntity;
import com.serverManagement.server.management.entity.rma.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotDispatchController {

    private final RmaItemDAO rmaItemDAO;

    @Autowired
    private com.serverManagement.server.management.dao.rma.DepotProofOfDeliveryDAO depotProofOfDeliveryDAO;

    private final AdminUserDAO adminUserDAO;

    public RmaDepotDispatchController(RmaItemDAO rmaItemDAO,
            AdminUserDAO adminUserDAO) {
        this.rmaItemDAO = rmaItemDAO;
        this.adminUserDAO = adminUserDAO;
    }

    @Autowired
    private com.serverManagement.server.management.dao.rma.RmaRequestDAO rmaRequestDAO;

    @Autowired
    private com.serverManagement.server.management.dao.rma.RmaAuditLogDAO rmaAuditLogDAO;

    // 1) GET: depot items waiting for first dispatch
    @GetMapping("/depot/ready-to-dispatch")
    public List<DepotDispatchItemDto> getDepotReadyToDispatch() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStage("DEPOT", "PENDING_DISPATCH_TO_DEPOT")
                .stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

    @Autowired
    private com.serverManagement.server.management.dao.rma.DepotDispatchDAO depotDispatchDAO;

    // 2) POST: mark as dispatched to Bangalore
    @PostMapping("/depot/dispatch-to-bangalore")
    public ResponseEntity<?> dispatchToBangalore(HttpServletRequest request,
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

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found for dispatch");
        }

        List<String> alreadyDispatchedSerials = items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsDispatched()))
                .map(RmaItemEntity::getSerialNo)
                .toList();

        if (!alreadyDispatchedSerials.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Items already dispatched (Serial No): " + alreadyDispatchedSerials
                            + ". Cannot dispatch again.");
        }

        DepotDispatchEntity dispatch = new DepotDispatchEntity();
        dispatch.setDcNo(req.getDcNo());
        dispatch.setEwayBillNo(req.getEwayBillNo());
        dispatch.setCourierName(req.getCourierName());
        dispatch.setTrackingNo(req.getTrackingNo());
        dispatch.setRemarks(req.getRemarks());

        if (req.getDispatchDate() != null) {
            dispatch.setDispatchDate(
                    req.getDispatchDate().atStartOfDay(java.time.ZoneId.systemDefault()));
        } else {
            dispatch.setDispatchDate(ZonedDateTime.now());
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("IN_TRANSIT_TO_DEPOT");
            item.setDepotDispatch(dispatch);

            item.setIsDispatched(true);
            item.setDispatchedDate(ZonedDateTime.now());
            item.setDispatchedByEmail(loggedInUserEmail);
            item.setDispatchedByName(loggedInUserName);
            item.setRmaStatus("DISPATCHED");
            item.setRepairStatus("DISPATCHED_TO_DEPOT");
            item.setDispatchTo("BANGALORE");

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
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

        return ResponseEntity.ok("Dispatched to Bangalore");
    }

    // 2.5) GET: Next DC Number
    @GetMapping("/depot/next-dc-no")
    public ResponseEntity<?> getNextDcNo() {
        try {
            // FIX: Get last *Non-Null* DC No ordered by DATE (latest first) to handle
            // updates to old IDs
            DepotDispatchEntity lastDispatch = depotDispatchDAO.findTopByDcNoIsNotNullOrderByDispatchDateDesc();
            String nextVal = "1";
            if (lastDispatch != null && lastDispatch.getDcNo() != null) {
                String lastDcNo = lastDispatch.getDcNo().trim();
                try {
                    long val = Long.parseLong(lastDcNo);
                    nextVal = String.valueOf(val + 1);
                } catch (NumberFormatException e) {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)$");
                    java.util.regex.Matcher m = p.matcher(lastDcNo);
                    if (m.find()) {
                        String numStr = m.group(1);
                        long val = Long.parseLong(numStr);
                        String prefix = lastDcNo.substring(0, lastDcNo.length() - numStr.length());
                        nextVal = prefix + (val + 1);
                    } else {
                        nextVal = lastDcNo;
                    }
                }
            }
            return ResponseEntity.ok(java.util.Collections.singletonMap("dcNo", nextVal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Error generating DC No: " + e.getMessage()));
        }
    }

    // 3) GET: depot items in transit or at depot
    @GetMapping("/depot/in-transit")
    public List<DepotDispatchItemDto> getInTransitItems() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStageIn("DEPOT", List.of(
                        "IN_TRANSIT_TO_DEPOT",
                        "AT_DEPOT_RECEIVED",
                        "AT_DEPOT_UNREPAIRED",
                        "AT_DEPOT_REPAIRING",
                        "AT_DEPOT_REPAIRED",
                        "IN_TRANSIT_FROM_DEPOT",
                        "GGN_RECEIVED_FROM_DEPOT",
                        "GGN_DISPATCHED_TO_CUSTOMER_HAND",
                        "GGN_DISPATCHED_TO_CUSTOMER_COURIER",
                        "GGN_DELIVERED_TO_CUSTOMER"))
                .stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

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
            item.setRepairStatus("RECEIVED_AT_DEPOT");

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
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
            List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No items found");
            }

            for (RmaItemEntity item : items) {
                if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                    continue;
                }

                String oldStage = item.getDepotStage();
                item.setDepotStage("AT_DEPOT_REPAIRED");

                String status = req.getRepairStatus();
                if (status == null || status.trim().isEmpty()) {
                    status = "REPAIRED";
                }
                item.setRepairStatus(status + "_AT_DEPOT");

                RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
                auditLog.setRmaItemId(item.getId());
                String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                        : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
                auditLog.setRmaNo(rmaNo);
                auditLog.setAction("DEPOT_STATUS_CHANGED");
                auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
                auditLog.setNewValue("Stage: AT_DEPOT_REPAIRED (" + status + ")");
                auditLog.setPerformedByEmail(loggedInUserEmail);
                auditLog.setPerformedByName(loggedInUserName);
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setRemarks("Item marked as " + status + " at Depot");

                rmaAuditLogDAO.save(auditLog);
            }

            rmaItemDAO.saveAll(items);
            return ResponseEntity.ok("Items marked as "
                    + (req.getRepairStatus() != null ? req.getRepairStatus() : "Repaired") + " at Depot");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking repaired: " + e.getMessage());
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

    // 5) existing LOCAL dispatch to customer – unchanged
    @PostMapping("/dispatch-to-customer")
    public ResponseEntity<?> dispatchToCustomer(HttpServletRequest request,
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

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found for dispatch");
        }

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

            item.setIsDispatched(true);
            item.setDispatchedDate(ZonedDateTime.now());
            item.setDispatchedByEmail(loggedInUserEmail);
            item.setDispatchedByName(loggedInUserName);
            item.setRmaStatus("DISPATCHED");
            item.setLocalStage("DISPATCHED");
            item.setDispatchTo("CUSTOMER");

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

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No items found");
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("GGN_RECEIVED_FROM_DEPOT");
            item.setRepairStatus("RECEIVED_AT_GURGAON");

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("DEPOT_STATUS_CHANGED");
            auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
            auditLog.setNewValue("Stage: GGN_RECEIVED_FROM_DEPOT");
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Repaired depot item received at Gurgaon");

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Items marked as Received at Gurgaon");
    }

    // 7) NEW: dispatch repaired depot item back to GGN or Customer
    @PostMapping("/depot/dispatch-return")
    public ResponseEntity<?> dispatchReturn(HttpServletRequest request,
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

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No items found");
        }

        for (RmaItemEntity item : items) {
            // Only allow dispatch from correct stages
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("IN_TRANSIT_FROM_DEPOT");

            // Set Dispatch Details
            item.setDepotReturnDcNo(req.getDcNo());
            item.setDepotReturnEwayBillNo(req.getEwayBillNo());
            item.setDepotReturnMethod(req.getDispatchMode() != null ? req.getDispatchMode() : "COURIER");
            item.setDepotReturnDispatchDate(ZonedDateTime.now());

            // Dispatch To Logic
            if ("CUSTOMER".equalsIgnoreCase(req.getDispatchTo())) {
                item.setDispatchTo("CUSTOMER");
                item.setRepairStatus("DISPATCHED_TO_CUSTOMER_FROM_DEPOT");
            } else {
                item.setDispatchTo("GURGAON");
                item.setRepairStatus("DISPATCHED_TO_GURGAON");
            }

            if ("COURIER".equalsIgnoreCase(item.getDepotReturnMethod())) {
                item.setDepotReturnCourierName(req.getCourierName());
                item.setDepotReturnTrackingNo(req.getTrackingNo());
            } else {
                // Handled locally or other method
                item.setDepotReturnHandlerName(req.getHandlerName());
                item.setDepotReturnHandlerContact(req.getHandlerContact());
            }

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("DEPOT_RETURN_DISPATCH");
            auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
            auditLog.setNewValue("Stage: IN_TRANSIT_FROM_DEPOT, To: " + item.getDispatchTo());
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Item dispatched from Depot to " + item.getDispatchTo());

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Items dispatched from Depot successfully");
    }

    // 7.5) DEPRECATED/OLD: plan dispatch from Gurgaon to Customer (Local logic or
    // secondary step)
    // Kept for backward compatibility if needed, but above covers the depot return
    // flow.
    @PostMapping("/depot/ggn-dispatch-plan")
    public ResponseEntity<?> planDispatchFromGgn(HttpServletRequest request,
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
            return ResponseEntity.badRequest().body("No items in request");
        }
        if (req.getDispatchMode() == null) {
            return ResponseEntity.badRequest().body("Dispatch mode (HAND/COURIER) is required");
        }

        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No items found");
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();

            item.setDepotReturnMethod(req.getDispatchMode());
            item.setDepotReturnDispatchDate(ZonedDateTime.now());
            item.setDispatchTo("CUSTOMER");
            item.setIsDispatched(true);
            item.setDispatchedByEmail(loggedInUserEmail);
            item.setDispatchedByName(loggedInUserName);
            item.setRmaStatus("DISPATCHED");

            if ("COURIER".equalsIgnoreCase(req.getDispatchMode())) {
                item.setDepotReturnCourierName(req.getCourierName());
                item.setDepotReturnTrackingNo(req.getTrackingNo());
                item.setDepotStage("GGN_DISPATCHED_TO_CUSTOMER_COURIER");
            } else {
                item.setDepotReturnHandlerName(req.getHandlerName());
                item.setDepotReturnHandlerContact(req.getHandlerContact());
                item.setDepotStage("GGN_DISPATCHED_TO_CUSTOMER_HAND");
            }
            item.setRepairStatus("DISPATCHED_TO_CUSTOMER");

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(item.getId());
            String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                    : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
            auditLog.setRmaNo(rmaNo);
            auditLog.setAction("GGN_DISPATCH_TO_CUSTOMER");
            auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
            auditLog.setNewValue("Stage: " + item.getDepotStage() +
                    ", Mode: " + req.getDispatchMode() +
                    ", Courier: " + (req.getCourierName() != null ? req.getCourierName() : "N/A") +
                    ", Tracking: " + (req.getTrackingNo() != null ? req.getTrackingNo() : "N/A"));
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Item dispatched from Gurgaon to customer");

            rmaAuditLogDAO.save(auditLog);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Dispatch from Gurgaon planned/executed");
    }

    // 8) NEW: upload signed DC / proof of delivery and close depot cycle
    @PostMapping("/depot/upload-proof-of-delivery")
    public ResponseEntity<?> uploadProofOfDelivery(HttpServletRequest request,
            @RequestBody ProofOfDeliveryRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        RmaItemEntity item = rmaItemDAO.findById(req.getItemId()).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item not found");
        }

        String oldStage = item.getDepotStage();

        item.setDepotProofOfDeliveryFileId(req.getFileId());
        item.setDepotProofOfDeliveryRemarks(req.getRemarks());
        item.setDepotReturnDeliveredDate(ZonedDateTime.now());
        item.setDepotStage("GGN_DELIVERED_TO_CUSTOMER");
        item.setDepotCycleClosed(Boolean.TRUE);
        item.setRmaStatus("DELIVERED");
        item.setRepairStatus("DELIVERED_TO_CUSTOMER");

        String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);

        // Save to new table
        com.serverManagement.server.management.entity.rma.DepotProofOfDeliveryEntity pod = new com.serverManagement.server.management.entity.rma.DepotProofOfDeliveryEntity();
        pod.setRmaItemId(item.getId());
        pod.setRmaNo(rmaNo);
        pod.setFileId(req.getFileId());
        pod.setRemarks(req.getRemarks());
        pod.setUploadedAt(ZonedDateTime.now());
        pod.setUploadedBy(loggedInUserEmail);

        // Copy dispatch details from item
        pod.setDispatchMode(item.getDepotReturnMethod());
        pod.setHandlerName(item.getDepotReturnHandlerName());
        pod.setCourierName(item.getDepotReturnCourierName());
        pod.setTrackingNo(item.getDepotReturnTrackingNo());

        depotProofOfDeliveryDAO.save(pod);

        RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
        auditLog.setRmaItemId(item.getId());
        auditLog.setRmaNo(rmaNo);

        auditLog.setAction("PROOF_OF_DELIVERY_UPLOADED");
        auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
        auditLog.setNewValue("Stage: GGN_DELIVERED_TO_CUSTOMER, POD File: " + req.getFileId());
        auditLog.setPerformedByEmail(loggedInUserEmail);
        auditLog.setPerformedByName(loggedInUserName);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setRemarks("Proof of Delivery uploaded for depot repair");

        rmaAuditLogDAO.save(auditLog);
        rmaItemDAO.save(item);

        return ResponseEntity.ok("Proof of Delivery Uploaded");
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

        RmaItemEntity originalItem = rmaItemDAO.findById(itemId).orElse(null);
        if (originalItem == null) {
            return ResponseEntity.badRequest().body("Original item not found");
        }

        // 1. Mark old item as FAULTY
        String oldStage = originalItem.getDepotStage();
        originalItem.setDepotStage("GGN_RETURNED_FAULTY");
        originalItem.setDepotCycleClosed(Boolean.TRUE);
        originalItem.setRmaStatus("CLOSED_FAULTY");
        originalItem.setRemarks("Closed as Faulty returned from Depot. New RMA generated.");
        rmaItemDAO.save(originalItem);

        // Log for old item
        RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
        auditLog.setRmaItemId(originalItem.getId());
        String oldRmaNo = originalItem.getRmaNo() != null ? originalItem.getRmaNo()
                : (originalItem.getRmaRequest() != null ? originalItem.getRmaRequest().getRequestNumber() : null);
        auditLog.setRmaNo(oldRmaNo);
        auditLog.setAction("DEPOT_RETURNED_FAULTY");
        auditLog.setOldValue("Stage: " + oldStage);
        auditLog.setNewValue("Status: CLOSED_FAULTY");
        auditLog.setPerformedByEmail(loggedInUserEmail);
        auditLog.setPerformedByName(loggedInUserName);
        auditLog.setRemarks("Item marked faulty. Cycle closed.");
        rmaAuditLogDAO.save(auditLog);

        // 2. CREATE NEW RMA REQUEST
        com.serverManagement.server.management.entity.rma.RmaRequestEntity originalRequest = originalItem
                .getRmaRequest();

        com.serverManagement.server.management.entity.rma.RmaRequestEntity newRequest = new com.serverManagement.server.management.entity.rma.RmaRequestEntity();

        // Generate new Request Number
        String newRequestNumber = "RMA-" + (System.currentTimeMillis() / 1000);
        newRequest.setRequestNumber(newRequestNumber);

        if (originalRequest != null) {
            newRequest.setCustomer(originalRequest.getCustomer());
            newRequest.setCompanyName(originalRequest.getCompanyName());
            newRequest.setContactName(originalRequest.getContactName());
            newRequest.setEmail(originalRequest.getEmail());
            newRequest.setMobile(originalRequest.getMobile());
            newRequest.setTelephone(originalRequest.getTelephone());
            newRequest.setReturnAddress(originalRequest.getReturnAddress());
        }

        newRequest.setCreatedDate(ZonedDateTime.now());
        newRequest.setCreatedByEmail(loggedInUserEmail);

        rmaRequestDAO.save(newRequest);

        // 3. CREATE NEW RMA ITEM linked to new request
        RmaItemEntity newItem = new RmaItemEntity();
        newItem.setRmaRequest(newRequest);
        newItem.setRmaNo(newRequestNumber);
        newItem.setSerialNo(originalItem.getSerialNo());
        newItem.setProduct(originalItem.getProduct());
        newItem.setModel(originalItem.getModel());
        newItem.setRepairStatus(originalItem.getRepairStatus());

        // Copy technical details
        newItem.setFmUlatex(originalItem.getFmUlatex());
        newItem.setCodeplug(originalItem.getCodeplug());
        newItem.setFlashCode(originalItem.getFlashCode());
        newItem.setEncryption(originalItem.getEncryption());
        newItem.setFirmwareVersion(originalItem.getFirmwareVersion());
        newItem.setLowerFirmwareVersion(originalItem.getLowerFirmwareVersion());
        newItem.setInvoiceNo(originalItem.getInvoiceNo());
        newItem.setDateCode(originalItem.getDateCode());

        newItem.setFaultDescription("Re-created from Faulty Depot Return. Ref Old RMA: " + oldRmaNo);
        // Per user request:
        // 1. RMA Number should NOT be auto-filled (so we leave rmaNo null or empty)
        // 2. Should appear in "Ready to Dispatch" -> depotStage = PENDING_DISPATCH
        // 3. Should be "mark repaired" button -> which appears in AT_DEPOT_RECEIVED

        newItem.setRepairType("DEPOT");
        newItem.setDepotStage("PENDING_DISPATCH_TO_DEPOT");
        newItem.setRmaStatus("OPEN");
        newItem.setRepairStatus("UNREPAIRED");
        newItem.setRmaNo(null); // No auto-filled RMA No

        rmaItemDAO.save(newItem);

        // Log creation
        RmaAuditLogEntity newLog = new RmaAuditLogEntity();
        newLog.setRmaItemId(newItem.getId());
        newLog.setRmaNo(newRequestNumber);
        newLog.setAction("AUTO_GENERATED_RMA");
        newLog.setNewValue("Generated from faulty item ID: " + originalItem.getId());
        newLog.setPerformedByEmail(loggedInUserEmail);
        newLog.setPerformedByName(loggedInUserName);
        rmaAuditLogDAO.save(newLog);

        return ResponseEntity.ok("New RMA Created: " + newRequestNumber);
    }

    // 9) NEW: when device is returned to Gurgaon faulty again → close old RMA and
    // create new one
    public static class NewRmaResponse {
        private String newRmaNo;
        private Long newRmaItemId;

        public String getNewRmaNo() {
            return newRmaNo;
        }

        public void setNewRmaNo(String newRmaNo) {
            this.newRmaNo = newRmaNo;
        }

        public Long getNewRmaItemId() {
            return newRmaItemId;
        }

        public void setNewRmaItemId(Long newRmaItemId) {
            this.newRmaItemId = newRmaItemId;
        }
    }

    @PostMapping("/depot/mark-faulty-and-create-rma/{itemId}")
    public ResponseEntity<?> markFaultyAndCreateNewRma(HttpServletRequest request,
            @PathVariable Long itemId) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
            loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        RmaItemEntity oldItem = rmaItemDAO.findById(itemId).orElse(null);
        if (oldItem == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item not found");
        }

        String oldStage = oldItem.getDepotStage();
        oldItem.setDepotStage("GGN_RETURNED_FAULTY");
        oldItem.setDepotCycleClosed(Boolean.TRUE);
        oldItem.setRmaStatus("CLOSED");
        rmaItemDAO.save(oldItem);

        RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
        auditLog.setRmaItemId(oldItem.getId());
        String rmaNo = oldItem.getRmaNo() != null ? oldItem.getRmaNo()
                : (oldItem.getRmaRequest() != null ? oldItem.getRmaRequest().getRequestNumber() : null);
        auditLog.setRmaNo(rmaNo);
        auditLog.setAction("GGN_RETURNED_FAULTY");
        auditLog.setOldValue("Stage: " + (oldStage != null ? oldStage : "UNKNOWN"));
        auditLog.setNewValue("Stage: GGN_RETURNED_FAULTY, RMA CLOSED");
        auditLog.setPerformedByEmail(loggedInUserEmail);
        auditLog.setPerformedByName(loggedInUserName);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setRemarks("Device returned faulty to Gurgaon; new RMA will be created");

        rmaAuditLogDAO.save(auditLog);

        // Create a new RMA item (simplified – call your existing RMA creation logic
        // here)
        RmaItemEntity newItem = new RmaItemEntity();
        newItem.setSerialNo(oldItem.getSerialNo());
        newItem.setProduct(oldItem.getProduct());
        newItem.setCustomerName(oldItem.getCustomerName());
        newItem.setRepairType("LOCAL");
        newItem.setRmaStatus("OPEN");
        newItem.setLocalStage("REQUESTED");
        // TODO: set RMA request / number via your existing services
        rmaItemDAO.save(newItem);

        NewRmaResponse resp = new NewRmaResponse();
        resp.setNewRmaNo(newItem.getRmaNo());
        resp.setNewRmaItemId(newItem.getId());

        return ResponseEntity.ok(resp);
    }
}
