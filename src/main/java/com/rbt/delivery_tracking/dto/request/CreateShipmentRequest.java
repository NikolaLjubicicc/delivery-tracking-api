package com.rbt.delivery_tracking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateShipmentRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public CreateShipmentRequest() {}

    public CreateShipmentRequest(Long userId, String description) {
        this.userId = userId;
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
