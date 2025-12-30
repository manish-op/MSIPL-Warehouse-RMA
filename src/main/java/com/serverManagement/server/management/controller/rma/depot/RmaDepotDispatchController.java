package com.serverManagement.server.management.controller.rma.depot;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.depot.DepotDispatchDAO;
import com.serverManagement.server.management.dao.rma.common.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.depot.DepotDispatchRequest;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.depot.DepotDispatchEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotDispatchController {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private DepotDispatchDAO depotDispatchDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

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
