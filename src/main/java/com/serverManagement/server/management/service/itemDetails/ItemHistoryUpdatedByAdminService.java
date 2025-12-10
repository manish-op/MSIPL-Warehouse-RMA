package com.serverManagement.server.management.service.itemDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.serverManagement.server.management.dto.itemRepairDetails.ActivityDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemHistoryUpdatedByAdminDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;

@Service
public class ItemHistoryUpdatedByAdminService {

    @Autowired
    private ItemHistoryUpdatedByAdminDAO historyDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private ItemDetailsDAO itemDetailsDAO;

    public ResponseEntity<?> getHistory(HttpServletRequest request, String serialNo) {
        String loggedInUserName;

        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }

        try {
            if (loggedInUserName == null || loggedInUserName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }

            AdminUserEntity adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());

            if (adminUserEntity == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
            }

            if (serialNo == null || serialNo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Serial number is required");
            }

            List<ItemHistoryUpdatedByAdminEntity> historyDetailsList = itemDetailsDAO
                    .getComponentHistorySerialNo(serialNo.trim().toLowerCase());

            if (historyDetailsList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No history found for this serial number");
            }

            // Sort by latest update
            historyDetailsList.sort((a, b) -> b.getUpdate_Date().compareTo(a.getUpdate_Date()));

            return ResponseEntity.ok(historyDetailsList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    public List<ActivityDto> getRecentActivities(HttpServletRequest request, int limit) {
        String loggedInUserName;

        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (Exception e) {
            return Collections.emptyList();
        }

        if (loggedInUserName == null || loggedInUserName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        AdminUserEntity user = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
        if (user == null) {
            return Collections.emptyList();
        }

        boolean isAdmin = user.getRoleModel() != null &&
                "admin".equalsIgnoreCase(user.getRoleModel().getRoleName());

        // Ensure limit bounds
        int pageSize = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, pageSize);

        List<ItemHistoryUpdatedByAdminEntity> rows;

        if (isAdmin) {
            rows = historyDAO.findRecentAll(pageable);
        } else {
            if (user.getRegionEntity() == null) {
                return Collections.emptyList();
            }
            String city = user.getRegionEntity().getCity();
            rows = historyDAO.findRecentByRegion(city != null ? city.toLowerCase() : null, pageable);
        }

        List<ActivityDto> result = new ArrayList<>();

        for (ItemHistoryUpdatedByAdminEntity h : rows) {
            ActivityDto dto = new ActivityDto();
            dto.setId(h.getId());
            dto.setSerialNo(h.getSerial_No());
            dto.setUpdatedByEmail(h.getUpdatedByEmail());
            dto.setRemark(h.getRemark());
            dto.setRegion(h.getRegion() != null ? h.getRegion().getCity() : null);
            dto.setUpdateDate(h.getUpdate_Date());

            // Enhanced audit fields
            dto.setKeyword(h.getKeywordEntity() != null ? h.getKeywordEntity().getKeywordName() : null);
            dto.setSubKeyword(h.getSubKeyWordEntity() != null ? h.getSubKeyWordEntity().getSubKeyword() : null);
            dto.setAvailabilityStatus(
                    h.getAvailableStatusId() != null ? h.getAvailableStatusId().getItemAvailableOption() : null);
            dto.setItemStatus(h.getItemStatusId() != null ? h.getItemStatusId().getItemStatus() : null);

            // Determine action type based on available data
            String action = "UPDATED";
            String remark = h.getRemark() != null ? h.getRemark().toLowerCase() : "";

            if (remark.contains("region changed") || remark.contains("quick update: region")) {
                action = "REGION_CHANGED";
            } else if (remark.contains("added") || remark.contains("created") || remark.contains("new item")) {
                action = "ADDED";
            } else if (h.getAvailableStatusId() != null) {
                String status = h.getAvailableStatusId().getItemAvailableOption().toLowerCase();
                if (status.equals("issue")) {
                    action = "ISSUED";
                } else if (status.equals("available")) {
                    // Check if the item was previously issued (it's a return)
                    // or if it's a new addition to inventory
                    // For first-time entries, we check if this is the only/first history record
                    if (remark.isEmpty() && h.getKeywordEntity() != null && h.getItemStatusId() != null) {
                        // If multiple fields are set together (keyword, status, etc), it's likely a new
                        // addition
                        action = "ADDED";
                    } else {
                        action = "RETURNED";
                    }
                } else if (status.equals("repairing")) {
                    action = "SENT_FOR_REPAIR";
                } else if (status.equals("delete")) {
                    action = "DELETED";
                }
            }
            dto.setAction(action);

            result.add(dto);
        }

        return result;
    }
}
