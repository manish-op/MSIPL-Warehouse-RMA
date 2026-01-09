package com.serverManagement.server.management.controller.rma.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.common.CustomerService;

@RestController
@RequestMapping("/api/rma")
public class RmaCustomerController {

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
    public ResponseEntity<?> searchCustomers(@RequestParam(name = "q", required = false) String q) {
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
    public ResponseEntity<?> getCustomerById(@PathVariable("id") Long id) {
        try {
            return customerService.getCustomerById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch customer");
        }
    }
}
