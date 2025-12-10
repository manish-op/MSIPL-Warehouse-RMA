package com.serverManagement.server.management.controller.itemstock;

import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemstock.InventoryThresholdRepository;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.entity.itemstock.InventoryThreshold;
import com.serverManagement.server.management.entity.region.RegionEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional; // <-- **1. ADD THIS IMPORT**

@RestController
@RequestMapping("/api/thresholds")
public class InventoryThresholdController {

    private final InventoryThresholdRepository thresholdRepository;
    private final RegionDAO regionDAO;
    private final ItemDetailsDAO itemDetailsDAO; // <-- **2. MAKE THIS 'final'**

    // **3. ADD ItemDetailsDAO TO THE CONSTRUCTOR**
    public InventoryThresholdController(InventoryThresholdRepository thresholdRepository,
                                        RegionDAO regionDAO,
                                        ItemDetailsDAO itemDetailsDAO) {
        this.thresholdRepository = thresholdRepository;
        this.regionDAO = regionDAO;
        this.itemDetailsDAO = itemDetailsDAO; // <-- **4. INITIALIZE IT HERE**
    }

    /**
     * A simple DTO (Data Transfer Object) class to receive the request data.
     */
    public static class ThresholdRequestDTO {
        private String partNo;
        private Integer minQuantity;
        private String regionName; // We'll use regionName to find the RegionEntity

        // Getters and Setters
        public String getPartNo() { return partNo; }
        public void setPartNo(String partNo) { this.partNo = partNo; }
        public Integer getMinQuantity() { return minQuantity; }
        public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
    }


    // **5. REPLACE YOUR ENTIRE createThreshold METHOD WITH THIS:**
    @PostMapping
    public ResponseEntity<?> createThreshold(@RequestBody ThresholdRequestDTO request) {

        // 1. Validate PartNo exists
        boolean partNoExists = itemDetailsDAO.existsByPartNo(request.getPartNo());
        if (!partNoExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Part number does not exist: " + request.getPartNo());
        }

        // 2. Find the region
        RegionEntity region = regionDAO.findByCity(request.getRegionName().trim().toLowerCase());
        if (region == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Region not found: " + request.getRegionName());
        }

        Optional<InventoryThreshold> existingThresholdOpt =
                thresholdRepository.findByPartNoAndRegion(request.getPartNo(), region);

        String message;
        InventoryThreshold thresholdToSave;

        if (existingThresholdOpt.isPresent()) {
            // IT EXISTS: Get the existing rule to update it
            thresholdToSave = existingThresholdOpt.get();
            message = "Threshold rule updated for " + request.getPartNo();
        } else {
            // IT'S NEW: Create a new rule object
            thresholdToSave = new InventoryThreshold();
            thresholdToSave.setPartNo(request.getPartNo());
            thresholdToSave.setRegion(region);
            message = "Threshold rule created for " + request.getPartNo();
        }

        // 4. Set (or update) the quantity
        thresholdToSave.setMinQuantity(request.getMinQuantity());

        // 5. Save the new OR updated record
        thresholdRepository.save(thresholdToSave);

        // Return the correct success message
        return ResponseEntity.status(HttpStatus.OK)
                .body(message);
    }
}