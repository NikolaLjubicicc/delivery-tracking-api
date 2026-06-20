package com.rbt.delivery_tracking.dto.request;

import com.rbt.delivery_tracking.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChangeStatusRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    public ChangeStatusRequest() {}

    public ChangeStatusRequest(ShipmentStatus status, String note) {
        this.status = status;
        this.note = note;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
