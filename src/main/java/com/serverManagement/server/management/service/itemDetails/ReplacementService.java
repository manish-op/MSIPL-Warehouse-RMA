package com.serverManagement.server.management.service.itemDetails;

import com.serverManagement.server.management.request.itemDetails.ReplacementRequest;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplacementService {

    @Autowired
    private ItemDetailsDAO itemDao;

    @Autowired
    private ItemAvailableStatusOptionDAO statusRepo;

    @Autowired
    private com.serverManagement.server.management.dao.rma.RmaItemDAO rmaItemDao;

    @Transactional
    public String processReplacement(ReplacementRequest request, String userEmail) {

        System.out.println(
                "Processing replacement for RMA: " + request.getRmaNumber() + ", Model: " + request.getModelNo());

        // 1. Check Inventory
        // Ensure DAO method uses 'AVAILABLE' (case sensitive check was fixed
        // previously)
        List<ItemDetailsEntity> availableItems = itemDao.findAvailableByModel(request.getModelNo());
        System.out.println("Available items found: " + availableItems.size());

        if (availableItems.isEmpty()) {
            throw new RuntimeException("OUT_OF_STOCK");
        }

        // 2. Assign Item
        ItemDetailsEntity replacementUnit = availableItems.get(0);
        String replacementSerial = replacementUnit.getSerial_No();

        // User explicitly requested DELETION of the item from warehouse inventory upon
        // issuance.
        // This removes the record from the item_details_table entirely.
        System.out.println("Processing Hard Deletion for Item Serial: " + replacementSerial);
        itemDao.delete(replacementUnit);
        itemDao.flush(); // Force delete immediately

        System.out.println(
                "Replacement processed successfully. Item DELETED from inventory. Serial was: " + replacementSerial);

        // 3. Update Original RMA Item
        List<com.serverManagement.server.management.entity.rma.RmaItemEntity> rmaItems = rmaItemDao
                .findByRmaRequest_RequestNumber(request.getRmaNumber());

        // Fallback for legacy items or explicit RMA numbers
        if (rmaItems == null || rmaItems.isEmpty()) {
            rmaItems = rmaItemDao.findByRmaNo(request.getRmaNumber());
        }
        // Find the specific item matching model
        com.serverManagement.server.management.entity.rma.RmaItemEntity targetItem = rmaItems.stream()
                .filter(item -> request.getModelNo().trim().equalsIgnoreCase(item.getModel().trim())) // Start with
                                                                                                      // case-insensitive,
                                                                                                      // trimmed match
                .findFirst()
                .orElse(null);

        // Fallback: Try contains if exact match fails (handling partial model numbers)
        if (targetItem == null) {
            System.out.println("Exact model match failed. Trying partial match for: " + request.getModelNo());
            targetItem = rmaItems.stream()
                    .filter(item -> item.getModel() != null
                            && item.getModel().toLowerCase().contains(request.getModelNo().trim().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }

        if (targetItem != null) {
            targetItem.setRepairStatus("ASSIGNED"); // Moving back to Assigned Page
            String existingRemarks = targetItem.getRepairRemarks() != null ? targetItem.getRepairRemarks() : "";
            targetItem.setRepairRemarks(existingRemarks + " | Replaced with unit: " + replacementSerial);

            // Force assignment if missing or if ownership needs to be taken
            if (userEmail != null && !userEmail.isEmpty()) {
                // Update the assignee to the current user (engineer processing replacement)
                // This ensures it appears in THEIR assigned page list.
                targetItem.setAssignedToEmail(userEmail);
                // We don't have name easily, so fallback to email or keep existing name if
                // present
                if (targetItem.getAssignedToName() == null || targetItem.getAssignedToName().isEmpty()) {
                    targetItem.setAssignedToName(userEmail);
                }
                targetItem.setAssignedDate(java.time.ZonedDateTime.now());
            }

            rmaItemDao.saveAndFlush(targetItem);
            System.out.println("Updated RMA Item status to ASSIGNED for Model: " + targetItem.getModel());
        } else {
            System.out.println("CRITICAL WARNING: Could not find original RMA item to update status.");
            System.out.println("Searching for Model: '" + request.getModelNo() + "' in RMA: " + request.getRmaNumber());
            System.out.println("Available Models in this RMA:");
            for (com.serverManagement.server.management.entity.rma.RmaItemEntity item : rmaItems) {
                System.out.println(" - " + item.getModel());
            }
        }

        return replacementSerial;
    }
}
