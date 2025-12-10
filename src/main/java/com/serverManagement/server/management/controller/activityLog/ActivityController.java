package com.serverManagement.server.management.controller.activityLog;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dto.itemRepairDetails.ActivityDto;
import com.serverManagement.server.management.service.itemDetails.ItemHistoryUpdatedByAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ActivityController {
    @Autowired
    private ItemHistoryUpdatedByAdminService historyService;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @GetMapping("/activity")
    public ResponseEntity<?> getRecentActivity(
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            HttpServletRequest request) {
        try {
            List<ActivityDto> activities = historyService.getRecentActivities(request, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsersCount() {
        try {
            // Count users active in the last 5 minutes
            ZonedDateTime cutoffTime = ZonedDateTime.now().minusMinutes(5);
            Long onlineCount = adminUserDAO.countOnlineUsers(cutoffTime);

            Map<String, Object> response = new HashMap<>();
            response.put("onlineUsers", onlineCount != null ? onlineCount : 0);
            response.put("cutoffMinutes", 5);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
