package com.serverManagement.server.management.controller.rma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.RmaOutwardGatepassService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for RMA Outward Gatepass (Local Repair)
 */
@RestController
@RequestMapping("/api/rma/outward-gatepass")
public class RmaOutwardGatepassController {

    @Autowired
    private RmaOutwardGatepassService rmaOutwardGatepassService;

    @PostMapping("/generate/{requestNumber}")
    public ResponseEntity<?> generateGatepass(HttpServletRequest request,
            @PathVariable("requestNumber") String requestNumber) {
        return rmaOutwardGatepassService.generateGatepass(request, requestNumber);
    }
}
