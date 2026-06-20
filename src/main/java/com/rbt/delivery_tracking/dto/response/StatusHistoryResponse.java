package com.rbt.delivery_tracking.dto.response;

import com.rbt.delivery_tracking.enums.ShipmentStatus;

import java.time.LocalDateTime;

public class StatusHistoryResponse {

    private ShipmentStatus status;
    private String note;
    private LocalDateTime changedAt;

    public StatusHistoryResponse() {}

    public StatusHistoryResponse(ShipmentStatus status, String note, LocalDateTime changedAt) {
        this.status = status;
        this.note = note;
        this.changedAt = changedAt;
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

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
