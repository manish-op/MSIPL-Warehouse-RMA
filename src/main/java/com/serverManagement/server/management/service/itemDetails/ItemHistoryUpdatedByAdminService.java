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

            // Clean the serial number - remove quotes if present from JSON string
            String cleanSerialNo = serialNo.trim().toLowerCase();
            if (cleanSerialNo.startsWith("\"") && cleanSerialNo.endsWith("\"")) {
                cleanSerialNo = cleanSerialNo.substring(1, cleanSerialNo.length() - 1);
            }

            // First try direct query from history table
            List<ItemHistoryUpdatedByAdminEntity> historyDetailsList = historyDAO
                    .getHistoryDetailsBySerialNo(cleanSerialNo);

            // Fallback to ItemDetailsEntity relationship if direct query returns empty
            if (historyDetailsList == null || historyDetailsList.isEmpty()) {
                historyDetailsList = itemDetailsDAO.getComponentHistorySerialNo(cleanSerialNo);
            }

            if (historyDetailsList == null || historyDetailsList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No history found for this serial number");
            }

            // Sort by latest update
            historyDetailsList.sort((a, b) -> b.getUpdate_Date().compareTo(a.getUpdate_Date()));

            return ResponseEntity.ok(historyDetailsList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
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

            // Check remark first for explicit action hints
            if (remark.contains("region changed") || remark.contains("quick update: region")) {
                action = "REGION_CHANGED";
            } else if (remark.contains("added") || remark.contains("created") || remark.contains("new item")) {
                action = "ADDED";
            } else if (h.getItemStatusId() != null) {
                // Check item status for NEW status
                String itemStatus = h.getItemStatusId().getItemStatus().toLowerCase();
                if (itemStatus.contains("new") || itemStatus.equals("added")) {
                    action = "ADDED";
                } else if (itemStatus.contains("repair")) {
                    action = "REPAIRING";
                } else if (itemStatus.contains("fault")) {
                    action = "FAULTY";
                } else if (h.getAvailableStatusId() != null) {
                    // Check availability status
                    String availStatus = h.getAvailableStatusId().getItemAvailableOption().toLowerCase();
                    if (availStatus.equals("issue") || availStatus.contains("issued")) {
                        action = "ISSUED";
                    } else if (availStatus.equals("available")) {
                        // For available items - check if it looks like a return based on remark
                        if (remark.contains("return")) {
                            action = "RETURNED";
                        } else {
                            action = "AVAILABLE";
                        }
                    } else if (availStatus.contains("repair")) {
                        action = "REPAIRING";
                    } else if (availStatus.equals("delete") || availStatus.contains("deleted")) {
                        action = "DELETED";
                    }
                }
            } else if (h.getAvailableStatusId() != null) {
                // Fallback: check availability status alone
                String availStatus = h.getAvailableStatusId().getItemAvailableOption().toLowerCase();
                if (availStatus.equals("issue") || availStatus.contains("issued")) {
                    action = "ISSUED";
                } else if (availStatus.equals("available")) {
                    action = "AVAILABLE";
                } else if (availStatus.contains("repair")) {
                    action = "REPAIRING";
                } else if (availStatus.equals("delete")) {
                    action = "DELETED";
                }
            }
            dto.setAction(action);

            result.add(dto);
        }

        return result;
    }
}
