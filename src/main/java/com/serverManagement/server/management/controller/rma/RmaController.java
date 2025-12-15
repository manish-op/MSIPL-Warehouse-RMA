package com.serverManagement.server.management.controller.rma;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.rma.CreateRmaRequest;
import com.serverManagement.server.management.service.rma.CustomerService;
import com.serverManagement.server.management.service.rma.RmaExcelExportService;
import com.serverManagement.server.management.service.rma.RmaService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/rma")
public class RmaController {

    @Autowired
    private RmaService rmaService;

    @Autowired
    private RmaExcelExportService excelExportService;

    @PostMapping("/create")
    public ResponseEntity<?> createRmaRequest(HttpServletRequest request,
            @RequestBody CreateRmaRequest createRmaRequest) {
        try {
            return rmaService.createRmaRequest(request, createRmaRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/grouped")
    public ResponseEntity<?> getRmaItemsGrouped() {
        try {
            return rmaService.getAllRmaItemsGrouped();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getRmaDashboardStats() {
        try {
            return rmaService.getRmaDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // ============ WORKFLOW ENDPOINTS ============

    @GetMapping("/items/unassigned")
    public ResponseEntity<?> getUnassignedItems() {
        try {
            return rmaService.getUnassignedItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/assigned")
    public ResponseEntity<?> getAssignedItems() {
        try {
            return rmaService.getAssignedItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/repaired")
    public ResponseEntity<?> getRepairedItems() {
        try {
            return rmaService.getRepairedItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/cant-be-repaired")
    public ResponseEntity<?> getCantBeRepairedItems() {
        try {
            return rmaService.getCantBeRepairedItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/assign")
    public ResponseEntity<?> assignItem(HttpServletRequest request, @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String assigneeEmail = payload.get("assigneeEmail");
            String assigneeName = payload.get("assigneeName");
            return rmaService.assignItem(request, id, assigneeEmail, assigneeName);
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
            return rmaService.bulkAssignByRmaNo(request, rmaNo, assigneeEmail, assigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/items/{id}/rma-number")
    public ResponseEntity<?> updateItemRmaNumber(HttpServletRequest request, @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String rmaNo = payload.get("rmaNo");
            return rmaService.updateItemRmaNumber(request, id, rmaNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/status")
    public ResponseEntity<?> updateItemStatus(HttpServletRequest request, @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            String remarks = payload.get("remarks");
            String issueFixed = payload.get("issueFixed");
            return rmaService.updateItemStatus(request, id, status, remarks, issueFixed);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/product-catalog")
    public ResponseEntity<?> getProductCatalog() {
        try {
            return rmaService.getProductCatalog();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs() {
        try {
            return rmaService.getAuditLogs();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/audit-logs/item/{itemId}")
    public ResponseEntity<?> getAuditLogsByItemId(@PathVariable Long itemId) {
        try {
            return rmaService.getAuditLogsByItemId(itemId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    /**
     * Export RMA data to Excel using template
     * Preserves original template formatting and images
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/export-excel")
    public ResponseEntity<?> exportToExcel(@RequestBody Map<String, Object> exportData) {
        try {
            Map<String, Object> formData = (Map<String, Object>) exportData.get("formData");
            List<Map<String, Object>> items = (List<Map<String, Object>>) exportData.get("items");

            if (formData == null || items == null) {
                return ResponseEntity.badRequest().body("Missing formData or items");
            }

            ByteArrayResource resource = excelExportService.exportToExcel(formData, items);

            String filename = "RMA_Request_" + java.time.LocalDate.now() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to export Excel: " + e.getMessage());
        }
    }

    // ============ Customer API Endpoints ============

    @Autowired
    private CustomerService customerService;

    /**
     * Get all saved customers (for dropdown list)
     */
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        try {
            return customerService.getAllCustomers();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch customers");
        }
    }

    /**
     * Search customers by company name or email (for auto-complete)
     */
    @GetMapping("/customers/search")
    public ResponseEntity<?> searchCustomers(@RequestParam(required = false) String q) {
        try {
            return customerService.searchCustomers(q);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to search customers");
        }
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            return customerService.getCustomerById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch customer");
        }
    }

    // ============ INWARD GATEPASS ENDPOINTS ============

    @Autowired
    private com.serverManagement.server.management.service.rma.RmaInwardGatepassService rmaInwardGatepassService;

    /**
     * Generate Inward Gatepass PDF for items in an RMA request
     */
    @PostMapping("/gatepass/generate/{requestNumber}")
    public ResponseEntity<?> generateInwardGatepass(HttpServletRequest request,
            @PathVariable String requestNumber) {
        try {
            return rmaInwardGatepassService.generateGatepass(request, requestNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to generate gatepass: " + e.getMessage());
        }
    }
<<<<<<< HEAD

    @Autowired
    private com.serverManagement.server.management.service.rma.RmaPdfService rmaPdfService;

    @PostMapping("/delivery-challan/generate")
    public ResponseEntity<?> generateDeliveryChallan(
            @RequestBody com.serverManagement.server.management.dto.rma.DeliveryChallanRequest payload) {
        try {
            byte[] pdfBytes = rmaPdfService.generateDeliveryChallan(payload);

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"DeliveryChallan_" + payload.getRmaNo() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to generate Delivery Challan: " + e.getMessage());
        }
    }
=======
>>>>>>> 4b696b9936a28222d4f1ee66323e246c86f5a4f3
}
