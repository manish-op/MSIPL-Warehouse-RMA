package com.serverManagement.server.management.service.itemstock;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemstock.InventoryThresholdRepository;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemstock.InventoryThreshold;
import com.serverManagement.server.management.entity.region.RegionEntity;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AdminUserDAO adminUserDAO;
    private final InventoryThresholdRepository thresholdRepository;
    private final ItemDetailsDAO itemDetailsDAO;

    public AlertService(AdminUserDAO adminUserDAO, InventoryThresholdRepository thresholdRepository,
            ItemDetailsDAO itemDetailsDAO) {
        this.adminUserDAO = adminUserDAO;
        this.thresholdRepository = thresholdRepository;
        this.itemDetailsDAO = itemDetailsDAO;
    }

    /**
     * This is the main method the controller will call.
     * It gets all active low-stock alerts for the logged-in user.
     */
    public List<Map<String, String>> getActiveAlerts(Principal principal) {
        // 1. Get the logged-in user and their role
        AdminUserEntity user = adminUserDAO.findByEmail(principal.getName().toLowerCase());
        RegionEntity managerRegion = user.getRegionEntity(); // This is the user's assigned region
        String role = user.getRoleModel().getRoleName().toLowerCase();

        // 2. Find which rules this user needs to check
        List<InventoryThreshold> rulesToCheck = new ArrayList<>();
        if (role.equals("admin")) {
            // Admins see all low-stock warnings from all regions
            rulesToCheck = thresholdRepository.findAll();
        } else if (role.equals("manager") && managerRegion != null) {
            // Managers only see warnings for their assigned region
            rulesToCheck = thresholdRepository.findByRegion(managerRegion);
        }
        // Other roles will see an empty list

        // 3. Process the rules and generate alert messages
        return processRules(rulesToCheck);
    }

    /**
     * A helper method that loops through a list of rules,
     * checks the database for each one, and returns a list of alert messages.
     */

    private List<Map<String, String>> processRules(List<InventoryThreshold> rules) {
        List<Map<String, String>> messages = new ArrayList<>();

        for (InventoryThreshold rule : rules) {
            // 4. Get the current stock count using the query we fixed
            long currentStock = itemDetailsDAO.countAvailableByPartNoAndRegion(rule.getPartNo(), rule.getRegion());

            // 5. Compare stock to the rule
            if (currentStock < rule.getMinQuantity()) {
                // 6. Create a structured alert
                Map<String, String> alert = new HashMap<>();
                alert.put("type", "STOCK");
                alert.put("severity", "WARNING");
                alert.put("message", String.format("LOW STOCK for part %s in %s. Current: %d, Threshold: %d.",
                        rule.getPartNo(), rule.getRegion().getCity(), currentStock, rule.getMinQuantity()));
                alert.put("partNo", rule.getPartNo());
                alert.put("region", rule.getRegion().getCity());
                alert.put("currentStock", String.valueOf(currentStock));
                alert.put("threshold", String.valueOf(rule.getMinQuantity()));
                messages.add(alert);
            }
        }
        // 7. Check for Overdue Items
        // System.out.println("Checking for overdue items...");
        List<ItemDetailsEntity> issuedItems = itemDetailsDAO
                .findIssuedItemsWithReturnDuration();
        // System.out.println("Found " + issuedItems.size() + " issued items with return
        // duration.");
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();

        for (ItemDetailsEntity item : issuedItems) {
            if (item.getReturnDuration() != null && item.getUpdate_Date() != null) {
                ZonedDateTime dueDate = item.getUpdate_Date().plusDays(item.getReturnDuration());
                String partyName = item.getPartyName() != null ? item.getPartyName() : "Unknown";

                if (now.isAfter(dueDate)) {
                    long overdueDays = ChronoUnit.DAYS.between(dueDate, now);

                    Map<String, String> alert = new HashMap<>();
                    alert.put("type", "RETURN");
                    alert.put("severity", "CRITICAL");
                    alert.put("message",
                            String.format("CRITICAL: Item %s issued to %s is OVERDUE by %d days (Due: %s).",
                                    item.getSerial_No(), partyName, overdueDays, dueDate.toLocalDate()));
                    alert.put("serialNo", item.getSerial_No());
                    alert.put("partyName", partyName);
                    alert.put("id", String.valueOf(item.getId()));
                    alert.put("daysOverdue", String.valueOf(overdueDays));
                    alert.put("dueDate", dueDate.toLocalDate().toString());
                    messages.add(alert);
                } else {
                    // 50% Duration Check
                    long totalDuration = item.getReturnDuration();
                    long elapsedDays = ChronoUnit.DAYS.between(item.getUpdate_Date(), now);

                    if (elapsedDays >= (totalDuration / 2)) {
                        long daysLeft = ChronoUnit.DAYS.between(now, dueDate);

                        Map<String, String> alert = new HashMap<>();
                        alert.put("type", "RETURN");
                        alert.put("severity", "WARNING");
                        alert.put("message", String.format(
                                "WARNING: Item %s issued to %s has reached 50%% of return duration. %d days left (Due: %s).",
                                item.getSerial_No(), partyName, daysLeft, dueDate.toLocalDate()));
                        alert.put("serialNo", item.getSerial_No());
                        alert.put("partyName", partyName);
                        alert.put("id", String.valueOf(item.getId()));
                        alert.put("daysLeft", String.valueOf(daysLeft));
                        alert.put("dueDate", dueDate.toLocalDate().toString());
                        messages.add(alert);
                    }
                }
            }
        }

        return messages;
    }
}