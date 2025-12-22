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
import com.serverManagement.server.management.dao.rma.DepotDispatchDAO;
import com.serverManagement.server.management.dao.rma.ProductValueDAO;
import com.serverManagement.server.management.dao.rma.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.RmaRequestDAO;
import com.serverManagement.server.management.dao.rma.TransporterDAO;
import com.serverManagement.server.management.dto.rma.DeliveryChallanRequest;
import com.serverManagement.server.management.dto.rma.ProductCatalogDTO;
import com.serverManagement.server.management.dto.rma.ProductModelDTO;
import com.serverManagement.server.management.dto.rma.RmaDashboardStatsDto;
import com.serverManagement.server.management.dto.rma.RmaItemWorkflowDTO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.DepotDispatchEntity;
import com.serverManagement.server.management.entity.rma.ProductValueEntity;
import com.serverManagement.server.management.entity.rma.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.RmaRequestEntity;
import com.serverManagement.server.management.entity.rma.TransporterEntity;
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
    private ProductValueDAO productValueDAO;

    @Autowired
    private TransporterDAO transporterDAO;

    @Autowired
    private DepotDispatchDAO depotDispatchDAO;

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
        String requestNumber = generateRmaNumber(); // REQ-YYYYMMDD-HHMMSS
        rmaRequestEntity.setRequestNumber(requestNumber);

        // RMA number will be set later by service team after approval
        // Do not auto-generate RMA number for items
        rmaRequestEntity.setRmaNo(null);

        // Set TAT and calculate due date
        if (createRmaRequest.getTat() != null && createRmaRequest.getTat() > 0) {
            rmaRequestEntity.setTat(createRmaRequest.getTat());
            rmaRequestEntity.setDueDate(now.plusDays(createRmaRequest.getTat()));
        }

        // 4a. Auto-save customer details (find existing or create new)
        // This allows customers to be reused in future RMA requests
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
                // This prevents unique constraint violations or overwrites while keeping "N/A"
                // semantic
                String uniqueId = "NA-" + System.currentTimeMillis() + "-"
                        + java.util.UUID.randomUUID().toString().substring(0, 8);
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
            // Set to null for new records (deprecated field kept for backward
            // compatibility)
            itemEntity.setRmaNo(null);

            // Set relationship
            itemEntity.setRmaRequest(rmaRequestEntity);

            // -------------DEPOT VS LOCAL STAGE LOGIC-------------
            // Use the normalized repair type from the request
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

        // 6. Save to Database (Cascade will save items too)
        RmaRequestEntity savedEntity = rmaRequestDAO.save(rmaRequestEntity);

        // Audit Log: Record creation of items
        String loggedInUserName = adminUserEntity.getName();
        if (savedEntity.getItems() != null) {
            for (RmaItemEntity item : savedEntity.getItems()) {
                RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
                auditLog.setRmaItemId(item.getId());
                auditLog.setRmaNo(requestNumber); // Use request number as RMA No initially
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
     * Get RMA dashboard statistics
     * Returns counts of total requests, total items, repaired, and unrepaired items
     * Also returns daily trend for the last 7 days and recent RMA numbers
     */
    public ResponseEntity<?> getRmaDashboardStats() {
        try {
            long totalRequests = rmaRequestDAO.count();
            long totalItems = rmaItemDAO.count();
            long repairedCount = rmaItemDAO.countRepaired();
            long unrepairedCount = rmaItemDAO.countUnrepaired();

            // Calculate Daily Trends for the last 7 days
            List<com.serverManagement.server.management.dto.rma.DailyTrendDto> dailyTrends = new ArrayList<>();
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime sevenDaysAgo = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);

            // Get requests for the last 7 days to minimize memory usage
            List<RmaRequestEntity> recentRequests = rmaRequestDAO.findByCreatedDateBetween(sevenDaysAgo, now);

            // Create a map for quick lookups
            // Group by Date (YYYY-MM-DD)
            java.util.Map<java.time.LocalDate, Long> requestsByDate = recentRequests.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            req -> req.getCreatedDate().withZoneSameInstant(now.getZone()).toLocalDate(),
                            java.util.stream.Collectors.counting()));

            // Iterate last 7 days to ensure all days are present even if 0 requests
            java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE");

            for (int i = 6; i >= 0; i--) {
                ZonedDateTime date = now.minusDays(i);
                java.time.LocalDate localDate = date.toLocalDate();

                String dayName = date.format(dayFormatter); // Mon, Tue, etc.
                long count = requestsByDate.getOrDefault(localDate, 0L);

                dailyTrends.add(new com.serverManagement.server.management.dto.rma.DailyTrendDto(dayName, count));
            }

            // Fetch recent RMA numbers from rma_item table (non-null, limited to 10)
            List<String> recentRmaNumbers = new ArrayList<>();
            try {
                List<RmaItemEntity> allItems = rmaItemDAO.findAll();
                recentRmaNumbers = allItems.stream()
                        .filter(item -> item.getRmaNo() != null && !item.getRmaNo().trim().isEmpty()
                                && !item.getRmaNo().startsWith("RMA-") // Exclude auto-generated format
                                && !"Unknown".equalsIgnoreCase(item.getRmaNo().trim()))
                        .map(RmaItemEntity::getRmaNo)
                        .distinct()
                        .limit(10)
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                // If failed to fetch, just return empty list
                e.printStackTrace();
            }

            RmaDashboardStatsDto stats = new RmaDashboardStatsDto(
                    totalRequests,
                    totalItems,
                    repairedCount,
                    unrepairedCount,
                    dailyTrends,
                    recentRmaNumbers);

            // Calculate SLA compliance stats
            try {
                List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();
                long totalWithTat = 0;
                long onTrack = 0;
                long atRisk = 0;
                long breached = 0;

                for (RmaRequestEntity req : allRequests) {
                    if (req.getTat() != null && req.getDueDate() != null) {
                        totalWithTat++;
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, req.getDueDate());
                        int halfTat = req.getTat() / 2;

                        if (daysRemaining < 0) {
                            breached++;
                        } else if (daysRemaining <= halfTat) {
                            atRisk++;
                        } else {
                            onTrack++;
                        }
                    }
                }

                stats.setTotalWithTat(totalWithTat);
                stats.setOnTrackCount(onTrack);
                stats.setAtRiskCount(atRisk);
                stats.setBreachedCount(breached);

                // Calculate compliance rate (closed within TAT)
                // For now, use (onTrack + atRisk) / totalWithTat as a simple metric
                if (totalWithTat > 0) {
                    double complianceRate = (double) (totalWithTat - breached) / totalWithTat * 100;
                    stats.setComplianceRate(Math.round(complianceRate * 10.0) / 10.0); // Round to 1 decimal
                } else {
                    stats.setComplianceRate(100.0); // No TAT defined = 100% compliant
                }
            } catch (Exception e) {
                // SLA calculation failed, set defaults
                stats.setTotalWithTat(0L);
                stats.setOnTrackCount(0L);
                stats.setAtRiskCount(0L);
                stats.setBreachedCount(0L);
                stats.setComplianceRate(100.0);
                System.err.println("Warning: Failed to calculate SLA stats: " + e.getMessage());
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch RMA statistics: " + e.getMessage());
        }
    }

    // ============ WORKFLOW METHODS ============

    /**
     * Helper method to check if user is Admin
     */
    private boolean isAdmin(AdminUserEntity user) {
        return user != null && user.getRoleModel() != null
                && "admin".equalsIgnoreCase(user.getRoleModel().getRoleName());
    }

    /**
     * Helper method to check if user is from Bangalore region
     */
    private boolean isBangaloreUser(AdminUserEntity user) {
        if (user == null || user.getRegionEntity() == null) {
            return false;
        }
        String city = user.getRegionEntity().getCity();
        return city != null && city.toLowerCase().contains("bangalore");
    }

    /**
     * Helper method to get logged-in user from request
     * Note: Forces initialization of lazy-loaded roleModel and regionEntity
     */
    private AdminUserEntity getLoggedInUser(HttpServletRequest request) {
        try {
            String email = request.getUserPrincipal().getName();
            AdminUserEntity user = adminUserDAO.findByEmail(email.toLowerCase());
            if (user != null) {
                // Force initialize lazy-loaded entities within transaction
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

    /**
     * Helper method to filter items based on user role, region, and repair type
     * - DEPOT items: Only visible to Bangalore users or Admin
     * - LOCAL items: Visible based on assigned_to_email or region
     */
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
                    // Check DEPOT repair type - only Bangalore users or Admin can see
                    if ("DEPOT".equalsIgnoreCase(item.getRepairType())) {
                        if (!isUserAdmin && !isUserFromBangalore) {
                            return false; // Non-Bangalore users cannot see DEPOT items
                        }
                    }

                    // If Admin, allow all (already passed DEPOT check above)
                    if (isUserAdmin) {
                        return true;
                    }

                    // For non-Admin: filter by assigned_to_email if required
                    if (filterByAssignee) {
                        return userEmail.equalsIgnoreCase(item.getAssignedToEmail());
                    }

                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get all unassigned RMA items (items without an assignee)
     * RBAC: Admins see all, technicians see only their region's items
     * DEPOT items: Only visible to Bangalore users or Admin
     */
    public ResponseEntity<?> getUnassignedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findUnassignedItems();

            // If Admin, return all items
            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(convertToItemDTOList(items));
            }

            // For non-Admin: filter by region and repair type
            if (loggedInUser == null || loggedInUser.getRegionEntity() == null) {
                return ResponseEntity.ok(convertToItemDTOList(items)); // Fallback to all if no region info
            }

            Long userRegionId = loggedInUser.getRegionEntity().getId();
            boolean isUserFromBangalore = isBangaloreUser(loggedInUser);

            // Filter items where the RMA request creator is from the same region
            // Also filter DEPOT items for non-Bangalore users
            List<RmaItemEntity> filteredItems = items.stream()
                    .filter(item -> {
                        // DEPOT items: only visible to Bangalore users
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
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch unassigned items: " + e.getMessage());
        }
    }

    /**
     * Get all RMA items
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllItems() {
        try {
            List<RmaItemEntity> items = rmaItemDAO.findAll();
            return ResponseEntity.ok(convertToItemDTOList(items));
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

            // Fetch all items and filter in memory to avoid DB query issues
            List<RmaItemEntity> allItems = rmaItemDAO.findAll();

            String lowerQuery = query.toLowerCase().trim();

            List<RmaItemEntity> filteredItems = allItems.stream()
                    .filter(item -> (item.getProduct() != null && item.getProduct().toLowerCase().contains(lowerQuery))
                            ||
                            (item.getSerialNo() != null && item.getSerialNo().equalsIgnoreCase(lowerQuery)))
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search items: " + e.getMessage());
        }
    }

    /**
     * Get all assigned RMA items (items with assignee, not yet completed)
     * RBAC: Admins see all, technicians see only items assigned to them
     * DEPOT items: Only visible to Bangalore users or Admin
     */
    public ResponseEntity<?> getAssignedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findAssignedItems();

            // If Admin, return all items
            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(convertToItemDTOList(items));
            }

            // For non-Admin: filter by assigned_to_email and DEPOT region check
            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>()); // Return empty if not authenticated
            }

            // Use the helper method that handles both DEPOT region check and assignee
            // filter
            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);

            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch assigned items: " + e.getMessage());
        }
    }

    /**
     * Get all repaired RMA items
     * RBAC: Admins see all, technicians see only items they repaired
     * DEPOT items: Only visible to Bangalore users or Admin
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRepairedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findRepairedItems();

            // If Admin, return all items
            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(convertToItemDTOList(items));
            }

            // For non-Admin: filter by assigned_to_email and DEPOT region check
            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch repaired items: " + e.getMessage());
        }
    }

    /**
     * Get all items that can't be repaired
     * RBAC: Admins see all, technicians see only items they worked on
     * DEPOT items: Only visible to Bangalore users or Admin
     */
    public ResponseEntity<?> getCantBeRepairedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findCantBeRepairedItems();

            // If Admin, return all items
            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(convertToItemDTOList(items));
            }

            // For non-Admin: filter with DEPOT region check and assignee filter
            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch cant be repaired items: " + e.getMessage());
        }
    }

    /**
     * Get all dispatched items (is_dispatched = true)
     * RBAC: Admins see all, technicians see only items they worked on
     * DEPOT items: Only visible to Bangalore users or Admin
     */
    public ResponseEntity<?> getDispatchedItems(HttpServletRequest request) {
        try {
            AdminUserEntity loggedInUser = getLoggedInUser(request);
            List<RmaItemEntity> items = rmaItemDAO.findByIsDispatched(true);

            // If Admin, return all items
            if (isAdmin(loggedInUser)) {
                return ResponseEntity.ok(convertToItemDTOList(items));
            }

            // For non-Admin: filter with DEPOT region check and assignee filter
            if (loggedInUser == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<RmaItemEntity> filteredItems = filterItemsByAccess(items, loggedInUser, true);
            return ResponseEntity.ok(convertToItemDTOList(filteredItems));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch dispatched items: " + e.getMessage());
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

            // Security: Role-based assignment check
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

            // Check if RMA Number is present either on item or on request
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
            item.setLastReassignmentReason(null); // Clear previous reassignment reason
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
     * Reassign an RMA item to a different technician with reason
     * Security: Requires authentication, validates inputs, logs action with reason
     */
    @Transactional
    public ResponseEntity<?> reassignItem(HttpServletRequest request, Long itemId, String newAssigneeEmail,
            String newAssigneeName, String reason) {
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

            // Sanitize inputs
            newAssigneeName = newAssigneeName.replaceAll("<[^>]*>", "").trim();
            if (newAssigneeName.length() > 100) {
                newAssigneeName = newAssigneeName.substring(0, 100);
            }
            reason = reason.replaceAll("<[^>]*>", "").trim();
            if (reason.length() > 500) {
                reason = reason.substring(0, 500);
            }

            // 3. Find and update item
            RmaItemEntity item = rmaItemDAO.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            String oldAssigneeEmail = item.getAssignedToEmail();
            String oldAssigneeName = item.getAssignedToName();

            // Check if item is actually assigned
            if (oldAssigneeEmail == null || oldAssigneeEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item is not currently assigned. Use assign instead of reassign.");
            }

            // Update assignee
            item.setAssignedToEmail(newAssigneeEmail.toLowerCase().trim());
            item.setAssignedToName(newAssigneeName);
            item.setAssignedDate(ZonedDateTime.now());
            item.setLastReassignmentReason(reason); // Save reassignment reason
            // Keep status as is (could be ASSIGNED, REPAIRING, etc.)

            rmaItemDAO.save(item);

            // 4. Create Audit Log Entry with reason
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

            // Validate status using Enum
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
                                java.util.Arrays
                                        .toString(com.serverManagement.server.management.enums.RepairStatus.values()));
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

            // Note: DISPATCHED status is handled by separate dispatch endpoints
            // (dispatch-to-customer, dispatch-to-bangalore)

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
     * Confirm delivery of dispatched items
     */
    public ResponseEntity<?> confirmDelivery(HttpServletRequest request, List<Long> itemIds,
            String deliveredTo, String deliveredBy, String deliveryNotes) {
        try {
            // Authentication
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
                // Only confirm items that are dispatched
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

                    // Create audit log
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
     * Helper method to convert entity list to DTO list
     */
    private List<RmaItemWorkflowDTO> convertToItemDTOList(List<RmaItemEntity> items) {
        List<RmaItemWorkflowDTO> dtoList = new ArrayList<>();
        for (RmaItemEntity item : items) {
            RmaItemWorkflowDTO dto = new RmaItemWorkflowDTO();
            dto.setId(item.getId());
            dto.setProduct(item.getProduct());

            // Mask internal unique IDs for display
            String displaySerial = item.getSerialNo();
            if (displaySerial != null && displaySerial.startsWith("NA-")) {
                displaySerial = "N/A";
            }
            dto.setSerialNo(displaySerial);

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
            dto.setLastReassignmentReason(item.getLastReassignmentReason()); // Populate DTO field
            dto.setIsDispatched(item.getIsDispatched()); // Dispatch status for filtering

            // Dispatch tracking fields
            dto.setDispatchTo(item.getDispatchTo());
            dto.setDispatchedDate(item.getDispatchedDate());
            dto.setDispatchedByEmail(item.getDispatchedByEmail());
            dto.setDispatchedByName(item.getDispatchedByName());
            dto.setDcNo(item.getDcNo());
            dto.setEwayBillNo(item.getEwayBillNo());

            // Depot return dates
            dto.setDepotReturnDispatchDate(item.getDepotReturnDispatchDate());
            dto.setDepotReturnDeliveredDate(item.getDepotReturnDeliveredDate());

            // Delivery confirmation fields
            dto.setDeliveredTo(item.getDeliveredTo());
            dto.setDeliveredBy(item.getDeliveredBy());
            dto.setDeliveryDate(item.getDeliveryDate());
            dto.setDeliveryNotes(item.getDeliveryNotes());
            dto.setIsDelivered(item.getDeliveredTo() != null); // Mark as delivered if deliveredTo is set

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

            // 2. Input Validation
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

            // Security: Role-based assignment check
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

    // Save DC Details (Transporter, etc)
    @Transactional
    public void saveDcDetails(DeliveryChallanRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty())
            return;

        // Collect Item IDs
        List<Long> itemIds = request.getItems().stream()
                .map(DeliveryChallanRequest.DcItemDto::getItemId)
                .filter(id -> id != null)
                .toList();

        if (itemIds.isEmpty())
            return;

        List<RmaItemEntity> rmaItems = rmaItemDAO.findAllById(itemIds);
        if (rmaItems.isEmpty())
            return;

        // Use existing dispatch from first item if available, or create new
        DepotDispatchEntity dispatch = rmaItems.get(0).getDepotDispatch();
        if (dispatch == null) {
            dispatch = new DepotDispatchEntity();
            dispatch.setCreatedAt(ZonedDateTime.now());
        }

        // Update fields
        // Note: dcNo usually comes from request? Request has rmaNo, but maybe not dcNo?
        // DeliveryChallanRequest has rmaNo (DC No usually same or generated).
        // We will assume rmaNo in request IS the DC number for now or update if needed.
        // Actually DC PDF uses "MSIPL/2025/110" etc hardcoded in PDF service? No, PDF
        if (request.getTransporterId() != null && !request.getTransporterId().isEmpty()) {

            java.util.Optional<TransporterEntity> transParams = java.util.Optional.empty();
            // Try fetching by transporterId (String) if unique? DAO doesn't have it yet.
            // fetching by Name is safer if we have it.
            if (request.getTransporterName() != null) {
                transParams = transporterDAO.findByName(request.getTransporterName());
            }

            if (transParams.isPresent()) {
                dispatch.setTransporter(transParams.get());
            } else if (request.getTransporterName() != null && request.getTransporterId() != null) {
                // Create new Transporter if name provided but not found, and ID is available
                TransporterEntity newTransporter = new TransporterEntity();
                newTransporter.setName(request.getTransporterName());
                newTransporter.setTransporterId(request.getTransporterId()); // The business ID entered by user
                newTransporter = transporterDAO.save(newTransporter);
                dispatch.setTransporter(newTransporter);
            }
            // Fallback: If not found, maybe just save the strings?
        }

        dispatch.setCourierName(request.getTransporterName()); // Legacy field

        // FIX: Save DC Number so auto-increment works
        System.out.println("DEBUG: RmaService.saveDcDetails called. DC No from Request: " + request.getDcNo());
        if (request.getDcNo() != null && !request.getDcNo().isEmpty()) {
            dispatch.setDcNo(request.getDcNo());
            dispatch.setDispatchDate(ZonedDateTime.now()); // FIX: Update date so it appears as latest
            System.out.println("DEBUG: Setting DC No to Entity: " + request.getDcNo());
        } else {
            System.out.println("DEBUG: DC No in request is NULL or EMPTY");
        }

        // Save dispatch
        dispatch = depotDispatchDAO.save(dispatch);
        System.out.println("DEBUG: Saved Dispatch. ID: " + dispatch.getId() + ", DC No: " + dispatch.getDcNo());

        // IMMEDIATE VERIFICATION
        DepotDispatchEntity verified = depotDispatchDAO.findById(dispatch.getId()).orElse(null);
        if (verified != null) {
            System.out.println("DEBUG: Verified from DB - ID: " + verified.getId() + ", DC No: " + verified.getDcNo());
        }

        // Link items
        for (RmaItemEntity item : rmaItems) {
            item.setDepotDispatch(dispatch);
        }
        rmaItemDAO.saveAll(rmaItems);

        // Save Product Rates / Values for future auto-fill
        if (request.getItems() != null) {
            for (DeliveryChallanRequest.DcItemDto itemDto : request.getItems()) {
                try {
                    String rate = itemDto.getRate();
                    String product = itemDto.getProduct();
                    String model = itemDto.getModel();

                    if (rate != null && !rate.trim().isEmpty() && product != null && !product.trim().isEmpty()) {
                        // Normalize
                        product = product.trim();
                        model = (model != null) ? model.trim() : "";
                        rate = rate.trim();

                        // Check if exists
                        java.util.Optional<com.serverManagement.server.management.entity.rma.ProductValueEntity> startVal = productValueDAO
                                .findByProductAndModel(product, model);

                        com.serverManagement.server.management.entity.rma.ProductValueEntity valEntity;
                        if (startVal.isPresent()) {
                            valEntity = startVal.get();
                            valEntity.setValue(rate); // update with new rate
                            valEntity.setLastUpdated(ZonedDateTime.now());
                        } else {
                            valEntity = new com.serverManagement.server.management.entity.rma.ProductValueEntity();
                            valEntity.setProduct(product);
                            valEntity.setModel(model);
                            valEntity.setValue(rate);
                            valEntity.setLastUpdated(ZonedDateTime.now());
                        }
                        productValueDAO.save(valEntity);
                    }
                } catch (Exception e) {
                    // Log but don't fail the transaction just for rate saving
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get saved rates for a list of products/models
     */
    public ResponseEntity<?> getProductRates(List<ProductModelDTO> items) {
        try {
            java.util.Map<String, String> rates = new java.util.HashMap<>();
            // Optimize: find all by (product, model) - but JPA might not support "IN"
            // tuples easily without custom query.
            // For now, iterate. If list is small (DC items usually < 20), it's fine.
            // Or we could fetch ALL values for these products?

            for (ProductModelDTO item : items) {
                if (item.getProduct() == null)
                    continue;
                String model = item.getModel() == null ? "" : item.getModel().trim();
                String product = item.getProduct().trim();

                String key = product + "::" + model;
                ProductValueEntity entity = productValueDAO.findByProductAndModel(product, model).orElse(null);
                if (entity != null) {
                    rates.put(key, entity.getValue());
                }
            }
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch product rates");
        }
    }

    /**
     * Get TAT Compliance Report - customer-wise breakdown
     * Shows requests with TAT, completed within/after TAT, and compliance rate per
     * customer
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTatComplianceReport() {
        try {
            List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();
            ZonedDateTime now = ZonedDateTime.now();

            // Group requests by company name
            java.util.Map<String, java.util.List<RmaRequestEntity>> requestsByCompany = allRequests.stream()
                    .collect(java.util.stream.Collectors.groupingBy(RmaRequestEntity::getCompanyName));

            List<com.serverManagement.server.management.dto.rma.TatComplianceReportDto> report = new java.util.ArrayList<>();

            for (java.util.Map.Entry<String, java.util.List<RmaRequestEntity>> entry : requestsByCompany.entrySet()) {
                String companyName = entry.getKey();
                java.util.List<RmaRequestEntity> companyRequests = entry.getValue();

                com.serverManagement.server.management.dto.rma.TatComplianceReportDto dto = new com.serverManagement.server.management.dto.rma.TatComplianceReportDto();
                dto.setCompanyName(companyName);
                dto.setTotalRequests((long) companyRequests.size());

                // Get default TAT from customer if available
                RmaRequestEntity firstReq = companyRequests.get(0);
                if (firstReq.getCustomer() != null && firstReq.getCustomer().getTat() != null) {
                    dto.setDefaultTat(firstReq.getCustomer().getTat());
                }

                long requestsWithTat = 0;
                long completedWithinTat = 0;
                long completedAfterTat = 0;
                long stillOpen = 0;
                long onTrack = 0;
                long atRisk = 0;
                long breached = 0;
                ZonedDateTime oldestOpenDueDate = null;

                for (RmaRequestEntity req : companyRequests) {
                    if (req.getTat() == null || req.getDueDate() == null) {
                        continue;
                    }
                    requestsWithTat++;

                    // Check if all items in request are delivered (completed)
                    // For Local repair: DELIVERED = cycle complete
                    // For Depot repair: DELIVERED = cycle complete (after dispatch to customer or
                    // Gurgaon)
                    boolean allDelivered = true;
                    ZonedDateTime latestDeliveryDate = null;

                    if (req.getItems() != null) {
                        for (RmaItemEntity item : req.getItems()) {
                            String status = item.getRmaStatus();
                            // Check for DELIVERED status (case-insensitive)
                            if (status == null || !status.toUpperCase().contains("DELIVERED")) {
                                allDelivered = false;
                            } else {
                                // Track latest delivery date
                                // For LOCAL repairs: use deliveryDate
                                // For DEPOT repairs: use depotReturnDeliveredDate
                                ZonedDateTime itemDeliveryDate = item.getDeliveryDate();
                                if (itemDeliveryDate == null) {
                                    // Check depot return date for depot repairs
                                    itemDeliveryDate = item.getDepotReturnDeliveredDate();
                                }
                                if (itemDeliveryDate != null) {
                                    if (latestDeliveryDate == null
                                            || itemDeliveryDate.isAfter(latestDeliveryDate)) {
                                        latestDeliveryDate = itemDeliveryDate;
                                    }
                                }
                            }
                        }
                    }

                    if (allDelivered && latestDeliveryDate != null) {
                        // Completed - check if within TAT
                        if (!latestDeliveryDate.isAfter(req.getDueDate())) {
                            completedWithinTat++;
                        } else {
                            completedAfterTat++;
                        }
                    } else {
                        // Still open
                        stillOpen++;
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, req.getDueDate());
                        int halfTat = req.getTat() / 2;

                        if (daysRemaining < 0) {
                            breached++;
                        } else if (daysRemaining <= halfTat) {
                            atRisk++;
                        } else {
                            onTrack++;
                        }

                        // Track oldest open due date
                        if (oldestOpenDueDate == null || req.getDueDate().isBefore(oldestOpenDueDate)) {
                            oldestOpenDueDate = req.getDueDate();
                        }
                    }
                }

                dto.setRequestsWithTat(requestsWithTat);
                dto.setCompletedWithinTat(completedWithinTat);
                dto.setCompletedAfterTat(completedAfterTat);
                dto.setStillOpen(stillOpen);
                dto.setOnTrack(onTrack);
                dto.setAtRisk(atRisk);
                dto.setBreached(breached);
                dto.setOldestOpenDueDate(oldestOpenDueDate);

                // Calculate compliance rate (only for completed requests)
                long totalCompleted = completedWithinTat + completedAfterTat;
                if (totalCompleted > 0) {
                    double rate = (double) completedWithinTat / totalCompleted * 100;
                    dto.setComplianceRate(Math.round(rate * 10.0) / 10.0);
                } else if (requestsWithTat > 0) {
                    // No completed yet, show as N/A (null)
                    dto.setComplianceRate(null);
                } else {
                    // No requests with TAT
                    dto.setComplianceRate(null);
                }

                // Only include customers with at least one request with TAT
                if (requestsWithTat > 0) {
                    report.add(dto);
                }
            }

            // Sort by company name
            report.sort((a, b) -> a.getCompanyName().compareToIgnoreCase(b.getCompanyName()));

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate TAT compliance report: " + e.getMessage());
        }
    }
}