package com.serverManagement.server.management.controller.rma.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.dto.rma.depot.DeliveryChallanRequest;
import com.serverManagement.server.management.service.rma.depot.RmaDepotService;
import com.serverManagement.server.management.service.rma.workflow.RmaInwardGatepassService;
import com.serverManagement.server.management.service.rma.workflow.RmaPdfService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/rma")
public class RmaDocumentController {

    @Autowired
    private RmaDepotService rmaDepotService;

    @Autowired
    private RmaInwardGatepassService rmaInwardGatepassService;

    @Autowired
    private RmaPdfService rmaPdfService;

    /**
     * Generate Inward Gatepass PDF for items in an RMA request
     */
    @PostMapping("/gatepass/generate/{requestNumber}")
    public ResponseEntity<?> generateInwardGatepass(HttpServletRequest request,
            @PathVariable("requestNumber") String requestNumber) {
        try {
            return rmaInwardGatepassService.generateGatepass(request, requestNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to generate gatepass: " + e.getMessage());
        }
    }

    @PostMapping("/delivery-challan/generate")
    public ResponseEntity<?> generateDeliveryChallan(@RequestBody DeliveryChallanRequest payload) {
        try {
            // Save Transporter and Dispatch details
            rmaDepotService.saveDcDetails(payload);

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
}
