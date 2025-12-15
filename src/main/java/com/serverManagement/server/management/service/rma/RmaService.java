package com.serverManagement.server.management.service.rma;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.rma.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.RmaRequestDAO;
import com.serverManagement.server.management.dto.rma.ProductCatalogDTO;
import com.serverManagement.server.management.dto.rma.RmaItemWorkflowDTO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.RmaRequestEntity;
import com.serverManagement.server.management.request.rma.CreateRmaRequest;
import com.serverManagement.server.management.request.rma.RmaItemRequest;
import com.serverManagement.server.management.response.rma.RmaResponse;
import com.serverManagement.server.management.response.rma.RmaResponse.RmaItemResponse;
import com.serverManagement.server.management.response.rma.RmaItemsGroupedResponse;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RmaService {

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private KeywordDAO keywordDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    @Autowired
    private CustomerService customerService;

    // Email validation regex pattern
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    /**
     * Create a new RMA request
     */

    @Transactional
    public ResponseEntity<?> createRmaRequest(HttpServletRequest request, CreateRmaRequest createRmaRequest) {

        // 1. Authentication Check
        String loggedInUserEmail = null;
        try {
            loggedInUserEmail = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated, Login first");
        }

        if (loggedInUserEmail == null || loggedInUserEmail.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated, Login first");
        }

        // 2. Verify user exists in database
        AdminUserEntity adminUserEntity = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
        if (adminUserEntity == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User Not Found");
        }

        // 3. Validate Request Data
        if (createRmaRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Request body cannot be null");
        }

        // Validate required fields
        if (isBlank(createRmaRequest.getCompanyName()) || isBlank(createRmaRequest.getEmail())
                || isBlank(createRmaRequest.getContactName()) || isBlank(createRmaRequest.getTelephone())
                || isBlank(createRmaRequest.getMobile()) || isBlank(createRmaRequest.getReturnAddress())
                || isBlank(createRmaRequest.getDate()) || isBlank(createRmaRequest.getModeOfTransport())
                || isBlank(createRmaRequest.getShippingMethod())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Required fields are missing: Company Name, Email, Contact Name, Telephone, Mobile, Return Address, Date, Mode of Transport, and Shipping Method are mandatory");
        }

        // Validate items
        if (createRmaRequest.getItems() == null || createRmaRequest.getItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("At least one RMA item is required");
        }

        // Validate each item
        for (int i = 0; i < createRmaRequest.getItems().size(); i++) {
            RmaItemRequest item = createRmaRequest.getItems().get(i);
            if (isBlank(item.getProduct()) || isBlank(item.getSerialNo())
                    || isBlank(item.getFaultDescription()) || isBlank(item.getFmUlatex())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item " + (i + 1)
                                + ": Product, Serial No, Fault Description, and FM/UL/ATEX are mandatory fields");
            }
        }

        // 4. Create RMA Request Entity
        RmaRequestEntity rmaRequestEntity = new RmaRequestEntity();

        // Map basic fields
        rmaRequestEntity.setDplLicense(createRmaRequest.getDplLicense());
        rmaRequestEntity.setDate(createRmaRequest.getDate());
        rmaRequestEntity.setModeOfTransport(createRmaRequest.getModeOfTransport());
        rmaRequestEntity.setShippingMethod(createRmaRequest.getShippingMethod());
        rmaRequestEntity.setCourierCompanyName(createRmaRequest.getCourierCompanyName());

        // Map return address fields
        rmaRequestEntity.setCompanyName(createRmaRequest.getCompanyName());
        rmaRequestEntity.setEmail(createRmaRequest.getEmail());
        rmaRequestEntity.setContactName(createRmaRequest.getContactName());
        rmaRequestEntity.setTelephone(createRmaRequest.getTelephone());
        rmaRequestEntity.setMobile(createRmaRequest.getMobile());
        rmaRequestEntity.setReturnAddress(createRmaRequest.getReturnAddress());

        // Map invoice address fields (optional)
        rmaRequestEntity.setInvoiceCompanyName(createRmaRequest.getInvoiceCompanyName());
        rmaRequestEntity.setInvoiceEmail(createRmaRequest.getInvoiceEmail());
        rmaRequestEntity.setInvoiceContactName(createRmaRequest.getInvoiceContactName());
        rmaRequestEntity.setInvoiceTelephone(createRmaRequest.getInvoiceTelephone());
        rmaRequestEntity.setInvoiceMobile(createRmaRequest.getInvoiceMobile());
        rmaRequestEntity.setInvoiceAddress(createRmaRequest.getInvoiceAddress());

        // Map signature
        rmaRequestEntity.setSignature(createRmaRequest.getSignature());
        rmaRequestEntity.setRepairType(createRmaRequest.getRepairType());

        // Set audit fields
        ZonedDateTime now = ZonedDateTime.now();
        rmaRequestEntity.setCreatedByEmail(loggedInUserEmail);
        rmaRequestEntity.setCreatedDate(now);
        rmaRequestEntity.setUpdatedDate(now);

        // Generate unique request number (auto-generated when request is submitted)
        String requestNumber = generateRmaNumber(); // REQ-YYYYMMDD-HHMMSS
        rmaRequestEntity.setRequestNumber(requestNumber);

        // RMA number will be set later by service team after approval
        // Do not auto-generate RMA number for items
        rmaRequestEntity.setRmaNo(null);

        // 4a. Auto-save customer details (find existing or create new)
        // This allows customers to be reused in future RMA requests
        try {
            // Save return address customer
            var returnCustomer = customerService.findOrCreateCustomer(
                    createRmaRequest.getCompanyName(),
                    createRmaRequest.getContactName(),
                    createRmaRequest.getEmail(),
                    createRmaRequest.getTelephone(),
                    createRmaRequest.getMobile(),
                    createRmaRequest.getReturnAddress());
            rmaRequestEntity.setCustomer(returnCustomer);

            // Save invoice customer if different from return customer
            if (createRmaRequest.getInvoiceCompanyName() != null
                    && !createRmaRequest.getInvoiceCompanyName().isEmpty()
                    && createRmaRequest.getInvoiceEmail() != null
                    && !createRmaRequest.getInvoiceEmail().isEmpty()) {
                var invoiceCustomer = customerService.findOrCreateCustomer(
                        createRmaRequest.getInvoiceCompanyName(),
                        createRmaRequest.getInvoiceContactName(),
                        createRmaRequest.getInvoiceEmail(),
                        createRmaRequest.getInvoiceTelephone(),
                        createRmaRequest.getInvoiceMobile(),
                        createRmaRequest.getInvoiceAddress());
                rmaRequestEntity.setInvoiceCustomer(invoiceCustomer);
            }
        } catch (Exception e) {
            // Log but don't fail - customer save is supplementary
            System.err.println("Warning: Failed to auto-save customer: " + e.getMessage());
        }

        // 5. Create RMA Item Entities
        List<RmaItemEntity> rmaItemEntities = new ArrayList<>();
        List<RmaItemResponse> itemResponses = new ArrayList<>();

        // request level repair type
        String repairType = createRmaRequest.getRepairType(); // local or depot

        for (int i = 0; i < createRmaRequest.getItems().size(); i++) {
            RmaItemRequest itemRequest = createRmaRequest.getItems().get(i);

            RmaItemEntity itemEntity = new RmaItemEntity();

            // Map item fields
            itemEntity.setProduct(itemRequest.getProduct());
            itemEntity.setModel(itemRequest.getModel());
            itemEntity.setSerialNo(itemRequest.getSerialNo());
            itemEntity.setFaultDescription(itemRequest.getFaultDescription());
            itemEntity.setCodeplug(itemRequest.getCodeplug());
            itemEntity.setFlashCode(itemRequest.getFlashCode());
            itemEntity.setRepairStatus(itemRequest.getRepairStatus());
            itemEntity.setInvoiceNo(itemRequest.getInvoiceNo());
            itemEntity.setDateCode(itemRequest.getDateCode());
            itemEntity.setFmUlatex(itemRequest.getFmUlatex());
            itemEntity.setEncryption(itemRequest.getEncryption());
            itemEntity.setFirmwareVersion(itemRequest.getFirmwareVersion());
            itemEntity.setLowerFirmwareVersion(itemRequest.getLowerFirmwareVersion());
            itemEntity.setPartialShipment(itemRequest.getPartialShipment());
            itemEntity.setRemarks(itemRequest.getRemarks());

            // Note: RMA number is now stored at request level, not item level
            // Set to null for new records (deprecated field kept for backward
            // compatibility)
            itemEntity.setRmaNo(null);

            // Set relationship
            itemEntity.setRmaRequest(rmaRequestEntity);

            // -------------DEPOT VS LOCAL STAGE LOGIC-------------

            if ("DEPOT".equalsIgnoreCase(repairType) || "Depot Repair".equalsIgnoreCase(repairType)) {
                itemEntity.setRepairType("DEPOT"); // Normalize to DEPOT
                // First for depot items:waiting to be dispatched
                itemEntity.setDepotStage("PENDING_DISPATCH_TO_DEPOT");
                itemEntity.setLocalStage(null);
            } else {
                itemEntity.setRepairType(repairType); // Keep original for others
                // local repair starts at unrepaired page
                itemEntity.setLocalStage("UNASSIGNED");
                itemEntity.setDepotStage(null);
            }

            rmaItemEntities.add(itemEntity);

            // Create response item (RMA number will be added later)
            RmaItemResponse itemResponse = new RmaItemResponse(
                    itemRequest.getProduct(),
                    itemRequest.getSerialNo(),
                    null); // RMA number not assigned yet
            itemResponses.add(itemResponse);
        }

        // Set items to request entity
        rmaRequestEntity.setItems(rmaItemEntities);

        // 6. Save to Database (Cascade will save items too)
        RmaRequestEntity savedEntity = rmaRequestDAO.save(rmaRequestEntity);

        // 7. Create Response
        RmaResponse response = new RmaResponse();
        response.setRmaRequestId(savedEntity.getId());
        response.setRmaNo(requestNumber); // Return the request number (not item RMA number)
        response.setMessage("RMA Request submitted successfully");
        response.setTimestamp(now);
        response.setItems(itemResponses);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Generate unique RMA number
     * Format: RMA-DDMMYYYY-HHMMSS
     * Note: This number is shared by all items in a single RMA request
     */
    private String generateRmaNumber() {
        ZonedDateTime now = ZonedDateTime.now();
        String timestamp = String.format("%02d%02d%04d-%02d%02d%02d",
                now.getDayOfMonth(),
                now.getMonthValue(),
                now.getYear(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
        return String.format("RMA-%s", timestamp);
    }

    /**
     * Utility method to check if string is blank
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public ResponseEntity<?> getAllRmaItemsGrouped() {
        try {
            // Fetch all RMA requests from database with items eagerly loaded
            List<RmaRequestEntity> allRequests = rmaRequestDAO.findAllWithItems();

            // Transform into grouped response DTOs
            List<RmaItemsGroupedResponse> groupedResponses = new ArrayList<>();

            for (RmaRequestEntity request : allRequests) {
                // Create item DTOs for this request
                List<RmaItemsGroupedResponse.RmaItemDTO> itemDTOs = new ArrayList<>();

                if (request.getItems() != null) {
                    for (RmaItemEntity item : request.getItems()) {
                        RmaItemsGroupedResponse.RmaItemDTO itemDTO = new RmaItemsGroupedResponse.RmaItemDTO(
                                item.getId(),
                                item.getProduct(),
                                item.getSerialNo(),
                                item.getModel(),
                                item.getFaultDescription(),
                                item.getRepairStatus());
                        itemDTOs.add(itemDTO);
                    }
                }

                // Create grouped response for this RMA request
                RmaItemsGroupedResponse groupedResponse = new RmaItemsGroupedResponse(
                        request.getRmaNo(),
                        request.getCompanyName(),
                        request.getContactName(),
                        request.getCreatedDate(),
                        itemDTOs);
                groupedResponses.add(groupedResponse);
            }

            return ResponseEntity.ok(groupedResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch RMA items: " + e.getMessage());
        }
    }

    /**
     * Get RMA dashboard statistics
     * Returns counts of total requests, total items, repaired, and unrepaired items
     */
    public ResponseEntity<?> getRmaDashboardStats() {
        try {
            long totalRequests = rmaRequestDAO.count();
            long totalItems = rmaItemDAO.count();
            long repairedCount = rmaItemDAO.countRepaired();
            long unrepairedCount = rmaItemDAO.countUnrepaired();

            com.serverManagement.server.management.dto.rma.RmaDashboardStatsDto stats = new com.serverManagement.server.management.dto.rma.RmaDashboardStatsDto(
                    totalRequests,
                    totalItems,
                    repairedCount,
                    unrepairedCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch RMA statistics: " + e.getMessage());
        }
    }

    // ============ WORKFLOW METHODS ============

    /**
     * Get all unassigned RMA items (items without an assignee)
     */
    public ResponseEntity<?> getUnassignedItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findUnassignedItems();
            return ResponseEntity.ok(convertToItemDTOList(items));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch unassigned items: " + e.getMessage());
        }
    }

    /**
     * Get all assigned RMA items (items with assignee, not yet completed)
     */
    public ResponseEntity<?> getAssignedItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findAssignedItems();
            return ResponseEntity.ok(convertToItemDTOList(items));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch assigned items: " + e.getMessage());
        }
    }

    /**
     * Get all repaired RMA items
     */
    public ResponseEntity<?> getRepairedItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findRepairedItems();
            return ResponseEntity.ok(convertToItemDTOList(items));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch repaired items: " + e.getMessage());
        }
    }

    /**
     * Get all items that can't be repaired
     */
    public ResponseEntity<?> getCantBeRepairedItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findCantBeRepairedItems();
            return ResponseEntity.ok(convertToItemDTOList(items));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch cant be repaired items: " + e.getMessage());
        }
    }

    /**
     * Assign a technician to an RMA item
     * Security: Requires authentication, validates inputs, logs action
     */
    @Transactional
    public ResponseEntity<?> assignItem(HttpServletRequest request, Long itemId, String assigneeEmail,
            String assigneeName) {
        try {
            // 1. Authentication Check
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

            // 2. Input Validation
            if (assigneeEmail == null || assigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee email is required");
            }

            if (!assigneeEmail.matches(EMAIL_REGEX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
            }

            if (assigneeName == null || assigneeName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assignee name is required");
            }

            // Sanitize inputs - remove any HTML/script tags
            assigneeName = assigneeName.replaceAll("<[^>]*>", "").trim();
            if (assigneeName.length() > 100) {
                assigneeName = assigneeName.substring(0, 100);
            }

            // 3. Find and update item
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String oldStatus = item.getRepairStatus();
            String oldAssignee = item.getAssignedToEmail();

            item.setAssignedToEmail(assigneeEmail.toLowerCase().trim());
            item.setAssignedToName(assigneeName);
            item.setAssignedDate(ZonedDateTime.now());
            item.setRepairStatus("ASSIGNED");

            rmaItemDAO.save(item);

            // 4. Create Audit Log Entry
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
     * Update the repair status of an RMA item
     * Security: Logs all status changes for audit trail
     */
    @Transactional
    public ResponseEntity<?> updateItemStatus(HttpServletRequest request, Long itemId, String status, String remarks,
            String issueFixed) {
        try {
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            // Get logged in user
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            // Validate status
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status is required");
            }

            String normalizedStatus = status.toUpperCase().trim();
            if (!normalizedStatus.equals("REPAIRING") && !normalizedStatus.equals("REPAIRED")
                    && !normalizedStatus.equals("CANT_BE_REPAIRED") && !normalizedStatus.equals("BER")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid status. Must be: REPAIRING, REPAIRED, CANT_BE_REPAIRED, or BER");
            }

            // Validate issueFixed is mandatory
            if (issueFixed == null || issueFixed.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Issue Fixed is required");
            }

            // Capture old values for audit
            String oldStatus = item.getRepairStatus();
            String oldRemarks = item.getRepairRemarks();

            item.setRepairStatus(normalizedStatus);
            item.setRepairRemarks(remarks);
            item.setIssueFixed(issueFixed.trim());

            // If marked as repaired, record who repaired it
            if (normalizedStatus.equals("REPAIRED")) {
                item.setRepairedByEmail(loggedInUserEmail);
                item.setRepairedByName(loggedInUserName);
                item.setRepairedDate(ZonedDateTime.now());
            }

            rmaItemDAO.save(item);

            // Create Audit Log Entry
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
     * Security: Logs all changes for audit trail
     */
    @Transactional
    public ResponseEntity<?> updateItemRmaNumber(HttpServletRequest request, Long itemId, String rmaNo) {
        try {
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            // Get logged in user
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                loggedInUserEmail = request.getUserPrincipal().getName();
                AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
            } catch (NullPointerException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            // Validate RMA number
            if (rmaNo == null || rmaNo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RMA Number is required");
            }

            String newRmaNo = rmaNo.trim();
            String oldRmaNo = item.getRmaNo();

            // Update item
            item.setRmaNo(newRmaNo);
            rmaItemDAO.save(item);

            // Create Audit Log Entry
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
     * Helper method to convert entity list to DTO list
     */
    private List<RmaItemWorkflowDTO> convertToItemDTOList(List<RmaItemEntity> items) {
        List<RmaItemWorkflowDTO> dtoList = new ArrayList<>();
        for (RmaItemEntity item : items) {
            RmaItemWorkflowDTO dto = new RmaItemWorkflowDTO();
            dto.setId(item.getId());
            dto.setProduct(item.getProduct());
            dto.setSerialNo(item.getSerialNo());
            dto.setModel(item.getModel());
            dto.setFaultDescription(item.getFaultDescription());
            dto.setRepairStatus(item.getRepairStatus());
            dto.setAssignedToEmail(item.getAssignedToEmail());
            dto.setAssignedToName(item.getAssignedToName());
            dto.setAssignedDate(item.getAssignedDate());
            dto.setRepairedByEmail(item.getRepairedByEmail());
            dto.setRepairedByName(item.getRepairedByName());
            dto.setRepairedDate(item.getRepairedDate());

            dto.setRepairRemarks(item.getRepairRemarks());
            // Get Request Number from parent request (for display)
            // Note: rmaNo is for actual RMA number assigned later, requestNumber is
            // auto-generated
            if (item.getRmaRequest() != null) {
                String reqNo = item.getRmaRequest().getRequestNumber();
                dto.setRmaNo(reqNo != null ? reqNo : item.getRmaNo()); // Fallback to item rmaNo if request number is
                                                                       // null

                // Set customer and date info for FRU sticker display
                dto.setCompanyName(item.getRmaRequest().getCompanyName());
                dto.setReceivedDate(item.getRmaRequest().getCreatedDate());
                dto.setRepairType(item.getRmaRequest().getRepairType());
            } else {
                // Fallback for items with missing parent request (Legacy data)
                dto.setRmaNo(item.getRmaNo());
            }

            // Set the item-level RMA number (distinct from parent request number)
            dto.setItemRmaNo(item.getRmaNo());
            dto.setIssueFixed(item.getIssueFixed());
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Get product catalog from KeywordEntity for RMA form dropdown
     */
    public ResponseEntity<?> getProductCatalog() {
        try {
            List<String> keywords = keywordDAO.getKeywordList();
            List<ProductCatalogDTO> catalog = new ArrayList<>();

            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    catalog.add(new ProductCatalogDTO(keyword, "", ""));
                }
            }

            // Add a default "Other" option if not already present
            boolean hasOther = catalog.stream().anyMatch(p -> "Other".equalsIgnoreCase(p.getName()));
            if (!hasOther) {
                catalog.add(new ProductCatalogDTO("Other", "", ""));
            }

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch product catalog: " + e.getMessage());
        }
    }

    /**
     * Helper method to extract client IP address from request
     * Handles proxied requests by checking X-Forwarded-For header
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Bulk assign all unassigned items with a given RMA number to a technician
     */
    @Transactional
    public ResponseEntity<?> bulkAssignByRmaNo(HttpServletRequest request, String rmaNo, String assigneeEmail,
            String assigneeName) {
        try {
            // 1. Authentication Check
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

            // 2. Input Validation
            if (rmaNo == null || rmaNo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RMA number is required");
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

            // Sanitize inputs
            assigneeName = assigneeName.replaceAll("<[^>]*>", "").trim();
            if (assigneeName.length() > 100) {
                assigneeName = assigneeName.substring(0, 100);
            }

            // 3. Find all unassigned items with this RMA number
            List<RmaItemEntity> unassignedItems = rmaItemDAO.findUnassignedByRmaNo(rmaNo);

            if (unassignedItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No unassigned items found for RMA: " + rmaNo);
            }

            // 4. Assign all items
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

                // Create Audit Log Entry for each item
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

    /**
     * Get all audit logs for viewing the audit trail
     */
    public ResponseEntity<?> getAuditLogs() {
        try {
            List<RmaAuditLogEntity> logs = rmaAuditLogDAO.findAllOrderByPerformedAtDesc();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch audit logs");
        }
    }

    /**
     * Get audit logs for a specific RMA item
     */
    public ResponseEntity<?> getAuditLogsByItemId(Long itemId) {
        try {
            List<RmaAuditLogEntity> logs = rmaAuditLogDAO.findByRmaItemId(itemId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch audit logs for item: " + itemId);
        }
    }
}
