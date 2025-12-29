package com.serverManagement.server.management.controller.rma.depot;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.DepotProofOfDeliveryDAO;
import com.serverManagement.server.management.dao.rma.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.depot.ProofOfDeliveryRequest;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.depot.DepotProofOfDeliveryEntity;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotPodController {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private DepotProofOfDeliveryDAO depotProofOfDeliveryDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    // 8) NEW: upload signed DC / proof of delivery and close depot cycle
    @PostMapping("/depot/upload-proof-of-delivery")
    public ResponseEntity<?> uploadProofOfDelivery(HttpServletRequest request,
            @RequestBody ProofOfDeliveryRequest req) {

        String loggedInUserEmail;
        String loggedInUserName;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());

            // Access Check: Only Admin or Bangalore
            if (!isAdminOrBangaloreUser(loggedInUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access Denied: Only Admin or Bangalore users can upload POD.");
            }

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
        DepotProofOfDeliveryEntity pod = new DepotProofOfDeliveryEntity();
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

    private boolean isAdminOrBangaloreUser(AdminUserEntity user) {
        if (user == null) {
            return false;
        }
        // Check if Admin
        if (user.getRoleModel() != null && "admin".equalsIgnoreCase(user.getRoleModel().getRoleName())) {
            return true;
        }
        // Check if Bangalore region
        if (user.getRegionEntity() != null) {
            String city = user.getRegionEntity().getCity();
            if (city != null && city.toUpperCase().contains("BANGALORE")) {
                return true;
            }
        }
        return false;
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
