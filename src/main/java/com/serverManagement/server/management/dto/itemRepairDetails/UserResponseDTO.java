package com.serverManagement.server.management.dto.itemRepairDetails;

import java.time.ZonedDateTime;

public class UserResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String mobileNo;
    private String role; // <-- Will hold the role name
    private String regionName; // <-- Will hold the region name
    private boolean isOnline; // <-- Online status (active within 5 minutes)
    private ZonedDateTime lastActiveAt; // <-- Last activity timestamp

    // --- Constructors ---
    public UserResponseDTO() {
    }

    public UserResponseDTO(Long userId, String name, String email, String mobileNo, String role, String regionName) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
        this.role = role;
        this.regionName = regionName;
    }

    public UserResponseDTO(Long userId, String name, String email, String mobileNo, String role, String regionName,
            boolean isOnline, ZonedDateTime lastActiveAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
        this.role = role;
        this.regionName = regionName;
        this.isOnline = isOnline;
        this.lastActiveAt = lastActiveAt;
    }

    // --- Add all Getters and Setters ---

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public ZonedDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(ZonedDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
