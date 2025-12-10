package com.serverManagement.server.management.service.itemstock;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemstock.InventoryThresholdRepository;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.itemstock.InventoryThreshold;
import com.serverManagement.server.management.entity.region.RegionEntity;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private final AdminUserDAO adminUserDAO;
    private final InventoryThresholdRepository thresholdRepository;
    private final ItemDetailsDAO itemDetailsDAO;

    public AlertService(AdminUserDAO adminUserDAO, InventoryThresholdRepository thresholdRepository, ItemDetailsDAO itemDetailsDAO) {
        this.adminUserDAO = adminUserDAO;
        this.thresholdRepository = thresholdRepository;
        this.itemDetailsDAO = itemDetailsDAO;
    }

    /**
     * This is the main method the controller will call.
     * It gets all active low-stock alerts for the logged-in user.
     */
    public List<String> getActiveAlerts(Principal principal) {
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

    private List<String> processRules(List<InventoryThreshold> rules) {
        List<String> messages = new ArrayList<>();

        for (InventoryThreshold rule : rules) {
            // 4. Get the current stock count using the query we fixed
            long currentStock = itemDetailsDAO.countAvailableByPartNoAndRegion(rule.getPartNo(), rule.getRegion());

            // 5. Compare stock to the rule
            if (currentStock < rule.getMinQuantity()) {
                // 6. Create a user-friendly message
                String message = String.format("LOW STOCK for part %s in %s. " + "Current: %d, Threshold: %d.", rule.getPartNo(), rule.getRegion().getCity(), currentStock, rule.getMinQuantity());
                messages.add(message);
            }
        }
        return messages;
    }
}