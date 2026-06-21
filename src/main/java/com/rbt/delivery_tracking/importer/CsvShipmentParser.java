package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.exception.BusinessException;
import com.rbt.delivery_tracking.importer.dto.ShipmentImportRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvShipmentParser implements ShipmentFileParser {

    @Override
    public boolean supports(String fileName, String contentType) {
        return fileName != null && fileName.toLowerCase().endsWith(".csv");
    }

    @Override
    public List<ShipmentImportRow> parse(InputStream inputStream) {
        List<ShipmentImportRow> rows = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(skipBom(inputStream), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            int rowNumber = 1;
            for (CSVRecord record : parser) {
                rowNumber++;
                ShipmentImportRow row = new ShipmentImportRow(
                        rowNumber,
                        getValue(record, "userId"),
                        getValue(record, "description"),
                        getValue(record, "status")
                );
                rows.add(row);
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to read CSV file: " + e.getMessage());
        }

        return rows;
    }

    private String getValue(CSVRecord record, String column) {
        if (record.isMapped(column) && record.isSet(column)) {
            return record.get(column);
        }
        return null;
    }

    private InputStream skipBom(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackStream = new PushbackInputStream(inputStream, 3);
        byte[] bom = new byte[3];
        int read = pushbackStream.read(bom, 0, 3);
        boolean hasBom = read == 3
                && (bom[0] & 0xFF) == 0xEF
                && (bom[1] & 0xFF) == 0xBB
                && (bom[2] & 0xFF) == 0xBF;
        if (!hasBom && read > 0) {
            pushbackStream.unread(bom, 0, read);
        }
        return pushbackStream;
    }
}

