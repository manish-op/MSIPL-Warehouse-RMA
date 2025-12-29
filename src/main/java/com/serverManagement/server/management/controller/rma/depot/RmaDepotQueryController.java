package com.serverManagement.server.management.controller.rma.depot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.depot.DepotDispatchItemDto;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotQueryController {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    // 1) GET: depot items waiting for first dispatch
    // RBAC: Only Bangalore users or Admins can see DEPOT items
    @GetMapping("/depot/ready-to-dispatch")
    public ResponseEntity<?> getDepotReadyToDispatch(HttpServletRequest request) {
        try {
            // RBAC Check
            String loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());

            if (!isAdminOrBangaloreUser(loggedInUser)) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }

            List<DepotDispatchItemDto> items = rmaItemDAO
                    .findByRepairTypeAndDepotStage("DEPOT", "PENDING_DISPATCH_TO_DEPOT")
                    .stream()
                    .map(DepotDispatchItemDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching depot items: " + e.getMessage());
        }
    }

    // 3) GET: depot items in transit or at depot
    // RBAC: Only Bangalore users or Admins can see DEPOT items
    @GetMapping("/depot/in-transit")
    public ResponseEntity<?> getInTransitItems(HttpServletRequest request) {
        try {
            // RBAC Check
            String loggedInUserEmail = request.getUserPrincipal().getName();
            AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());

            if (!isAdminOrBangaloreUser(loggedInUser)) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }

            List<DepotDispatchItemDto> items = rmaItemDAO
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
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching in-transit items: " + e.getMessage());
        }
    }

    /**
     * Helper method to check if user is Admin or from Bangalore region
     */
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
}
