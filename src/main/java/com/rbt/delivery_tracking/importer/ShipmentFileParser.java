package com.rbt.delivery_tracking.importer;

import java.io.InputStream;
import java.util.List;

public interface ShipmentFileParser {

    boolean supports(String fileName, String contentType);

    List<ShipmentImportRow> parse(InputStream inputStream);
}
