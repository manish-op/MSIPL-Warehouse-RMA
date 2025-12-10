package com.serverManagement.server.management.controller.activityLog;

import com.serverManagement.server.management.dto.itemRepairDetails.ActivityDto;
import com.serverManagement.server.management.service.itemDetails.ItemHistoryUpdatedByAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity-logs")
public class ActivityLog {

    public String test() {
        return "Activity Controller";
    }




}
