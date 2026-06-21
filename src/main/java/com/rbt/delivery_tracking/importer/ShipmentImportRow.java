package com.rbt.delivery_tracking.importer;

public class ShipmentImportRow {

    private int rowNumber;
    private String email;
    private String description;
    private String status;

    public ShipmentImportRow() {}

    public ShipmentImportRow(int rowNumber, String email, String description, String status) {
        this.rowNumber = rowNumber;
        this.email = email;
        this.description = description;
        this.status = status;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
