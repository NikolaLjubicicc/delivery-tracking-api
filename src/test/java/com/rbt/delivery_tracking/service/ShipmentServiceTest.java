package com.rbt.delivery_tracking.service;

import com.rbt.delivery_tracking.dto.request.ChangeStatusRequest;
import com.rbt.delivery_tracking.dto.request.CreateShipmentRequest;
import com.rbt.delivery_tracking.dto.response.ShipmentResponse;
import com.rbt.delivery_tracking.entity.Shipment;
import com.rbt.delivery_tracking.entity.ShipmentStatusHistory;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.enums.ShipmentStatus;
import com.rbt.delivery_tracking.exception.BusinessException;
import com.rbt.delivery_tracking.exception.NotFoundException;
import com.rbt.delivery_tracking.repository.ShipmentRepository;
import com.rbt.delivery_tracking.repository.ShipmentStatusHistoryRepository;
import com.rbt.delivery_tracking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private User user() {
        User user = new User("Marko Markovic", "marko@example.com", "+381641234567");
        user.setId(1L);
        return user;
    }

    @Test
    void createShipment_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(shipmentRepository.getNextTrackingSequence()).thenReturn(5L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment shipment = invocation.getArgument(0);
            shipment.setId(10L);
            return shipment;
        });

        ShipmentResponse response = shipmentService.createShipment(new CreateShipmentRequest(1L, "Laptop"));

        assertEquals("TRK-0000000005", response.getTrackingNumber());
        assertEquals(ShipmentStatus.CREATED, response.getCurrentStatus());
        assertEquals(1L, response.getUserId().longValue());

        ArgumentCaptor<ShipmentStatusHistory> historyCaptor = ArgumentCaptor.forClass(ShipmentStatusHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals(ShipmentStatus.CREATED, historyCaptor.getValue().getStatus());
    }

    @Test
    void createShipment_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> shipmentService.createShipment(new CreateShipmentRequest(99L, "Laptop")));
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void changeStatus_validTransition() {
        Shipment shipment = new Shipment("TRK-0000000001", "Laptop", ShipmentStatus.CREATED, user());
        shipment.setId(1L);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponse response = shipmentService.changeStatus(1L, new ChangeStatusRequest(ShipmentStatus.PICKED_UP, "Picked up"));

        assertEquals(ShipmentStatus.PICKED_UP, response.getCurrentStatus());

        ArgumentCaptor<ShipmentStatusHistory> historyCaptor = ArgumentCaptor.forClass(ShipmentStatusHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals(ShipmentStatus.PICKED_UP, historyCaptor.getValue().getStatus());
        assertEquals("Picked up", historyCaptor.getValue().getNote());
    }

    @Test
    void changeStatus_invalidTransition() {
        Shipment shipment = new Shipment("TRK-0000000001", "Laptop", ShipmentStatus.DELIVERED, user());
        shipment.setId(1L);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        assertThrows(BusinessException.class,
                () -> shipmentService.changeStatus(1L, new ChangeStatusRequest(ShipmentStatus.IN_TRANSIT, null)));
        verify(historyRepository, never()).save(any());
    }

    @Test
    void getById_notFound() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shipmentService.getById(99L));
    }
}
