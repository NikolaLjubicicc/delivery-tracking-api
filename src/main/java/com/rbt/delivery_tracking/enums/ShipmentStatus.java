package com.rbt.delivery_tracking.enums;

public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        if (this.isTerminal()) {
            return false;
        }
        return switch (this) {
            case CREATED          -> next == PICKED_UP || next == CANCELLED;
            case PICKED_UP        -> next == IN_TRANSIT || next == CANCELLED;
            case IN_TRANSIT       -> next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == CANCELLED;
            default               -> false;
        };
    }
}
