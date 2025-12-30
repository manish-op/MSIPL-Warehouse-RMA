package com.serverManagement.server.management.service.rma.workflow;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.common.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;

import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.service.rma.common.RmaModelMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RmaWorkflowService {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    @Autowired
    private RmaModelMapper rmaModelMapper;

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    /**
     * Get all unassigned RMA items (items without an assignee)
     */
    public ResponseEntity<?> getUnassignedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findUnassignedItems();

            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            if (loggedInUser == null || loggedInUser.getRegionEntity() == null) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            Long userRegionId = loggedInUser.getRegionEntity().getId();
            boolean isUserFromBangalore = isBangaloreUser(loggedInUser);

            List<RmaItemEntity> filteredItems = items.stream()
                    .filter(item -> {
                        if ("DEPOT".equalsIgnoreCase(item.getRepairType())) {
                            if (!isUserFromBangalore) {
                                return false;
                            }
                        }

                        if (item.getRmaRequest() == null || item.getRmaRequest().getCreatedByEmail() == null) {
                            return false;
                        }
                        AdminUserEntity creator = adminUserDAO.findByEmail(
                                item.getRmaRequest().getCreatedByEmail().toLowerCase());
                        if (creator == null || creator.getRegionEntity() == null) {
                            return false;
                        }
                        return userRegionId.equals(creator.getRegionEntity().getId());
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch unassigned items: " + e.getMessage());
        }
    }

    /**
     * Get all assigned RMA items
     */
    public ResponseEntity<?> getAssignedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findAssignedItems();

            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);

            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch assigned items: " + e.getMessage());
        }
    }

    /**
     * Get all repaired RMA items
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRepairedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findRepairedItems();

            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch repaired items: " + e.getMessage());
        }
    }

    /**
     * Get all items that can't be repaired
     */
    public ResponseEntity<?> getCantBeRepairedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findCantBeRepairedItems();

            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch cant be repaired items: " + e.getMessage());
        }
    }

    /**
     * Get all dispatched items
     */
    public ResponseEntity<?> getDispatchedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findByIsDispatched(true);

            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
            }

            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch dispatched items: " + e.getMessage());
        }
    }

    /**
     * Assign a technician to an RMA item
     */
    @Transactional
    public ResponseEntity<?> assignItem(HttpServletRequest request, Long itemId, String assigneeEmail,
            String assigneeName) {
        try {
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            AdminUserEntity loggedInUser = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (loggedInUserEmail == null || loggedInUserEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (assigneeEmail == null || assigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee email is required");
            }

            if (!assigneeEmail.matches(EMAIL_REGEX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
            }

            if (assigneeName == null || assigneeName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee name is required");
            }

            boolean isAdmin = false;
            if (loggedInUser != null && loggedInUser.getRoleModel() != null
                    && "Admin".equalsIgnoreCase(loggedInUser.getRoleModel().getRoleName())) {
                isAdmin = true;
            }

            if (!isAdmin) {
                if (!assigneeEmail.trim().equalsIgnoreCase(loggedInUserEmail.trim())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access Denied: You can only assign items to yourself");
                }
            }

            assigneeName = assigneeName.replaceAll("<[^>]*>", "").trim();
            if (assigneeName.length() > 100) {
                assigneeName = assigneeName.substring(0, 100);
            }

            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String itemRmaNo = item.getRmaNo();
            String requestRmaNo = item.getRmaRequest() != null ? item.getRmaRequest().getRmaNo() : null;

            boolean hasRmaNo = (itemRmaNo != null && !itemRmaNo.trim().isEmpty()
                    && !"Unknown".equalsIgnoreCase(itemRmaNo))
                    || (requestRmaNo != null && !requestRmaNo.trim().isEmpty()
                            && !"Unknown".equalsIgnoreCase(requestRmaNo));

            if (!hasRmaNo) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cannot assign technician: RMA Number is missing or invalid. Please generate or update RMA number first.");
            }

            String oldStatus = item.getRepairStatus();
            String oldAssignee = item.getAssignedToEmail();

            item.setAssignedToEmail(assigneeEmail.toLowerCase().trim());
            item.setAssignedToName(assigneeName);
            item.setAssignedDate(ZonedDateTime.now());
            item.setLastReassignmentReason(null);
            item.setRepairStatus("ASSIGNED");

            rmaItemDAO.save(item);

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(itemId);
            auditLog.setRmaNo(item.getRmaRequest() != null ? item.getRmaRequest().getRmaNo() : null);
            auditLog.setAction("ASSIGNED");
            auditLog.setOldValue(
                    oldAssignee != null ? "Assigned to: " + oldAssignee + ", Status: " + oldStatus : "Unassigned");
            auditLog.setNewValue("Assigned to: " + assigneeEmail);
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Item assigned to " + assigneeName);

            rmaAuditLogDAO.save(auditLog);

            return ResponseEntity.ok("Item assigned successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to assign item");
        }
    }

    /**
     * Reassign an RMA item to a different technician with reason
     */
    @Transactional
    public ResponseEntity<?> reassignItem(HttpServletRequest request, Long itemId, String newAssigneeEmail,
            String newAssigneeName, String reason) {
        try {
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (loggedInUserEmail == null || loggedInUserEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (newAssigneeEmail == null || newAssigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New assignee email is required");
            }

            if (!newAssigneeEmail.matches(EMAIL_REGEX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
            }

            if (newAssigneeName == null || newAssigneeName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New assignee name is required");
            }

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reassignment reason is required");
            }

            newAssigneeName = newAssigneeName.replaceAll("<[^>]*>", "").trim();
            if (newAssigneeName.length() > 100) {
                newAssigneeName = newAssigneeName.substring(0, 100);
            }
            reason = reason.replaceAll("<[^>]*>", "").trim();
            if (reason.length() > 500) {
                reason = reason.substring(0, 500);
            }

            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String oldAssigneeEmail = item.getAssignedToEmail();
            String oldAssigneeName = item.getAssignedToName();

            if (oldAssigneeEmail == null || oldAssigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item is not currently assigned. Use assign instead of reassign.");
            }

            item.setAssignedToEmail(newAssigneeEmail.toLowerCase().trim());
            item.setAssignedToName(newAssigneeName);
            item.setAssignedDate(ZonedDateTime.now());
            item.setLastReassignmentReason(reason);
            rmaItemDAO.save(item);

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(itemId);
            auditLog.setRmaNo(item.getRmaRequest() != null ? item.getRmaRequest().getRmaNo() : null);
            auditLog.setAction("REASSIGNED");
            auditLog.setOldValue("Assigned to: " + oldAssigneeName + " (" + oldAssigneeEmail + ")");
            auditLog.setNewValue("Reassigned to: " + newAssigneeName + " (" + newAssigneeEmail + ")");
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Reason: " + reason);

            rmaAuditLogDAO.save(auditLog);

            return ResponseEntity.ok("Item reassigned successfully from " + oldAssigneeName + " to " + newAssigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to reassign item");
        }
    }

    /**
     * Update the repair status of an RMA item
     */
    @Transactional
    public ResponseEntity<?> updateItemStatus(HttpServletRequest request, Long itemId, String status, String remarks,
            String issueFixed) {
        try {
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status is required");
            }

            String normalizedStatus = status.toUpperCase().trim();
            boolean isValidStatus = false;
            try {
                com.serverManagement.server.management.enums.RepairStatus.valueOf(normalizedStatus);
                isValidStatus = true;
            } catch (IllegalArgumentException e) {
                isValidStatus = false;
            }

            if (!isValidStatus) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid status. Must be one of: " +
                                Arrays.toString(com.serverManagement.server.management.enums.RepairStatus.values()));
            }

            if (issueFixed == null || issueFixed.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Issue Fixed is required");
            }

            String oldStatus = item.getRepairStatus();

            String oldRemarks = item.getRepairRemarks();

            item.setRepairStatus(normalizedStatus);
            item.setRepairRemarks(remarks);
            item.setIssueFixed(issueFixed.trim());

            if (normalizedStatus.equals("REPAIRED") || normalizedStatus.equals("REPLACED")) {
                if (item.getAssignedToEmail() != null && !item.getAssignedToEmail().isEmpty()) {
                    item.setRepairedByEmail(item.getAssignedToEmail());
                    item.setRepairedByName(item.getAssignedToName());
                } else {
                    item.setRepairedByEmail(loggedInUserEmail);
                    item.setRepairedByName(loggedInUserName);
                }
                item.setRepairedDate(ZonedDateTime.now());
            }

            rmaItemDAO.save(item);

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(itemId);
            auditLog.setRmaNo(item.getRmaRequest() != null ? item.getRmaRequest().getRmaNo() : null);
            auditLog.setAction("STATUS_CHANGED");
            auditLog.setOldValue("Status: " + (oldStatus != null ? oldStatus : "null") +
                    (oldRemarks != null ? ", Remarks: " + oldRemarks : ""));
            auditLog.setNewValue("Status: " + normalizedStatus +
                    (remarks != null ? ", Remarks: " + remarks : ""));
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("Status changed from " + oldStatus + " to " + normalizedStatus);

            rmaAuditLogDAO.save(auditLog);

            return ResponseEntity.ok("Status updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update status");
        }
    }

    /**
     * Update the RMA number for an RMA item
     */
    @Transactional
    public ResponseEntity<?> updateItemRmaNumber(HttpServletRequest request, Long itemId, String rmaNo) {
        try {
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (rmaNo == null || rmaNo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RMA Number is required");
            }

            String newRmaNo = rmaNo.trim();
            String oldRmaNo = item.getRmaNo();

            item.setRmaNo(newRmaNo);
            rmaItemDAO.save(item);

            RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
            auditLog.setRmaItemId(itemId);
            auditLog.setRmaNo(newRmaNo);
            auditLog.setAction("RMA_NUMBER_UPDATE");
            auditLog.setOldValue("RMA No: " + (oldRmaNo != null ? oldRmaNo : "null"));
            auditLog.setNewValue("RMA No: " + newRmaNo);
            auditLog.setPerformedByEmail(loggedInUserEmail);
            auditLog.setPerformedByName(loggedInUserName);
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setRemarks("RMA Number updated manually");

            rmaAuditLogDAO.save(auditLog);

            return ResponseEntity.ok("RMA Number updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update RMA Number");
        }
    }

    /**
     * Confirm delivery of dispatched items
     */
    public ResponseEntity<?> confirmDelivery(HttpServletRequest request, List<Long> itemIds,
            String deliveredTo, String deliveredBy, String deliveryNotes) {
        try {
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (itemIds == null || itemIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No items selected for delivery confirmation");
            }

            if (deliveredTo == null || deliveredTo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("'Delivered To' is required");
            }

            List<RmaItemEntity> items = rmaItemDAO.findAllById(itemIds);
            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No items found");
            }

            ZonedDateTime now = ZonedDateTime.now();
            int confirmedCount = 0;

            for (RmaItemEntity item : items) {
                if (Boolean.TRUE.equals(item.getIsDispatched())) {
                    item.setDeliveredTo(deliveredTo.trim());
                    item.setDeliveredBy(deliveredBy != null ? deliveredBy.trim() : null);
                    item.setDeliveryDate(now);
                    item.setDeliveryNotes(deliveryNotes != null ? deliveryNotes.trim() : null);
                    item.setDeliveryConfirmedByEmail(loggedInUserEmail);
                    item.setDeliveryConfirmedByName(loggedInUserName);
                    item.setDeliveryConfirmedDate(now);
                    item.setRmaStatus("DELIVERED");

                    rmaItemDAO.save(item);
                    confirmedCount++;

                    RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
                    auditLog.setRmaItemId(item.getId());
                    auditLog.setRmaNo(item.getRmaNo());
                    auditLog.setAction("DELIVERY_CONFIRMED");
                    auditLog.setNewValue("Delivered to: " + deliveredTo);
                    auditLog.setPerformedByEmail(loggedInUserEmail);
                    auditLog.setPerformedByName(loggedInUserName);
                    auditLog.setIpAddress(getClientIpAddress(request));
                    auditLog.setRemarks(deliveryNotes);
                    rmaAuditLogDAO.save(auditLog);
                }
            }

            return ResponseEntity.ok("Delivery confirmed for " + confirmedCount + " item(s)");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to confirm delivery: " + e.getMessage());
        }
    }

    /**
     * Bulk assign all unassigned items with a given RMA number to a technician
     */
    @Transactional
    public ResponseEntity<?> bulkAssignByRmaNo(HttpServletRequest request, String rmaNo, String assigneeEmail,
            String assigneeName) {
        try {
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            AdminUserEntity loggedInUser = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (loggedInUserEmail == null || loggedInUserEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (rmaNo == null || rmaNo.trim().isEmpty() || "Unknown".equalsIgnoreCase(rmaNo)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valid RMA number is required");
            }

            if (assigneeEmail == null || assigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee email is required");
            }

            if (!assigneeEmail.matches(EMAIL_REGEX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
            }

            if (assigneeName == null || assigneeName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee name is required");
            }

            boolean isAdmin = false;
            if (loggedInUser != null && loggedInUser.getRoleModel() != null
                    && "Admin".equalsIgnoreCase(loggedInUser.getRoleModel().getRoleName())) {
                isAdmin = true;
            }

            if (!isAdmin) {
                if (!assigneeEmail.trim().equalsIgnoreCase(loggedInUserEmail.trim())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access Denied: You can only assign items to yourself");
                }
            }

            assigneeName = assigneeName.replaceAll("<[^>]*>", "").trim();
            if (assigneeName.length() > 100) {
                assigneeName = assigneeName.substring(0, 100);
            }

            List<RmaItemEntity> unassignedItems = rmaItemDAO.findUnassignedByRmaNo(rmaNo);

            if (unassignedItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No unassigned items found for RMA: " + rmaNo);
            }

            int assignedCount = 0;
            for (RmaItemEntity item : unassignedItems) {
                String oldStatus = item.getRepairStatus();
                String oldAssignee = item.getAssignedToEmail();

                item.setAssignedToEmail(assigneeEmail.toLowerCase().trim());
                item.setAssignedToName(assigneeName);
                item.setAssignedDate(ZonedDateTime.now());
                item.setRepairStatus("ASSIGNED");

                rmaItemDAO.save(item);
                assignedCount++;

                RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
                auditLog.setRmaItemId(item.getId());
                auditLog.setRmaNo(rmaNo);
                auditLog.setAction("BULK_ASSIGNED");
                auditLog.setOldValue(
                        oldAssignee != null ? "Assigned to: " + oldAssignee + ", Status: " + oldStatus : "Unassigned");
                auditLog.setNewValue("Assigned to: " + assigneeEmail + " (Bulk Assignment)");
                auditLog.setPerformedByEmail(loggedInUserEmail);
                auditLog.setPerformedByName(loggedInUserName);
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setRemarks("Bulk assigned " + assignedCount + " items to " + assigneeName);

                rmaAuditLogDAO.save(auditLog);
            }

            return ResponseEntity.ok("Successfully assigned " + assignedCount + " items to " + assigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to bulk assign items");
        }
    }

    // ============ HELPERS ============

    private boolean isAdmin(AdminUserEntity user) {
        return user != null && user.getRoleModel() != null
                && "admin".equalsIgnoreCase(user.getRoleModel().getRoleName());
    }

    private boolean isBangaloreUser(AdminUserEntity user) {
        if (user == null || user.getRegionEntity() == null) {
            return false;
        }
        String city = user.getRegionEntity().getCity();
        return city != null && city.toLowerCase().contains("bangalore");
    }

    private AdminUserEntity getLoggedInUser(HttpServletRequest request) {
        try {
            String email = request.getUserPrincipal().getName();
            AdminUserEntity user = adminUserDAO.findByEmail(email.toLowerCase());
            if (user != null) {
                if (user.getRoleModel() != null) {
                    user.getRoleModel().getRoleName();
                }
                if (user.getRegionEntity() != null) {
                    user.getRegionEntity().getCity();
                }
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private List<RmaItemEntity> filterItemsByAccess(List<RmaItemEntity> items, AdminUserEntity user,
            boolean filterByAssignee) {
        if (user == null) {
            return new ArrayList<>();
        }

        String userEmail = user.getEmail().toLowerCase();
        boolean isUserAdmin = isAdmin(user);
        boolean isUserFromBangalore = isBangaloreUser(user);

        return items.stream()
                .filter(item -> {
                    if ("DEPOT".equalsIgnoreCase(item.getRepairType())) {
                        if (!isUserAdmin && !isUserFromBangalore) {
                            return false;
                        }
                    }

                    if (isUserAdmin) {
                        return true;
                    }

                    if (filterByAssignee) {
                        return userEmail.equalsIgnoreCase(item.getAssignedToEmail());
                    }

                    return true;
                })
                .collect(Collectors.toList());
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
