package com.posty.postingapi.domain.common;

public enum ScheduleStatus {
    SCHEDULED,
    CANCELLED,
    IN_PROGRESS,
    COMPLETED,
    FAILED;

    public boolean canReschedule() {
        return switch (this) {
            case CANCELLED, FAILED -> true;
            default -> false;
        };
    }
}
