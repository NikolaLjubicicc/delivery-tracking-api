package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.importer.dto.ShipmentImportRow;

import java.io.InputStream;
import java.util.List;

public interface ShipmentFileParser {

    boolean supports(String fileName, String contentType);

    List<ShipmentImportRow> parse(InputStream inputStream);
}
