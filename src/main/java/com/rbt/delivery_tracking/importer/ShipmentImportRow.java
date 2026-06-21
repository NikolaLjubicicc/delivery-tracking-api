package com.rbt.delivery_tracking.importer;

public class ShipmentImportRow {

    private int rowNumber;
    private String userId;
    private String description;
    private String status;

    public ShipmentImportRow() {}

    public ShipmentImportRow(int rowNumber, String userId, String description, String status) {
        this.rowNumber = rowNumber;
        this.userId = userId;
        this.description = description;
        this.status = status;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
