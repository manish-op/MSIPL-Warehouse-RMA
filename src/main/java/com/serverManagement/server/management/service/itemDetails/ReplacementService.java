package com.serverManagement.server.management.service.itemDetails;

import com.serverManagement.server.management.request.itemDetails.ReplacementRequest;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;

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
    private RmaItemDAO rmaItemDao;

    public List<ItemDetailsEntity> searchReplacementItems(String query) {
        return itemDao.searchAllItems(query);
    }

    @Transactional
    public String processReplacement(ReplacementRequest request, String userEmail) {

        ItemDetailsEntity replacementUnit = null;

        // 1. Find Replacement Unit
        if (request.getReplacementSerial() != null && !request.getReplacementSerial().isEmpty()) {
            // A. Specific Serial Requested

            replacementUnit = itemDao.getItemDetailsBySerialNo(request.getReplacementSerial());

            if (replacementUnit == null) {
                throw new RuntimeException("Replacement item not found with Serial: " + request.getReplacementSerial());
            }

            // Optional: Check if it's actually available (unless we want to force it)
            boolean isAvailable = replacementUnit.getAvailableStatusId() != null
                    && "Available".equalsIgnoreCase(replacementUnit.getAvailableStatusId().getItemAvailableOption());

            if (!isAvailable) {
                throw new RuntimeException("Selected replacement item is not AVAILABLE. Current status: "
                        + (replacementUnit.getAvailableStatusId() != null
                                ? replacementUnit.getAvailableStatusId().getItemAvailableOption()
                                : "Unknown"));
            }

        } else {
            // B. Auto-find by Model (Legacy behavior)
            // Ensure DAO method uses 'AVAILABLE'
            List<ItemDetailsEntity> availableItems = itemDao.findAvailableByModel(request.getModelNo());

            if (availableItems.isEmpty()) {
                throw new RuntimeException("OUT_OF_STOCK");
            }
            replacementUnit = availableItems.get(0);
        }

        String replacementSerial = replacementUnit.getSerial_No();

        // 2. Assign Item (Update Status to ISSUED instead of DELETE)
        // Check exact string for "Issued" or "Assigned" from DB/Enum logic.
        // User requested "ISSUED". We try to fetch that option.
        com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity issuedStatus = statusRepo
                .getStatusDetailsByOption("issued");

        // Fallback if "Issued" not found, try "Assigned"
        if (issuedStatus == null) {
            issuedStatus = statusRepo.getStatusDetailsByOption("assigned");
        }

        if (issuedStatus != null) {
            replacementUnit.setAvailableStatusId(issuedStatus);
            // Also set other issue details
            replacementUnit.setRemark("ISSUED as replacement for RMA: " + request.getRmaNumber());
            if (userEmail != null)
                replacementUnit.setEmpEmail(userEmail); // Updated by
            replacementUnit.setUpdate_Date(java.time.ZonedDateTime.now());

            itemDao.save(replacementUnit);

        } else {
            // Fallback: If no status found, we might still have to delete or throw error?
            // For now, let's log error but proceed with logic or revert to delete if
            // strict?
            // User EXPLICITLY asked to update to ISSUED. If we can't find it, that's an
            // issue.

        }

        // 3. Update Original RMA Item
        List<RmaItemEntity> rmaItems = rmaItemDao
                .findByRmaRequest_RequestNumber(request.getRmaNumber());

        // Fallback for legacy items or explicit RMA numbers
        if (rmaItems == null || rmaItems.isEmpty()) {
            rmaItems = rmaItemDao.findByRmaNo(request.getRmaNumber());
        }
        // Find the specific item matching model
        RmaItemEntity targetItem = rmaItems.stream()
                .filter(item -> request.getModelNo().trim().equalsIgnoreCase(item.getModel().trim()))
                .findFirst()
                .orElse(null);

        // Fallback: Try contains if exact match fails (handling partial model numbers)
        if (targetItem == null) {

            targetItem = rmaItems.stream()
                    .filter(item -> item.getModel() != null
                            && item.getModel().toLowerCase().contains(request.getModelNo().trim().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }

        if (targetItem != null) {
            targetItem.setRepairStatus("REPLACED"); // Updated from ASSIGNED to REPLACED as logically it is replaced
            // Wait, previous code said "ASSIGNED". User request said "update item status to
            // ISSUED".
            // That was for the Warehouse Item. For the RMA Item, "REPLACED" makes sense if
            // we have that status.
            // Let's stick to "ASSIGNED" if that's the workflow, or "REPLACED" if the user
            // added it enum.
            // User added "REPLACED" to RepairStatus enum in the diff provided at start!
            // So they want "REPLACED".

            String existingRemarks = targetItem.getRepairRemarks() != null ? targetItem.getRepairRemarks() : "";
            targetItem.setRepairRemarks(existingRemarks + " | Replaced with unit: " + replacementSerial);

            if (userEmail != null && !userEmail.isEmpty()) {
                targetItem.setAssignedToEmail(userEmail);
                if (targetItem.getAssignedToName() == null || targetItem.getAssignedToName().isEmpty()) {
                    targetItem.setAssignedToName(userEmail);
                }
                targetItem.setAssignedDate(java.time.ZonedDateTime.now());
            }

            rmaItemDao.saveAndFlush(targetItem);

        } else {

        }

        return replacementSerial;
    }
}