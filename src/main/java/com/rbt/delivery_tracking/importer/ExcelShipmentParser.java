package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.exception.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelShipmentParser implements ShipmentFileParser {

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public boolean supports(String fileName, String contentType) {
        return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
    }

    @Override
    public List<ShipmentImportRow> parse(InputStream inputStream) {
        List<ShipmentImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel file has no sheets");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new BusinessException("Excel file has no header row");
            }
            Map<String, Integer> headerIndex = readHeader(headerRow);

            int lastRow = sheet.getLastRowNum();
            for (int i = headerRow.getRowNum() + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                ShipmentImportRow importRow = new ShipmentImportRow(
                        i + 1,
                        getCellValue(row, headerIndex, "email"),
                        getCellValue(row, headerIndex, "description"),
                        getCellValue(row, headerIndex, "status")
                );
                rows.add(importRow);
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to read Excel file: " + e.getMessage());
        }

        return rows;
    }

    private Map<String, Integer> readHeader(Row headerRow) {
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell != null) {
                String name = dataFormatter.formatCellValue(cell).trim().toLowerCase();
                if (!name.isEmpty()) {
                    headerIndex.put(name, c);
                }
            }
        }
        return headerIndex;
    }

    private String getCellValue(Row row, Map<String, Integer> headerIndex, String column) {
        Integer index = headerIndex.get(column);
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        String value = dataFormatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private boolean isEmptyRow(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !dataFormatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
