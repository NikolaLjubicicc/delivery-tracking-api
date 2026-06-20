package com.rbt.delivery_tracking.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateShipmentRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public UpdateShipmentRequest() {}

    public UpdateShipmentRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
