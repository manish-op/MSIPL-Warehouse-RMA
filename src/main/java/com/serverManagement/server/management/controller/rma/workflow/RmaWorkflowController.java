package com.serverManagement.server.management.controller.rma.workflow;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.workflow.RmaWorkflowService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaWorkflowController {

    @Autowired
    private RmaWorkflowService rmaWorkflowService;

    @GetMapping("/items/unassigned")
    public ResponseEntity<?> getUnassignedItems(HttpServletRequest request) {
        try {
            return rmaWorkflowService.getUnassignedItems(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/assigned")
    public ResponseEntity<?> getAssignedItems(HttpServletRequest request) {
        try {
            return rmaWorkflowService.getAssignedItems(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/repaired")
    public ResponseEntity<?> getRepairedItems(HttpServletRequest request) {
        try {
            return rmaWorkflowService.getRepairedItems(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/cant-be-repaired")
    public ResponseEntity<?> getCantBeRepairedItems(HttpServletRequest request) {
        try {
            return rmaWorkflowService.getCantBeRepairedItems(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/dispatched")
    public ResponseEntity<?> getDispatchedItems(HttpServletRequest request) {
        try {
            return rmaWorkflowService.getDispatchedItems(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-delivery")
    public ResponseEntity<?> confirmDelivery(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> itemIdInts = (List<Integer>) payload.get("itemIds");
            List<Long> itemIds = itemIdInts.stream().map(Integer::longValue).toList();
            String deliveredTo = (String) payload.get("deliveredTo");
            String deliveredBy = (String) payload.get("deliveredBy");
            String deliveryNotes = (String) payload.get("deliveryNotes");
            return rmaWorkflowService.confirmDelivery(request, itemIds, deliveredTo, deliveredBy, deliveryNotes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/assign")
    public ResponseEntity<?> assignItem(HttpServletRequest request, @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String assigneeEmail = payload.get("assigneeEmail");
            String assigneeName = payload.get("assigneeName");
            return rmaWorkflowService.assignItem(request, id, assigneeEmail, assigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/items/{id}/reassign")
    public ResponseEntity<?> reassignItem(HttpServletRequest request, @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String newAssigneeEmail = payload.get("assigneeEmail");
            String newAssigneeName = payload.get("assigneeName");
            String reason = payload.get("reason");
            return rmaWorkflowService.reassignItem(request, id, newAssigneeEmail, newAssigneeName, reason);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<?> bulkAssign(HttpServletRequest request,
            @RequestBody Map<String, String> payload) {
        try {
            String rmaNo = payload.get("rmaNo");
            String assigneeEmail = payload.get("assigneeEmail");
            String assigneeName = payload.get("assigneeName");
            return rmaWorkflowService.bulkAssignByRmaNo(request, rmaNo, assigneeEmail, assigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/items/{id}/rma-number")
    public ResponseEntity<?> updateItemRmaNumber(HttpServletRequest request, @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String rmaNo = payload.get("rmaNo");
            return rmaWorkflowService.updateItemRmaNumber(request, id, rmaNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/status")
    public ResponseEntity<?> updateItemStatus(HttpServletRequest request, @PathVariable("id") Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            String remarks = payload.get("remarks");
            String issueFixed = payload.get("issueFixed");
            return rmaWorkflowService.updateItemStatus(request, id, status, remarks, issueFixed);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
