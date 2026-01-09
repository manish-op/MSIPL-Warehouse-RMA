package com.serverManagement.server.management.controller.rma.depot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.depot.DepotDispatchItemDto;
import com.serverManagement.server.management.service.rma.common.RmaModelMapper;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotQueryController {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private RmaModelMapper rmaModelMapper;

    // 1) GET: depot items waiting for first dispatch
    // All authenticated users can see DEPOT items
    @GetMapping("/depot/ready-to-dispatch")
    public ResponseEntity<?> getDepotReadyToDispatch(HttpServletRequest request) {
        try {
            List<RmaItemEntity> entities = rmaItemDAO
                    .findByRepairTypeAndDepotStage("DEPOT", "PENDING_DISPATCH_TO_DEPOT");
            List<DepotDispatchItemDto> items = rmaModelMapper.convertToDepotDTOList(entities);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching depot items: " + e.getMessage());
        }
    }

    // 3) GET: depot items in transit or at depot
    // All authenticated users can see DEPOT items
    @GetMapping("/depot/in-transit")
    public ResponseEntity<?> getInTransitItems(HttpServletRequest request) {
        try {
            List<RmaItemEntity> entities = rmaItemDAO
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
                            "GGN_DELIVERED_TO_CUSTOMER"));
            List<DepotDispatchItemDto> items = rmaModelMapper.convertToDepotDTOList(entities);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching in-transit items: " + e.getMessage());
        }
    }

    // Helper method removed - RBAC check no longer used
}
