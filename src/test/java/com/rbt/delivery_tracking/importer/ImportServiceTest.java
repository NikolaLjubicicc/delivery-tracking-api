package com.rbt.delivery_tracking.importer;

import com.rbt.delivery_tracking.dto.response.ImportResultResponse;
import com.rbt.delivery_tracking.dto.response.ImportRowError;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.repository.ShipmentRepository;
import com.rbt.delivery_tracking.repository.ShipmentStatusHistoryRepository;
import com.rbt.delivery_tracking.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private ShipmentFileParserResolver parserResolver;

    @Mock
    private ShipmentFileParser parser;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusHistoryRepository historyRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ImportService importService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(importService, "entityManager", entityManager);
    }

    @Test
    void importShipments_savesValidRowsAndReportsInvalidOnes() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("shipments.csv");
        when(file.getContentType()).thenReturn("text/csv");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(parserResolver.resolve(any(), any())).thenReturn(parser);

        List<ShipmentImportRow> rows = new ArrayList<>();
        rows.add(new ShipmentImportRow(2, "marko@example.com", "Laptop", "CREATED"));       // valid
        rows.add(new ShipmentImportRow(3, "marko@example.com", "Knjige", "IN_TRANSIT"));    // valid
        rows.add(new ShipmentImportRow(4, "", "Bad email", ""));                            // missing email
        rows.add(new ShipmentImportRow(5, "nobody@example.com", "No such user", "CREATED")); // user not found
        rows.add(new ShipmentImportRow(6, "marko@example.com", "Bad status", "FOOBAR"));    // invalid status
        when(parser.parse(any())).thenReturn(rows);

        User user = new User("Marko", "marko@example.com", "+381");
        user.setId(1L);
        when(userRepository.findAllByEmailIn(any())).thenReturn(List.of(user));
        when(shipmentRepository.getNextTrackingSequences(anyInt())).thenReturn(List.of(100L, 101L));

        ImportResultResponse result = importService.importShipments(file);

        assertEquals(5, result.getTotalRows());
        assertEquals(2, result.getImported());
        assertEquals(3, result.getFailed());
        assertEquals(3, result.getErrors().size());

        Set<Integer> errorRows = new HashSet<>();
        for (ImportRowError error : result.getErrors()) {
            errorRows.add(error.getRow());
        }
        assertTrue(errorRows.contains(4));
        assertTrue(errorRows.contains(5));
        assertTrue(errorRows.contains(6));

        verify(shipmentRepository).saveAll(any());
        verify(historyRepository).saveAll(any());
    }

    @Test
    void importShipments_emptyFileThrows() {
        when(file.isEmpty()).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.rbt.delivery_tracking.exception.BusinessException.class,
                () -> importService.importShipments(file));
    }
}
