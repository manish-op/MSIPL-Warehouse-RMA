package com.serverManagement.server.management.controller.rma;

import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dto.rma.DepotDispatchItemDto;
import com.serverManagement.server.management.dto.rma.DepotDispatchRequest;
import com.serverManagement.server.management.entity.rma.RmaItemEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rma")
public class RmaDepotDispatchController {

    private final RmaItemDAO rmaItemDAO;

    public RmaDepotDispatchController(RmaItemDAO rmaItemDAO) {
        this.rmaItemDAO = rmaItemDAO;
    }

    // 1) GET: depot items waiting for first dispatch
    @GetMapping("/depot/ready-to-dispatch")
    public List<DepotDispatchItemDto> getDepotReadyToDispatch() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStage("DEPOT", "PENDING_DISPATCH_TO_DEPOT")
                .stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

    // 2) POST: mark as dispatched to Bangalore
    @PostMapping("/depot/dispatch-to-bangalore")
    public ResponseEntity<?> dispatchToBangalore(@RequestBody DepotDispatchRequest req) {
        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found for dispatch");
        }

        // Create new Dispatch Entity
        com.serverManagement.server.management.entity.rma.DepotDispatchEntity dispatch = new com.serverManagement.server.management.entity.rma.DepotDispatchEntity();
        dispatch.setDcNo(req.getDcNo());
        dispatch.setEwayBillNo(req.getEwayBillNo());
        dispatch.setCourierName(req.getCourierName());
        dispatch.setTrackingNo(req.getTrackingNo());
        dispatch.setRemarks(req.getRemarks());

        if (req.getDispatchDate() != null) {
            dispatch.setDispatchDate(req.getDispatchDate().atStartOfDay(java.time.ZoneId.systemDefault()));
        } else {
            dispatch.setDispatchDate(java.time.ZonedDateTime.now());
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }
            item.setDepotStage("IN_TRANSIT_TO_DEPOT");

            // Link to the new dispatch entity
            item.setDepotDispatch(dispatch);
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Dispatched to Bangalore");
    }

    // 3) GET: depot items in transit or delivered (history)
    @GetMapping("/depot/in-transit")
    public List<DepotDispatchItemDto> getInTransitItems() {
        return rmaItemDAO
                .findByRepairTypeAndDepotStageIn("DEPOT", List.of(
                        "IN_TRANSIT_TO_DEPOT",
                        "AT_DEPOT_RECEIVED",
                        "AT_DEPOT_UNREPAIRED",
                        "AT_DEPOT_REPAIRING",
                        "AT_DEPOT_REPAIRED"
                )).stream()
                .map(DepotDispatchItemDto::fromEntity)
                .toList();
    }

    // 4) POST: mark as received at depot
    @PostMapping("/depot/mark-received")
    public ResponseEntity<?> markAsReceived(@RequestBody DepotDispatchRequest req) {
        List<RmaItemEntity> items = rmaItemDAO.findAllById(req.getItemIds());
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No items found");
        }

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }
            // Update stage to specific received status
            item.setDepotStage("AT_DEPOT_RECEIVED");
        }

        rmaItemDAO.saveAll(items);
        return ResponseEntity.ok("Items marked as Received at Depot");
    }
}
