package com.serverManagement.server.management.service.rma.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dto.rma.workflow.RmaItemWorkflowDTO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;

@Component
public class RmaModelMapper {

    @Autowired
    private AdminUserDAO adminUserDAO;

    /**
     * Helper method to convert entity list to DTO list
     */
    public List<RmaItemWorkflowDTO> convertToItemDTOList(List<RmaItemEntity> items) {
        List<RmaItemWorkflowDTO> dtoList = new ArrayList<>();
        // Cache to prevent redundant DB calls for the same user
        Map<String, String> userEmailToNameMap = new HashMap<>();

        for (RmaItemEntity item : items) {
            RmaItemWorkflowDTO dto = new RmaItemWorkflowDTO();
            dto.setId(item.getId());
            dto.setProduct(item.getProduct());

            // Mask internal unique IDs for display
            String displaySerial = item.getSerialNo();
            if (displaySerial != null && displaySerial.startsWith("NA-")) {
                displaySerial = "N/A";
            }
            dto.setSerialNo(displaySerial);

            dto.setModel(item.getModel());
            dto.setFaultDescription(item.getFaultDescription());
            dto.setRepairStatus(item.getRepairStatus());
            dto.setAssignedToEmail(item.getAssignedToEmail());
            dto.setAssignedToName(item.getAssignedToName());
            dto.setAssignedDate(item.getAssignedDate());
            dto.setRepairedByEmail(item.getRepairedByEmail());
            dto.setRepairedByName(item.getRepairedByName());
            dto.setRepairedDate(item.getRepairedDate());

            dto.setRepairRemarks(item.getRepairRemarks());
            // Get Request Number from parent request (for display)
            // Note: rmaNo is for actual RMA number assigned later, requestNumber is
            // auto-generated
            if (item.getRmaRequest() != null) {
                String reqNo = item.getRmaRequest().getRequestNumber();
                dto.setRmaNo(reqNo != null ? reqNo : item.getRmaNo()); // Fallback to item rmaNo if request number is
                                                                       // null

                // Set customer and date info for FRU sticker display
                dto.setCompanyName(item.getRmaRequest().getCompanyName());
                dto.setReceivedDate(item.getRmaRequest().getCreatedDate());
                dto.setRepairType(item.getRmaRequest().getRepairType());
                dto.setTat(item.getRmaRequest().getTat());
            } else {
                // Fallback for items with missing parent request (Legacy data)
                dto.setRmaNo(item.getRmaNo());
            }

            // Populate Created By User Name
            if (item.getRmaRequest() != null) {
                String creatorEmail = item.getRmaRequest().getCreatedByEmail();
                if (creatorEmail != null && !creatorEmail.trim().isEmpty()) {
                    String lowerEmail = creatorEmail.toLowerCase();
                    if (userEmailToNameMap.containsKey(lowerEmail)) {
                        dto.setUserName(userEmailToNameMap.get(lowerEmail));
                    } else {
                        try {
                            AdminUserEntity user = adminUserDAO.findByEmail(lowerEmail);
                            if (user != null) {
                                userEmailToNameMap.put(lowerEmail, user.getName());
                                dto.setUserName(user.getName());
                            } else {
                                userEmailToNameMap.put(lowerEmail, "Unknown");
                                dto.setUserName("Unknown");
                            }
                        } catch (Exception e) {
                            dto.setUserName("Unknown");
                        }
                    }
                }
            }

            // Set the item-level RMA number (distinct from parent request number)
            dto.setItemRmaNo(item.getRmaNo());
            dto.setIssueFixed(item.getIssueFixed());
            dto.setLastReassignmentReason(item.getLastReassignmentReason()); // Populate DTO field
            dto.setIsDispatched(item.getIsDispatched()); // Dispatch status for filtering

            // Dispatch tracking fields
            dto.setDispatchTo(item.getDispatchTo());
            dto.setDispatchedDate(item.getDispatchedDate());
            dto.setDispatchedByEmail(item.getDispatchedByEmail());
            dto.setDispatchedByName(item.getDispatchedByName());
            dto.setDcNo(item.getDcNo());
            dto.setEwayBillNo(item.getEwayBillNo());

            // Depot return dates
            dto.setDepotReturnDispatchDate(item.getDepotReturnDispatchDate());
            dto.setDepotReturnDeliveredDate(item.getDepotReturnDeliveredDate());

            // Delivery confirmation fields
            dto.setDeliveredTo(item.getDeliveredTo());
            dto.setDeliveredBy(item.getDeliveredBy());
            dto.setDeliveryDate(item.getDeliveryDate());
            dto.setDeliveryNotes(item.getDeliveryNotes());
            dto.setIsDelivered(item.getDeliveredTo() != null); // Mark as delivered if deliveredTo is set

            dtoList.add(dto);
        }
        return dtoList;
    }
}
