package com.serverManagement.server.management.controller.rma.depot;

import com.serverManagement.server.management.dao.rma.depot.ProductHsnRepository;
import com.serverManagement.server.management.dao.rma.depot.SavedAddressRepository;
import com.serverManagement.server.management.entity.rma.depot.ProductHsnEntity;
import com.serverManagement.server.management.entity.rma.depot.SavedAddressEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rma")
@CrossOrigin(origins = "*") // Allow frontend access
public class AddressHsnController {

    @Autowired
    private SavedAddressRepository savedAddressRepository;

    @Autowired
    private ProductHsnRepository productHsnRepository;

    // --- Saved Addresses ---

    @GetMapping("/addresses")
    public List<SavedAddressEntity> getAllAddresses() {
        return savedAddressRepository.findAll();
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> saveAddress(@RequestBody SavedAddressEntity address) {
        if (address.getName() == null || address.getAddress() == null) {
            return ResponseEntity.badRequest().body("Name and Address are required");
        }

        Optional<SavedAddressEntity> existing = savedAddressRepository.findByName(address.getName());
        if (existing.isPresent()) {
            // Update existing
            SavedAddressEntity entity = existing.get();
            entity.setAddress(address.getAddress());
            entity.setGstIn(address.getGstIn());
            entity.setContactPerson(address.getContactPerson());
            entity.setContactNumber(address.getContactNumber());
            savedAddressRepository.save(entity);
            return ResponseEntity.ok("Address updated successfully");
        }

        savedAddressRepository.save(address);
        return ResponseEntity.ok("Address saved successfully");
    }

    // --- Product HSN ---

    @GetMapping("/hsn/{productName}")
    public ResponseEntity<?> getHsnForProduct(@PathVariable String productName) {
        Optional<ProductHsnEntity> hsn = productHsnRepository.findByProductName(productName);
        if (hsn.isPresent()) {
            return ResponseEntity.ok(hsn.get());
        }
        return ResponseEntity.status(404).body("HSN not found for product: " + productName);
    }

    @PostMapping("/hsn")
    public ResponseEntity<?> saveHsn(@RequestBody ProductHsnEntity hsnEntity) {
        if (hsnEntity.getProductName() == null || hsnEntity.getHsnCode() == null) {
            return ResponseEntity.badRequest().body("Product Name and HSN Code are required");
        }

        Optional<ProductHsnEntity> existing = productHsnRepository.findByProductName(hsnEntity.getProductName());
        if (existing.isPresent()) {
            ProductHsnEntity entity = existing.get();
            entity.setHsnCode(hsnEntity.getHsnCode());
            productHsnRepository.save(entity);
            return ResponseEntity.ok("HSN updated successfully");
        }

        productHsnRepository.save(hsnEntity);
        return ResponseEntity.ok("HSN saved successfully");
    }
}
