package com.serverManagement.server.management.enums;

public enum RepairStatus {
    ASSIGNED("Assigned"),
    REPAIRING("Repairing"),
    REPAIRED("Repaired"),
    BER("BER"),
    REPLACED("Replaced"),
    CANT_BE_REPAIRED("Can't Be Repaired");

    private final String displayName;

    RepairStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
