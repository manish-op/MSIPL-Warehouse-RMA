package com.serverManagement.server.management.service.employeeList;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.serverManagement.server.management.dto.itemRepairDetails.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class GetEmployeeListService {

    // Online timeout in minutes
    private static final int ONLINE_TIMEOUT_MINUTES = 5;

    @Autowired
    private AdminUserDAO adminUserDAO; // <-- Use your actual repository

    public ResponseEntity<?> getEmployeeList(HttpServletRequest httpRequest, String regionName) {

        try {
            List<AdminUserEntity> users;

            // 1. Fetch users from the database
            if (regionName != null && !regionName.isEmpty()) {
                // You'll need to create this method in your repository
                users = adminUserDAO.findByRegionEntityCity(regionName);
            } else {
                users = adminUserDAO.findAll();
            }

            // 2. Convert the AdminUserEntity list to a UserResponseDTO list
            List<UserResponseDTO> userResponseList = users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // 3. Return the new DTO list (this will be the JSON)
            return ResponseEntity.ok(userResponseList);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching user list: " + e.getMessage());
        }
    }

    private UserResponseDTO convertToDTO(AdminUserEntity user) {

        String roleName = "N/A"; // Default value
        if (user.getRoleModel() != null) {

            roleName = user.getRoleModel().getRoleName();
        }

        String regionName = "N/A"; // Default value
        if (user.getRegionEntity() != null) {

            regionName = user.getRegionEntity().getCity();
        }

        // Calculate online status (active within last 5 minutes)
        ZonedDateTime lastActive = user.getLastActiveAt();
        boolean isOnline = false;
        if (lastActive != null) {
            long minutesSinceActive = ChronoUnit.MINUTES.between(lastActive, ZonedDateTime.now());
            isOnline = minutesSinceActive < ONLINE_TIMEOUT_MINUTES;
        }

        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNo(),
                roleName,
                regionName,
                isOnline,
                lastActive);
    }
}