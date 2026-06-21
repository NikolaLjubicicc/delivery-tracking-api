package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentFileParserResolver {

    private final List<ShipmentFileParser> parsers;

    public ShipmentFileParserResolver(List<ShipmentFileParser> parsers) {
        this.parsers = parsers;
    }

    public ShipmentFileParser resolve(String fileName, String contentType) {
        for (ShipmentFileParser parser : parsers) {
            if (parser.supports(fileName, contentType)) {
                return parser;
            }
        }
        throw new BusinessException("Unsupported file format. Please upload a .csv or .xlsx file");
    }
}
