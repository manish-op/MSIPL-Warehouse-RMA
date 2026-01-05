package com.serverManagement.server.management.controller.rma.request;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.rma.CreateRmaRequest;
import com.serverManagement.server.management.service.rma.request.RmaExcelExportService;
import com.serverManagement.server.management.service.rma.request.RmaRequestService;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaRequestController {

    @Autowired
    private RmaRequestService rmaRequestService;

    @Autowired
    private RmaExcelExportService excelExportService;

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRmaRequests(@RequestParam(name = "timeFilter", required = false) String timeFilter) {
        try {
            return rmaRequestService.getAllRmaRequests(timeFilter);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRmaRequest(HttpServletRequest request,
            @RequestBody CreateRmaRequest createRmaRequest) {
        try {
            return rmaRequestService.createRmaRequest(request, createRmaRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
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

    @GetMapping("/serial-history")
    public ResponseEntity<?> getSerialHistory(@RequestParam String serialNo) {
        try {
            return rmaRequestService.getSerialHistory(serialNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/raw-history")
    public ResponseEntity<?> getRawHistory(@RequestParam String serialNo) {
        try {
            return ResponseEntity.ok(rmaItemDAO.findBySerialNoIgnoreCaseOrderByIdDesc(serialNo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
