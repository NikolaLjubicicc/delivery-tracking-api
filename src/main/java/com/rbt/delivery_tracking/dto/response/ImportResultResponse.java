package com.rbt.delivery_tracking.dto.response;

import java.util.List;

public class ImportResultResponse {

    private int totalRows;
    private int imported;
    private int failed;
    private List<ImportRowError> errors;

    public ImportResultResponse() {}

    public ImportResultResponse(int totalRows, int imported, int failed, List<ImportRowError> errors) {
        this.totalRows = totalRows;
        this.imported = imported;
        this.failed = failed;
        this.errors = errors;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<ImportRowError> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportRowError> errors) {
        this.errors = errors;
    }
}
