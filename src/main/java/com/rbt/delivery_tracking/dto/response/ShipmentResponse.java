package com.rbt.delivery_tracking.dto.response;

import com.rbt.delivery_tracking.enums.ShipmentStatus;

import java.time.LocalDateTime;

public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private String description;
    private ShipmentStatus currentStatus;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShipmentResponse() {}

    public ShipmentResponse(Long id, String trackingNumber, String description, ShipmentStatus currentStatus,
                            Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.trackingNumber = trackingNumber;
        this.description = description;
        this.currentStatus = currentStatus;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ShipmentStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ShipmentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
