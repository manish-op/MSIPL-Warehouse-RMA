package com.serverManagement.server.management.service.rma.request;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.common.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.request.RmaRequestDAO;
import com.serverManagement.server.management.response.rma.RmaItemsGroupedResponse;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.request.RmaRequestEntity;
import com.serverManagement.server.management.request.rma.CreateRmaRequest;
import com.serverManagement.server.management.request.rma.RmaItemRequest;
import com.serverManagement.server.management.response.rma.RmaResponse;
import com.serverManagement.server.management.response.rma.RmaResponse.RmaItemResponse;
import com.serverManagement.server.management.service.rma.common.CustomerService;
import com.serverManagement.server.management.service.rma.common.RmaModelMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RmaRequestService {

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RmaModelMapper rmaModelMapper;

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

        // Validate each item (Serial No is now optional for accessories without serial
        // numbers)
        for (int i = 0; i < createRmaRequest.getItems().size(); i++) {
            RmaItemRequest item = createRmaRequest.getItems().get(i);
            if (isBlank(item.getProduct())
                    || isBlank(item.getFaultDescription()) || isBlank(item.getFmUlatex())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item " + (i + 1)
                                + ": Product, Fault Description, and FM/UL/ATEX are mandatory fields");
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

        // Normalize repair type
        String rawRepairType = createRmaRequest.getRepairType();
        String normalizedRepairType = "LOCAL"; // Default
        if (rawRepairType != null) {
            if (rawRepairType.toUpperCase().contains("DEPOT")) {
                normalizedRepairType = "DEPOT";
            } else if (rawRepairType.toUpperCase().contains("LOCAL")) {
                normalizedRepairType = "LOCAL";
            } else {
                normalizedRepairType = rawRepairType.toUpperCase(); // Fallback
            }
        }

        rmaRequestEntity.setRepairType(normalizedRepairType);

        // Set audit fields
        ZonedDateTime now = ZonedDateTime.now();
        rmaRequestEntity.setCreatedByEmail(loggedInUserEmail);
        rmaRequestEntity.setCreatedDate(now);
        rmaRequestEntity.setUpdatedDate(now);

        // Generate unique request number (auto-generated when request is submitted)

        // RMA number will be set later by service team after approval
        // Do not auto-generate RMA number for items
        rmaRequestEntity.setRmaNo(null);

        // Set TAT and calculate due date
        if (createRmaRequest.getTat() != null && createRmaRequest.getTat() > 0) {
            rmaRequestEntity.setTat(createRmaRequest.getTat());
            rmaRequestEntity.setDueDate(now.plusDays(createRmaRequest.getTat()));
        }

        // 4a. Auto-save customer details (find existing or create new)
        try {
            // Save return address customer with TAT
            var returnCustomer = customerService.findOrCreateCustomer(
                    createRmaRequest.getCompanyName(),
                    createRmaRequest.getContactName(),
                    createRmaRequest.getEmail(),
                    createRmaRequest.getTelephone(),
                    createRmaRequest.getMobile(),
                    createRmaRequest.getReturnAddress(),
                    createRmaRequest.getTat());
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
                        createRmaRequest.getInvoiceAddress(),
                        null); // Invoice customer doesn't use TAT
                rmaRequestEntity.setInvoiceCustomer(invoiceCustomer);
            }
        } catch (Exception e) {
            // Log but don't fail - customer save is supplementary
            System.err.println("Warning: Failed to auto-save customer: " + e.getMessage());
        }

        // 5. Create RMA Item Entities
        List<RmaItemEntity> rmaItemEntities = new ArrayList<>();
        List<RmaItemResponse> itemResponses = new ArrayList<>();

        for (int i = 0; i < createRmaRequest.getItems().size(); i++) {
            RmaItemRequest itemRequest = createRmaRequest.getItems().get(i);

            RmaItemEntity itemEntity = new RmaItemEntity();

            // Map item fields
            itemEntity.setProduct(itemRequest.getProduct());
            itemEntity.setModel(itemRequest.getModel());
            // Auto-fill "N/A" for accessories without serial numbers with a unique internal
            // ID
            String serialNo = itemRequest.getSerialNo();
            if (serialNo == null || serialNo.trim().isEmpty() || "NA".equalsIgnoreCase(serialNo.trim())
                    || "N/A".equalsIgnoreCase(serialNo.trim())) {
                // Generate Unique ID: NA-Timestamp-Random
                String uniqueId = "NA-" + System.currentTimeMillis() + "-"
                        + UUID.randomUUID().toString().substring(0, 8);
                itemEntity.setSerialNo(uniqueId);
            } else {
                itemEntity.setSerialNo(serialNo.trim());
            }
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
            itemEntity.setRmaNo(null);

            // Set relationship
            itemEntity.setRmaRequest(rmaRequestEntity);

            // -------------DEPOT VS LOCAL STAGE LOGIC-------------
            itemEntity.setRepairType(normalizedRepairType);

            if ("DEPOT".equals(normalizedRepairType)) {
                // First for depot items:waiting to be dispatched
                itemEntity.setDepotStage("PENDING_DISPATCH_TO_DEPOT");
                itemEntity.setLocalStage(null);
            } else {
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

        // 6. Save to Database to generate ID
        // Set temporary request number to satisfy NOT NULL constraint
        rmaRequestEntity.setRequestNumber("RMA-" + java.util.UUID.randomUUID().toString());
        RmaRequestEntity savedEntity = rmaRequestDAO.save(rmaRequestEntity);

        // 6a. Update request number with ID (RMA-1, RMA-2, etc.)
        String requestNumber = "RMA-" + savedEntity.getId();
        savedEntity.setRequestNumber(requestNumber);
        rmaRequestDAO.save(savedEntity);

        // Audit Log: Record creation of items
        String loggedInUserName = adminUserEntity.getName();
        if (savedEntity.getItems() != null) {
            for (RmaItemEntity item : savedEntity.getItems()) {
                RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
                auditLog.setRmaItemId(item.getId());
                auditLog.setRmaNo(requestNumber); // Use the new ID-based number
                auditLog.setAction("CREATED");
                auditLog.setOldValue("None");
                auditLog.setNewValue("Created via RMA Form");
                auditLog.setPerformedByEmail(loggedInUserEmail);
                auditLog.setPerformedByName(loggedInUserName);
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setRemarks("RMA Request generated by " + loggedInUserName);
                rmaAuditLogDAO.save(auditLog);
            }
        }

        // 7. Create Response
        RmaResponse response = new RmaResponse();
        response.setRmaRequestId(savedEntity.getId());
        response.setRmaNo(requestNumber);
        response.setMessage("RMA Request submitted successfully");
        response.setTimestamp(now);
        response.setItems(itemResponses);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Utility method to check if string is blank
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllRmaItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findAll();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching all items: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllRmaRequests(String timeFilter) {
        try {
            List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();

            if (timeFilter == null || timeFilter.equalsIgnoreCase("all")) {
                return ResponseEntity.ok(allRequests);
            }

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime threshold = now;

            if (timeFilter.equalsIgnoreCase("week")) {
                threshold = now.minusWeeks(1);
            } else if (timeFilter.equalsIgnoreCase("month")) {
                threshold = now.minusMonths(1);
            } else if (timeFilter.equalsIgnoreCase("year")) {
                threshold = now.minusYears(1);
            } else if (timeFilter.equalsIgnoreCase("today")) {
                // Filter for today: from 00:00:00 today
                threshold = now.toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault());
            }

            final ZonedDateTime finalThreshold = threshold;
            List<RmaRequestEntity> filtered = allRequests.stream()
                    .filter(req -> {
                        if (req.getCreatedDate() == null)
                            return false;
                        return req.getCreatedDate().isAfter(finalThreshold);
                    })
                    .toList();

            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching requests: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
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
                        String displaySerial = item.getSerialNo();
                        if (displaySerial != null && displaySerial.startsWith("NA-")) {
                            displaySerial = "N/A";
                        }

                        RmaItemsGroupedResponse.RmaItemDTO itemDTO = new RmaItemsGroupedResponse.RmaItemDTO(
                                item.getId(),
                                item.getProduct(),
                                displaySerial,
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
     * Get all RMA items
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findAll();
            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(items));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch all items: " + e.getMessage());
        }
    }

    /**
     * Search items by query (product or serial no)
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchItems(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Search query cannot be empty");
            }

            // Optimized database-level search
            List<RmaItemEntity> filteredItems = rmaItemDAO.searchItems(query.trim());

            return ResponseEntity.ok(rmaModelMapper.convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search items: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getSerialHistory(String serialNo) {
        try {
            if (serialNo == null || serialNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Serial number cannot be empty");
            }

            List<RmaItemEntity> items = rmaItemDAO.findBySerialNoIgnoreCaseOrderByIdDesc(serialNo.trim());

            List<Map<String, Object>> history = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

            for (RmaItemEntity item : items) {
                Map<String, Object> h = new HashMap<>();
                RmaRequestEntity req = item.getRmaRequest();

                if (req == null) {
                    // Handle items without parent request
                    String rmaNum = item.getRmaNo();
                    h.put("rmaNo", rmaNum != null ? rmaNum : "N/A");
                    h.put("createdDate", "N/A");
                    h.put("customerName", "N/A");
                } else {
                    String rmaNum = req.getRmaNo() != null ? req.getRmaNo() : req.getRequestNumber();
                    if (rmaNum != null && rmaNum.toUpperCase().startsWith("RMA")) {
                        h.put("rmaNo", rmaNum);
                    } else {
                        h.put("rmaNo", "RMA " + rmaNum);
                    }
                    h.put("createdDate", req.getCreatedDate() != null ? req.getCreatedDate().format(formatter) : "N/A");
                    h.put("customerName", req.getCompanyName());
                }

                h.put("itemId", item.getId());
                h.put("currentStatus", item.getRmaStatus());
                h.put("repairStatus", item.getRepairStatus());
                h.put("depotStage", item.getDepotStage() != null ? item.getDepotStage() : item.getLocalStage());
                h.put("product", item.getProduct());
                h.put("model", item.getModel());
                h.put("serialNo", item.getSerialNo());
                h.put("repairedDate", item.getRepairedDate() != null ? item.getRepairedDate().format(formatter) : null);
                h.put("faultDescription", item.getFaultDescription());
                h.put("issueFixed", item.getIssueFixed() != null ? item.getIssueFixed() : "N/A");

                // Logic for technician names with email fallback
                String assignedName = item.getAssignedToName();
                String assignedEmail = item.getAssignedToEmail();

                if (isBlank(assignedName)) {
                    assignedName = assignedEmail;
                }
                h.put("assignedTechnician", !isBlank(assignedName) ? assignedName : "N/A");

                String repairedName = item.getRepairedByName();
                String repairedEmail = item.getRepairedByEmail();

                if (isBlank(repairedName)) {
                    repairedName = repairedEmail;
                }
                h.put("repairedBy", !isBlank(repairedName) ? repairedName : "N/A");

                // Fallback for remarks
                String repairs = item.getRepairRemarks();
                if (isBlank(repairs)) {
                    repairs = item.getRemarks(); // Fallback to general remarks
                }
                h.put("repairRemarks", !isBlank(repairs) ? repairs : "N/A");

                history.add(h);
            }

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching serial history: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getUniqueCourierCompanies() {
        try {
            List<String> companies = rmaRequestDAO.findDistinctCourierCompanyNames();
            // Ensure Blue Dart and Safe Express are always in the list
            if (!companies.contains("Blue Dart"))
                companies.add("Blue Dart");
            if (!companies.contains("Safe Express"))
                companies.add("Safe Express");
            // Remove duplicates and sort
            List<String> sortedCompanies = companies.stream().distinct().sorted().collect(Collectors.toList());
            return ResponseEntity.ok(sortedCompanies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch courier companies");
        }
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
