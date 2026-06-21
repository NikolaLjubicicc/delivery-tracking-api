package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.entity.Shipment;
import com.rbt.delivery_tracking.entity.ShipmentStatusHistory;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.enums.ShipmentStatus;
import com.rbt.delivery_tracking.dto.response.ImportResultResponse;
import com.rbt.delivery_tracking.dto.response.ImportRowError;
import com.rbt.delivery_tracking.exception.BusinessException;
import com.rbt.delivery_tracking.repository.ShipmentRepository;
import com.rbt.delivery_tracking.repository.ShipmentStatusHistoryRepository;
import com.rbt.delivery_tracking.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ImportService {

    private static final int CHUNK_SIZE = 1000;

    private final ShipmentFileParserResolver parserResolver;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository historyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ImportService(ShipmentFileParserResolver parserResolver,
                         UserRepository userRepository,
                         ShipmentRepository shipmentRepository,
                         ShipmentStatusHistoryRepository historyRepository) {
        this.parserResolver = parserResolver;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public ImportResultResponse importShipments(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file is empty");
        }

        ShipmentFileParser parser = parserResolver.resolve(file.getOriginalFilename(), file.getContentType());

        List<ShipmentImportRow> rows;
        try {
            rows = parser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("Failed to read uploaded file: " + e.getMessage());
        }

        List<ImportRowError> errors = new ArrayList<>();

        List<ParsedRow> parsedRows = new ArrayList<>();
        Set<String> emails = new HashSet<>();
        for (ShipmentImportRow row : rows) {
            try {
                String email = parseEmail(row.getEmail());
                ShipmentStatus status = parseStatus(row.getStatus());
                parsedRows.add(new ParsedRow(row.getRowNumber(), email, row.getDescription(), status));
                emails.add(email);
            } catch (RuntimeException ex) {
                errors.add(new ImportRowError(row.getRowNumber(), ex.getMessage()));
            }
        }

        Map<String, User> userMap = loadUsersByEmail(emails);

        List<Shipment> validShipments = new ArrayList<>();
        for (ParsedRow parsed : parsedRows) {
            User user = userMap.get(parsed.email);
            if (user == null) {
                errors.add(new ImportRowError(parsed.rowNumber, "User with email '" + parsed.email + "' not found"));
                continue;
            }
            validShipments.add(new Shipment(null, parsed.description, parsed.status, user));
        }

        assignTrackingNumbers(validShipments);
        persistInChunks(validShipments);

        return new ImportResultResponse(rows.size(), validShipments.size(), errors.size(), errors);
    }

    private Map<String, User> loadUsersByEmail(Set<String> emails) {
        Map<String, User> userMap = new HashMap<>();
        if (emails.isEmpty()) {
            return userMap;
        }
        List<User> users = userRepository.findAllByEmailIn(new ArrayList<>(emails));
        for (User user : users) {
            userMap.put(user.getEmail(), user);
        }
        return userMap;
    }

    private void assignTrackingNumbers(List<Shipment> shipments) {
        if (shipments.isEmpty()) {
            return;
        }
        List<Long> sequenceValues = shipmentRepository.getNextTrackingSequences(shipments.size());
        for (int i = 0; i < shipments.size(); i++) {
            shipments.get(i).setTrackingNumber(String.format("TRK-%010d", sequenceValues.get(i)));
        }
    }

    private void persistInChunks(List<Shipment> shipments) {
        for (int start = 0; start < shipments.size(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, shipments.size());
            List<Shipment> chunk = shipments.subList(start, end);

            shipmentRepository.saveAll(chunk);
            shipmentRepository.flush();

            List<ShipmentStatusHistory> histories = new ArrayList<>();
            for (Shipment shipment : chunk) {
                histories.add(new ShipmentStatusHistory(shipment, shipment.getCurrentStatus(), "Imported"));
            }
            historyRepository.saveAll(histories);
            historyRepository.flush();

            entityManager.clear();
        }
    }

    private String parseEmail(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("email is required");
        }
        return raw.trim();
    }

    private ShipmentStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return ShipmentStatus.CREATED;
        }
        try {
            return ShipmentStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status '" + raw + "'");
        }
    }

    private static final class ParsedRow {
        private final int rowNumber;
        private final String email;
        private final String description;
        private final ShipmentStatus status;

        private ParsedRow(int rowNumber, String email, String description, ShipmentStatus status) {
            this.rowNumber = rowNumber;
            this.email = email;
            this.description = description;
            this.status = status;
        }
    }
}
