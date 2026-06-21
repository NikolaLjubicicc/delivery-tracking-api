package com.rbt.delivery_tracking.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipmentStatusTest {

    @Test
    void allowedTransitions() {
        assertTrue(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.PICKED_UP));
        assertTrue(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.CANCELLED));
        assertTrue(ShipmentStatus.PICKED_UP.canTransitionTo(ShipmentStatus.IN_TRANSIT));
        assertTrue(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY));
        assertTrue(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.DELIVERED));
    }

    @Test
    void disallowedTransitions() {
        assertFalse(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.DELIVERED));
        assertFalse(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.IN_TRANSIT));
        assertFalse(ShipmentStatus.PICKED_UP.canTransitionTo(ShipmentStatus.DELIVERED));
    }

    @Test
    void terminalStatusesCannotTransition() {
        assertFalse(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.IN_TRANSIT));
        assertFalse(ShipmentStatus.CANCELLED.canTransitionTo(ShipmentStatus.CREATED));
    }

    @Test
    void isTerminal() {
        assertTrue(ShipmentStatus.DELIVERED.isTerminal());
        assertTrue(ShipmentStatus.CANCELLED.isTerminal());
        assertFalse(ShipmentStatus.CREATED.isTerminal());
        assertFalse(ShipmentStatus.IN_TRANSIT.isTerminal());
    }
}
