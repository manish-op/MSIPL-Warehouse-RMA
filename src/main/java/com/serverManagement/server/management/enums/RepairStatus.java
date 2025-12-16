package com.serverManagement.server.management.enums;

public enum RepairStatus {
    ASSIGNED("Assigned"),
    REPAIRING("Repairing"),
    REPAIRED("Repaired"),
    CANT_BE_REPAIRED("Can't Be Repaired"),
    BER("BER");

    private final String displayName;

    RepairStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
