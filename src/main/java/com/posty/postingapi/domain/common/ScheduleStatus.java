package com.posty.postingapi.domain.common;

public enum ScheduleStatus {
    SCHEDULED,
    CANCELLED,
    IN_PROGRESS,
    COMPLETED,
    FAILED;

    public boolean canReschedule() {
        switch (this) {
            case CANCELLED:
            case FAILED:
                return true;
            case SCHEDULED:
            case IN_PROGRESS:
            case COMPLETED:
            default:
                return false;
        }
    }
}
