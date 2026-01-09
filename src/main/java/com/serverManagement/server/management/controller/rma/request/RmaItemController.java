package com.serverManagement.server.management.controller.rma.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.request.RmaRequestService;

@RestController
@RequestMapping("/api/rma")
public class RmaItemController {

    @Autowired
    private RmaRequestService rmaRequestService;

    @GetMapping("/items")
    public ResponseEntity<?> getAllRmaItems() {
        try {
            return rmaRequestService.getAllRmaItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/grouped")
    public ResponseEntity<?> getRmaItemsGrouped() {
        try {
            return rmaRequestService.getAllRmaItemsGrouped();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/items/all")
    public ResponseEntity<?> getAllItems() {
        try {
            return rmaRequestService.getAllItems();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/search/items")
    public ResponseEntity<?> searchItems(@RequestParam("q") String query) {
        try {
            return rmaRequestService.searchItems(query);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
